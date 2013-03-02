(ns org.timmc.pellucida.thumbs
  "Routes for serving thumbnails."
  (:require (org.timmc.pellucida (settings :as settings))
            [compojure.core :refer (defroutes GET)]))

(defn find-image
  "Assumes id is a numeric string and size is an allowable format."
  [filename]
  (when (re-matches #"[0-9]+\.(fullsize|solo|thumb)\.jpg" filename)
    (let [f (java.io.File. (:thumbs-proxy-base settings/config) filename)]
      (when (.exists f) f))))

(defroutes thumb-routes
  (GET ["/thumbs/:filename"]
       [filename]
       (find-image filename)))
