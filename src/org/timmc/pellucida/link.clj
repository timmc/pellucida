(ns org.timmc.pellucida.link
  "Building links to various parts of the application."
  (:require (org.timmc.pellucida (settings :as settings))))

(defn photo
  [id which]
  {:pre [(#{:fullsize :solo :thumb} which)]}
  (format "%s%d.%s.jpg" (:thumbs-link-base @settings/config) id (name which)))
