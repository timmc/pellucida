(ns org.timmc.pellucida.layout
  "Standard page layout and rendering."
  (:require [net.cgrand.enlive-html :as e]
            (org.timmc.pellucida (link :as ln))))

(def std (e/html-resource "org/timmc/pellucida/html/standard.html"))

(defn render "Render an enlive dom as an HTML string."
  [dom]
  (apply str (e/emit* dom)))

(defn standard
  "Given a page resource (with .std-* overrides), a body transformer, and
a set of simple values to insert, produce a standard layout node tree.

* `pg-resource` must be an Enlive resource
* `mode` is the page mode
* `body-xform` is expected to be an e/transformation or compatible
* `vals` is a map containing :doc-title, :page-title, :mode"
  [pg-resource body-xform vals]
  (let [xformed (body-xform pg-resource)] ;; TODO: Move application into caller
    (e/at std
      [:title] (e/content (:doc-title vals))
      [:head] (e/append (-> (e/select xformed [:head]) first e/unwrap))
      [:.std-home :a] (e/set-attr :href (ln/main (:mode vals)))
      [:.std-ptitle] (e/content (:page-title vals))
      ;; Replace the contents of .std-body in the standard template with
      ;; the contents of the (transformed) .std-body from the page template.
      [:.std-body] (e/substitute
                    (e/transform (e/select xformed [:.std-body])
                                 [:.std-body] (e/remove-attr :stitch))))))
