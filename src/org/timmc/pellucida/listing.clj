(ns org.timmc.pellucida.listing
  "A tabular, filterable listing view showing thumbnails of many photos."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer (defroutes GET)]
   (org.timmc.pellucida (db :refer (read-db))
                        (layout :as lay)
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

;;;; data

(defn recent-photos
  [{:keys [limit filters] :or {limit 30, filters []}}]
  {:pre [(integer? limit)]}
  (read-db
   (sql/with-query-results r
     [(format "SELECT *
               FROM image natural join image_meta
               order by imageID desc
               limit %d"
              limit)]
     (doall r))))

(defn total-count
  [{:keys [filters] :or {filters []}}]
  (read-db
   (sql/with-query-results r
     ["SELECT COUNT(*) as cnt FROM image"]
     (:cnt (first r)))))

;;;; html

(defn pg [] (e/html-resource "org/timmc/pellucida/html/listing.html"))

(defn ths-one "Transformation for a .ths-one node using a photo record."
  [p]
  (e/transformation
   ;; TODO: use URL formatter
   [:a.ths-goto] (e/set-attr :href (ln/single (:imageID p)))
   [:.ths-title] (e/content (:label p))
   [:img.ths-solo] (e/set-attr :src (ln/photo (:imageID p) :thumb))
   [:.ths-meta] (e/content (:added p))))

(defn list-page "Render a listing of recent photos."
  []
  (lay/standard
   (pg)
   (e/transformation
    [:.ths-container :.ths-one] (e/clone-for [p (recent-photos {})]
                                             (ths-one p))
    [:.total-count] (e/content (str (total-count {}))))
   {:doc-title "Listing of photos"
    :page-title "Recent photos"}))

(defroutes listing-routes
  (GET "/" [] (lay/render (list-page))))
