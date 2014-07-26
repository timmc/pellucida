(ns org.timmc.pellucida.link
  "Building links to various parts of the application."
  (:require (org.timmc.pellucida (settings :as settings)
                                 (filter :as filter))))

(defn ^:internal build-pq
  "Build URL string from a path and a coll of querystring component strings."
  [path qscs]
  (if (seq qscs)
    (apply str path "?" (interpose "&" qscs))
    path))

(defn main "Main page"
  []
  "/")

(defn listing "Photo listing with thumbnails."
  [filters page]
  (let [filters (map filter/qsc filters)
        page (when-not (zero? page) [(format "page=%d" page)])]
    (build-pq "/list" (concat filters page))))

(defn single "Single-image page."
  [id]
  {:pre [(integer? id)]}
  (format "/image/%d" id))

(defn photo "Photo file itself."
  [id which]
  {:pre [(integer? id), (#{:fullsize :solo :thumb} which)]}
  (format "%s%d.%s.jpg" (:thumbs-link-base @settings/config) id (name which)))

(defn tags "Tag cloud page"
  []
  "/tags")
