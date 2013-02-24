(ns org.timmc.pellucida.single
  "Showing just one photo."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer (defroutes GET)]
   (org.timmc.pellucida (db :refer (read-db))
                        (layout :as lay)
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

(defn photo-data
  [id]
  {:pre [(integer? id)]}
  (read-db
   (sql/with-query-results r
     ["SELECT * FROM image WHERE imageID = ?" id]
     (first r))))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/single.html"))

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
       [:.md-dim] (e/content (format "%d x %d" (:width data) (:height data))))
      {:doc-title (:label data)
       :page-title (:label data)}))
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Image not found."}))

(defroutes single-routes
  (GET ["/image/:id", :id #"[0-9]+"]
       [id] (single-page (Long/parseLong id))))
