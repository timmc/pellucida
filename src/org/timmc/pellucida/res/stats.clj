(ns org.timmc.pellucida.res.stats
  "Page with statistics on images in DB."
  (:require
   [clojure.java.jdbc :as sql]
   [compojure.core :refer [defroutes GET]]
   [net.cgrand.enlive-html :as e]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (mode :as m))))

(defn stats-data
  []
  (let [date-sql #(format (str "SELECT date(startDate) as date"
                               " FROM image"
                               " ORDER BY startDate %s"
                               " LIMIT 1")
                          %)]
    (db/read
     {:count
      (sql/with-query-results r
        ["SELECT count(imageID) as cnt FROM image"]
        (:cnt (first r)))

      :earliest
      (sql/with-query-results r
        [(date-sql "ASC")]
        (:date (first r)))

      :latest
      (sql/with-query-results r
        [(date-sql "DESC")]
        (:date (first r)))})))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/stats.html"))

(defn stats-page
  [mode]
  (let [data (stats-data)]
    (lay/standard
     (pg)
     (e/transformation
      [:.sts-count] (e/content (str (:count data)))
      [:.sts-earliest] (e/content (:earliest data))
      [:.sts-latest] (e/content (:latest data)))
     {:doc-title "Image statistics"
      :page-title "Stats"
      :mode mode})))

(defroutes stats-routes
  (GET "/v2/about/stats" r
       (let [mode (m/from-request r)]
         (lay/render (stats-page mode)))))
