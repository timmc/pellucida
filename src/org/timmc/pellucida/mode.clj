(ns org.timmc.pellucida.mode
  "Support for different viewing modes."
  (:require [org.timmc.pellucida.util :as u]))

(def modes
  "Map of shortcodes to maps of {:shortcode, :name, :desc, :filters}."
  (into {}
        (map (juxt :shortcode identity)
             [{:shortcode "gal"
               :name "gallery"
               :desc "images more suitable for default display"
               :filters [{:type :tt, :cat "Meta", :tag "gallery"}]}
              #_
              {:shortcode "TEST"
               :name "TESTING filter"
               :desc "Just red images"
               :filters [{:type :tt, :cat "Content", :tag "red"}]}
              {:shortcode "raw"
               :name "raw public"
               :desc (str "all public images, including near-duplicates,"
                          " low-quality images, and images included only"
                          " for aiding identification")
               :filters []}])))

(def default-key
  "Shortcode for default mode."
  "gal")

(defn from-request
  "Extract the mode object from the Ring request, or nil if not
present or invalid."
  [request]
  (or (get modes (get-in request [:params :mode]))
      (get modes default-key)))

(defn qsc
  "Given a mode object, produce a coll of encoded query-string
components."
  [mode]
  {:pre [(associative? mode)]}
  (when (and mode
             (not= (:shortcode mode) default-key))
    [(str "mode=" (u/enc-queryc (:shortcode mode)))]))
