(ns org.timmc.pellucida.link
  "Building links to various parts of the application."
  (:require (org.timmc.pellucida (settings :as settings))))

(defn listing "Photo listing with thumbnails."
  [filters page]
  (if (seq filters)
    "/todo"
    (if (zero? page)
      "/list"
      (format "/list?page=%d" page))))

(defn single "Single-image page."
  [id]
  {:pre [(integer? id)]}
  (format "/image/%d" id))

(defn photo "Photo file itself."
  [id which]
  {:pre [(integer? id), (#{:fullsize :solo :thumb} which)]}
  (format "%s%d.%s.jpg" (:thumbs-link-base settings/config) id (name which)))
