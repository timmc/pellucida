(ns org.timmc.pellucida.single
  "Showing just one photo."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

(defn photo-data
  [id]
  {:pre [(integer? id)]}
  (db/read
   (sql/with-query-results r
     ["SELECT * FROM image WHERE imageID = ?" id]
     (first r))))

(defn tags
  [id]
  {:pre [(integer? id)]}
  (db/read
   (sql/with-query-results r
     ["select catName, tagName, implicit
       from imagetags natural join tags natural join categories
       where imageID = ?
       order by catName asc, tagName asc" id]
     (doall r))))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/single.html"))

;; TODO:
;; Previous 3 images:
;;   select imageID from image where imageID < ? order by imageID desc limit 3;
;; Next 3 images:
;;   select imageID from image where imageID > ? order by imageID asc limit 3;

(defn tags-block
  [id]
  (e/transformation
   [:group] (e/do->
             (e/clone-for
              [[cat-name tags] (group-by :catName (tags id))]
              (e/transformation
               [:.category] (e/content cat-name)
               [:.tag] (e/clone-for
                        [tag tags]
                        (e/transformation
                         [:.tagname] (e/content (:tagName tag))
                         [:.implicit] (when (= 1 (:implicit tag))
                                        identity)
                         [:.tt]
                         (let [filter {:type :tt
                                       :cat (:catName tag)
                                       :tag (:tagName tag)}]
                           (e/set-attr :href (ln/listing [filter] 0)))))))
             e/unwrap)))

(defn single-page "Render a page for a single photo."
  [id]
  {:pre [(integer? id)]}
  (if-let [data (photo-data id)]
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
       [:#tags] (tags-block id))
      {:doc-title (:label data)
       :page-title (:label data)}))
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Image not found."}))

(defroutes single-routes
  (GET ["/image/:id", :id #"[0-9]+"]
       [id] (single-page (Long/parseLong id))))
