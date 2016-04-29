(ns org.timmc.pellucida.enlive-utils
  "Helpers for enlive HTML work."
  (:require [net.cgrand.enlive-html :as e]))

(def no-op
  "Transformation that does nothing."
  (e/transformation))

(def delete
  "Transformation that deletes the selection."
  (e/substitute []))
