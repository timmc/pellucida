(ns org.timmc.pellucida.layout
  "Standard page layotu and rendering."
  (:require [net.cgrand.enlive-html :as e]))

(defn std [] (e/html-resource "org/timmc/pellucida/html/standard.html"))

(defn render "Render an enlive dom as an HTML string."
  [dom]
  (apply str (e/emit* dom)))

(defn standard
  "Given a page resource (with .std-* overrides), a body transformer, and
a set of simple values to insert, produce a stanard layout node tree.

* `pg-resource` must be an Enlive resource
* `body-xform` is expected to be an e/transformation or compatible
* `vals` is a map containing :doc-title, :page-title"
  [pg-resource body-xform vals]
  (e/at (std)
        [:title] (e/content (:doc-title vals))
        [:head] (e/append (-> (e/select pg-resource [:head]) first e/unwrap))
        [:.std-ptitle] (e/content (:page-title vals))
        ;; Replace the contents of .std-body in the standard template with
        ;; the contents of the (transformed) .std-body from the page template.
        [:.std-body] (e/content
                      (body-xform (e/select pg-resource [:.std-body])))))