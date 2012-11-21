(ns org.timmc.pellucida.listing
  "A tabular, filterable listing view showing thumbnails of many photos."
  (:require (hiccup (core :refer (h))
                    (page :refer (html5)))
            [compojure.core :refer (defroutes GET)]))

(defn list-page "Render a listing of recent photos."
  []
  (html5 "hello"))

(defroutes listing-routes
  (GET "/list" [] (list-page)))
