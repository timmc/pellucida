(ns org.timmc.pellucida.listing
  "A tabular, filterable listing view showing thumbnails of many photos."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer (defroutes GET)]
   (org.timmc.pellucida (db :refer (read-db))
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

;;;; html

(defn std [] (e/html-resource "org/timmc/pellucida/html/standard.html"))
(defn pg [] (e/html-resource "org/timmc/pellucida/html/listing.html"))

(defn render "Render an enlive dom as an HTML string."
  [dom]
  (apply str (e/emit* dom)))

(defn ths-one "Transformation for a .ths-one node using a photo record."
  [p]
  (e/transformation
   ;; TODO: use URL formatter
   [:a.ths-goto] (e/set-attr :href (format "/image/%d" (:imageID p)))
   [:.ths-title] (e/content (:label p))
   [:img.ths-solo] (e/set-attr :src (ln/photo (:imageID p) :thumb))
   [:.ths-meta] (e/content (:added p))))

(defn list-page "Render a listing of recent photos."
  []
  (e/at (std)
        [:title] (e/content "Listing of photos")
        [:head] (e/append (-> (e/select (pg) [:head]) first e/unwrap))
        [:.std-ptitle] (e/content "Recent photos")
        ;; Replace the contents of .std-body in the standard template with
        ;; the contents of the (transformed) .std-body from the page template.
        [:.std-body] (e/content
                      (e/at (e/select (pg) [:.std-body])
                            [:.ths-container :.ths-one]
                            (e/clone-for [p (recent-photos {})]
                                         (ths-one p))))))

(defroutes listing-routes
  (GET "/" [] (render (list-page))))
