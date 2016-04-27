(ns org.timmc.pellucida.res.single
  "Showing just one photo."
  (:refer-clojure :exclude [if-let])
  (:require
   [net.cgrand.enlive-html :as e]
   [clojure.string :as str]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (link :as ln)
                        (mode :as m)
                        (settings :as cnf))
   [org.timmc.handy :refer [if-let+] :rename {if-let+ if-let}]))

(defn photo-data
  [id]
  {:pre [(integer? id)]}
  (first (db/read ["SELECT * FROM image WHERE imageID = ?" id])))

(defn get-tags
  "Yield a coll of maps :cat, :tag, :implicit for an image by ID."
  [id]
  {:pre [(integer? id)]}
  (db/read
   ["select cat, tag, implicit
     from imagetags
     where imageID = ?
     order by cat asc, tag asc" id]))

(def pg (e/html-resource "org/timmc/pellucida/html/single.html"))

;; TODO:
;; Previous 3 images:
;;   select imageID from image where imageID < ? order by imageID desc limit 3;
;; Next 3 images:
;;   select imageID from image where imageID > ? order by imageID asc limit 3;

(defn tags-block
  [mode tags]
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
                           (e/set-attr :href (ln/listing mode [filter] 0)))))))
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
string, either filling it in or deleting it."
  [im-data tags]
  (if-let [api-key (@cnf/config :google-static-maps-v2-api-key-browser)
           is-geocoded? (tag-match? tags [["Location" "geocode"]])
           coords (find-geocode tags)]
    (let [a-href (format "https://maps.google.com/maps?q=loc:%s(%s)&t=h&iwloc=0"
                         (pencode coords)
                         (pencode (:label im-data "Photo location")))
          img-src (str "https://maps.googleapis.com/maps/api/staticmap?"
                       (str/join \&
                                 ["zoom=13"
                                  "size=300x300"
                                  "maptype=hybrid"
                                  (str "markers=" (pencode coords))
                                  (str "key=" (pencode api-key))]))]
      (e/transformation
       [:.smd-block.geocode] (e/set-attr "data-geocode" coords)
       [:a.gco-link] (e/set-attr "href" a-href)
       [:a.gco-link :img] (e/set-attr "src" img-src
                                      "alt" (str "Photo taken at " coords))))
    delete))

(defn sidemod-libre
  []
  (let [btc-addr (@cnf/config :btc-donate-addr)]
    (e/transformation
     [:.para-donate] (if btc-addr
                       (e/transformation
                        [:a.donate] (e/set-attr "href"
                                                (str "bitcoin:" btc-addr))
                        [:.bitcoin-addr] (e/content btc-addr))
                       delete))))

(defn single-page "Render a page for a single photo."
  [mode id]
  {:pre [(integer? id)]}
  (if-let [data (photo-data id)]
    (let [tags (get-tags id)
          basename (:basename data)
          suffixes (get-in @db/last-check [:config "sizeSuffixes"])]
      (lay/render
       (lay/standard
        pg
        (e/transformation
         [:.view-fullsize] (e/set-attr :href
                                       (ln/photo basename suffixes :fullsize))
         [:.view-fullsize :img] (e/set-attr :src
                                            (ln/photo basename suffixes :solo))
         [:.description] (e/content (:description data))
         [:.md-date] (e/content (str (:startDate data)))
         [:.md-angle] (e/content (str (:angle data)))
         [:.md-dim] (e/content (format "%d x %d" (:width data) (:height data)))
         [:#tags] (tags-block mode tags)

         [:.smd-block.unidentified]
         (if (tag-match? tags [["Meta" "unidentified"]
                               ["Meta" "identification unsure"]])
           no-op
           delete)

         [:.smd-block.geocode]
         (sidemod-geocode data tags)

         [:.smd-block.license.nonfree]
         (if (tag-match? tags [["Meta" "free-licensed"]])
           delete
           no-op)

         [:.smd-block.license.libre]
         (if (tag-match? tags [["Meta" "free-licensed"]])
           (sidemod-libre)
           delete))
        {:doc-title (:label data)
         :page-title (:label data)
         :mode mode})))
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Image not found."}))

(defroutes single-routes
  ;; TODO decode path component before regex checking. *sigh*
  (GET ["/v2/image/:id", :id #"[0-9]+"] [id :as r]
       (let [mode (m/from-request r)]
         (single-page mode (Long/parseLong id)))))
