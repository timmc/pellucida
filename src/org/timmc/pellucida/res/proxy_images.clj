(ns org.timmc.pellucida.res.proxy-images
  "Routes for proxying images from the filesystem in dev mode."
  (:require (org.timmc.pellucida (settings :as settings))
            [compojure.core :refer [defroutes GET]]))

(defn ^:internal find-image
  "Assumes id is a numeric string and size is an allowable format."
  [filename]
  (when (re-matches #"id_[0-9a-z_\-]{0,200}\.jpg" filename)
    (let [f (java.io.File. (:thumbs-proxy-base @settings/config) filename)]
      (when (.exists f) f))))

(defroutes proxy-image-routes
  (GET ["/v2/proxy-image/:filename"]
       [filename]
       (find-image filename)))
