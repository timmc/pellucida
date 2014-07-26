(ns org.timmc.pellucida.res.listing
  "A tabular, filterable listing view showing thumbnails of many photos."
  (:require
   [org.timmc.handy :as handy]
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (pager :as pager)
                        (filter :as filter)
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

;;;; data

(defn total-count
  [filters]
  (let [[fsql fparams] (reduce filter/sql-wrap nil filters)
        sql (str "select count(*) as cnt from "
                 (if fsql
                   (str "( " fsql " )")
                   "image"))
        params fparams]
    (db/read
     (sql/with-query-results r
       (db/jdbc-psql [sql params])
       (:cnt (first r))))))

(def per-page 30)

(defn recent-photos
  [pag filters]
  (let [[fsql fparams] (reduce filter/sql-wrap nil filters)
        sql (str "select * from image natural join image_meta "
                 (when fsql
                   (str " where imageID in ( " fsql " ) "))
                 (format " order by imageID desc limit %d offset %d"
                         per-page, (cast Long (:first-record pag))))
        params fparams]
    (db/read
     (sql/with-query-results r
       (db/jdbc-psql [sql params])
       (doall r)))))

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
  [cur-page filters]
  (let [pag (handy/paging (total-count filters) cur-page per-page)
        photos (when (:cur-valid pag)
                 (recent-photos pag filters))
        pager-node (pager/build-pager pag (partial ln/listing filters))]
    (lay/standard
     (pg)
     (e/transformation
      ;; TODO: Better out-of-bounds and no-results pages
      [:.ths-container :.ths-one] (e/clone-for [p photos]
                                               (ths-one p))
      [:.pgr-container] (e/content pager-node)
      [:.total-count] (e/content (str (total-count filters))))
     {:doc-title "Listing of photos"
      :page-title "Recent photos"})))

(defn maybe-param
  [request param parse default]
  (if-let [s (get-in request [:params param])]
    (parse s)
    default))

(defroutes listing-routes
  (GET "/list" [:as r]
       (let [page (maybe-param r :page #(Integer/parseInt %) 0)
             filters (filter/parse-request r)]
         (lay/render (list-page page filters)))))
