(ns org.timmc.pellucida.main
  "Main page. Maybe show a few photos."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer (defroutes GET)]
   (org.timmc.pellucida (layout :as lay)
                        (link :as ln))))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/main.html"))

(defn main-page
  []
  (lay/standard
   (pg)
   (e/transformation
    [:.go-to-gallery] (e/set-attr :href (ln/listing [] 0)))
   {:doc-title "Tim McCormack's photo gallery"
    :page-title "Tim McCormack's photo gallery"}))

(defroutes main-routes
  (GET "/" [] (lay/render (main-page))))
