(ns org.timmc.pellucida
  (:require
   (compojure (route :as route)
              (handler :as handler)
              (response :as response)
              (core :refer (defroutes GET)))
   [hiccup.middleware :refer (wrap-base-url)]
   (org.timmc.pellucida (listing :refer (listing-routes)))))

(defroutes all-routes
  listing-routes)

(def app "Server entrance point."
  (-> (handler/site all-routes)
      (wrap-base-url)))
