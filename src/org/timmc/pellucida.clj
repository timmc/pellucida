(ns org.timmc.pellucida
  (:require
   (compojure (route :as route)
              (handler :as handler)
              (response :as response)
              (core :refer (defroutes GET)))
   [hiccup.middleware :refer (wrap-base-url)]
   (org.timmc.pellucida (listing :refer (listing-routes))
                        (thumbs :refer (thumb-routes))
                        (db :refer (*db-spec*)))))

(defroutes all-routes
  thumb-routes
  listing-routes)

#_
(if-let [db-path (System/getenv "PELL_DB")]
  (reset! db-loc (.getAbsolutePath (java.io.File. db-path)))
  (throw (RuntimeException. "Missing PELL_DB environment variable.")))
#_
(if-let [thumbs-path (System/getenv "PELL_THUMBS")]
  (reset! thumbs-loc (.getAbsolutePath (java.io.File. thumbs-path)))
  (throw (RuntimeException. "Missing PELL_THUMBS environment variable.")))

(def app "Server entrance point."
  (-> (handler/site all-routes)
      (wrap-base-url)))
