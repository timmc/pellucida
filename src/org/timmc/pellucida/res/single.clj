(ns org.timmc.pellucida.res.single
  "Showing just one photo."
  (:refer-clojure :exclude [if-let])
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (link :as ln)
                        (settings :as cnf))
   [org.timmc.handy :refer [if-let+] :rename {if-let+ if-let}]
   [clojure.java.jdbc :as sql]))

(defn photo-data
  [id]
  {:pre [(integer? id)]}
  (db/read
   (sql/with-query-results r
     ["SELECT * FROM image WHERE imageID = ?" id]
     (first r))))

(defn get-tags
  "Yield a coll of maps :cat, :tag, :implicit for an image by ID."
  [id]
  {:pre [(integer? id)]}
  (db/read
   (sql/with-query-results r
     ["select cat, tag, implicit
       from imagetags
       where imageID = ?
       order by cat asc, tag asc" id]
     (doall r))))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/single.html"))

;; TODO:
;; Previous 3 images:
;;   select imageID from image where imageID < ? order by imageID desc limit 3;
;; Next 3 images:
;;   select imageID from image where imageID > ? order by imageID asc limit 3;

(defn tags-block
  [tags]
  (e/transformation
   [:group] (e/do->
             (e/clone-for
              [[cat-name tags] (group-by :cat tags)]
              (e/transformation
               [:.category] (e/content cat-name)
               [:.tag] (e/clone-for
                        [tag tags]
                        (e/transformation
                         [:.tagname] (e/content (:tag tag))
                         [:.implicit] (when (= 1 (:implicit tag))
                                        identity)
                         [:.tt]
                         (let [filter {:type :tt
                                       :cat (:cat tag)
                                       :tag (:tag tag)}]
                           (e/set-attr :href (ln/listing [filter] 0)))))))
             e/unwrap)))

(defn tag-match?
  "Return truthy iff at least one of the cat/tag pair vectors in
`match-tags` matches the image tags."
  [im-tags match-tags]
  (seq (filter #(contains? (set match-tags) %)
               (for [{:keys [cat tag]} im-tags]
                 [cat tag]))))

(def no-op
  "Transformation that does nothing."
  (e/transformation))

(def delete
  "Transformation that deletes the selection."
  (e/substitute []))

(def geocode-match
  #"^(-?[0-9]{1,3}\.[0-9]{3,7}),(-?[0-9]{1,3}\.[0-9]{3,7})$")

(defn find-geocode
  "Find a geocode in a list of image tags, or nil."
  [im-tags]
  (->> im-tags
       (filter #(and (= (:cat %) "Location")
                     (re-matches geocode-match (:tag %))))
       first
       :tag))

(defn pencode
  "Percent-encode the input, conservatively."
  [s]
  (java.net.URLEncoder/encode s))

(defn sidemod-geocode
  "Transformation of the geocode sidebar module using a lat/long
string."
  [im-data code]
  (let [a-href (format "http://maps.google.com/maps?q=loc:%s(%s)&t=h&iwloc=0"
                       (pencode code)
                       (pencode (:label im-data "Photo location")))
        img-src (format "http://maps.google.com/staticmap?size=300x300&markers=%s&maptype=hybrid&key=%s"
                        (pencode code)
                        (pencode (cnf/config :gmaps-api-key)))]
    (e/transformation
     [:.smd-block.geocode] (e/set-attr "data-geocode" code)
     [:a.gco-link] (e/set-attr "href" a-href)
     [:a.gco-link :img] (e/set-attr "src" img-src
                                    "alt" (str "Photo taken at " code)))))

(defn sidemod-libre
  []
  (let [btc-addr (cnf/config :btc-donate-addr)]
    (e/transformation
     [:.para-donate] (if btc-addr
                       (e/transformation
                        [:a.donate] (e/set-attr "href"
                                                (str "bitcoin:" btc-addr))
                        [:.bitcoin-addr] (e/content btc-addr))
                       delete))))

(defn single-page "Render a page for a single photo."
  [id]
  {:pre [(integer? id)]}
  (if-let [data (photo-data id)]
    (let [tags (get-tags id)]
      (lay/render
       (lay/standard
        (pg)
        (e/transformation
         [:.view-fullsize] (e/set-attr :href (ln/photo id :fullsize))
         [:.view-fullsize :img] (e/set-attr :src (ln/photo id :solo))
         [:.description] (e/content (:description data))
         [:.md-date] (e/content (str (:startDate data)))
         [:.md-angle] (e/content (str (:angle data)))
         [:.md-dim] (e/content (format "%d x %d" (:width data) (:height data)))
         [:#tags] (tags-block tags)

         [:.smd-block.unidentified]
         (if (tag-match? tags [["Meta" "unidentified"]
                               ["Meta" "identification unsure"]])
           no-op
           delete)

         [:.smd-block.geocode]
         (if-let [_ (cnf/config :gmaps-api-key)
                  _ (tag-match? tags [["Location" "geocode"]])
                  code (find-geocode tags)]
           (sidemod-geocode data code)
           delete)

         [:.smd-block.license.nonfree]
         (if (tag-match? tags [["Meta" "free-licensed"]])
           delete
           no-op)

         [:.smd-block.license.libre]
         (if (tag-match? tags [["Meta" "free-licensed"]])
           (sidemod-libre)
           delete))
        {:doc-title (:label data)
         :page-title (:label data)})))
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Image not found."}))

(defroutes single-routes
  (GET ["/image/:id", :id #"[0-9]+"]
       [id] (single-page (Long/parseLong id))))