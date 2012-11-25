(ns org.timmc.pellucida.thumbs
  "Routes for serving thumbnails."
  (:require (org.timmc.pellucida (settings :as settings))
            [compojure.core :refer (defroutes GET)]))

(defn find-image
  "Assumes id is a numeric string and size is an allowable format."
  [id size]
  (let [fn (format "%s.%s.jpg" id size)
        f (java.io.File. (:thumbs-base @settings/config) fn)]
    (when (.exists f) f)))

(defroutes thumb-routes
  (GET ["/image/:id/dl/:size"
        :id #"[0-9]+" :size #"fullsize|solo|thumb"]
       [id size]
       (find-image id size)))
