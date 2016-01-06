(ns org.timmc.pellucida.link
  "Builds URLs to various parts of the application. URLs are absolute
paths unless otherwise indicated."
  (:require (org.timmc.pellucida (settings :as settings)
                                 (util :as u)
                                 (mode :as m)
                                 (filter :as filter))))

;; TODO This is terrible, should take unencoded params
(defn ^:internal build-pq
  "Build URL string from a path and a coll of encoded query component strings."
  [path qscs]
  (if (seq qscs)
    (apply str path "?" (interpose "&" qscs))
    path))

(defn abs
  "Turn absolute path reference into absolute URL, if configuration
supports it (return input otherwise.)"
  [path]
  (str (:base-url @settings/config) path))

(defn main "Main page"
  [mode]
  (build-pq "/"
            (m/qsc mode)))

(defn listing "Photo listing with thumbnails."
  [mode filters page]
  (let [filters (map filter/qsc filters)
        page (when-not (zero? page) [(format "page=%d" page)])]
    (build-pq "/v2/list"
              (concat (m/qsc mode)
                      filters
                      page))))

(defn single "Single-image page."
  [mode id]
  {:pre [(integer? id)]}
  (build-pq
   (format "/v2/image/%d" id)
   (m/qsc mode)))

(defn photo "Photo file itself."
  [base-name suffixes which]
  {:pre [(string? base-name)
         (map? suffixes)
         (#{:fullsize :solo :thumb} which)]}
  (format "%s%s-%s.jpg"
          (:thumbs-link-base @settings/config)
          base-name
          (get suffixes (name which))))

(defn tags "Tag cloud page"
  [mode]
  (build-pq "/v2/tags"
            (m/qsc mode)))
