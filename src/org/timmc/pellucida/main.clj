(ns org.timmc.pellucida.main
  "Main page. Maybe show a few photos."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/main.html"))

(defn recent-photos
  []
  (db/read
   (sql/with-query-results r
     ["select * from image order by imageID desc limit 3"]
     (doall r))))

(defn main-page
  []
  (lay/standard
   (pg)
   (e/transformation
    [:#teaser :.imglink]
    (e/clone-for [p (recent-photos)]
                 (e/transformation
                  [:a] (e/set-attr :href (ln/single (:imageID p)))
                  [:img] (e/set-attr :src (ln/photo (:imageID p) :thumb))))

    [:.go-to-gallery]
    (e/set-attr :href (ln/listing [] 0)))

   {:doc-title "Tim McCormack's photo gallery"
    :page-title "Tim McCormack's photo gallery"}))

(defroutes main-routes
  (GET "/" [] (lay/render (main-page))))
