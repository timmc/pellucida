(ns org.timmc.pellucida.res.feed
  "An Atom feed of recent images"
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (filter :as filter)
                        (mode :as m)
                        (layout :as lay)
                        (link :as ln))))

;;;; data

(defn recent-images
  "Yield table of imageIDs."
  [mode]
  (db/read [(str "select *"
                 "from image "
                 "order by imageID desc "
                 "limit 20")]))

;;;; html

(def pg (e/xml-resource "org/timmc/pellucida/html/atom-feed.xml"))

(defn one-entry
  [photo-data]
  (e/transformation
   [:title] (e/content (:label photo-data))))

(defn feed-page "Produce an Atom feed of recent photos."
  [mode]
  (let [data (recent-images mode)]
    (e/at pg
          [:entry] (e/clone-for [x data]
                                (one-entry x)))))

(defroutes feed-routes
  (GET "/v2/feed" r
       (let [mode (m/from-request r)]
         {:status 200
          :headers {"Content-Type" "text/xml"}
          :body (lay/render (feed-page mode))})))
