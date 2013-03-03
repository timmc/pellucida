(ns org.timmc.pellucida.util
  "Utility catch-all ns.")

(defn always-coll
  "If input is not a collection (or nil), wrap it in one.
Useful for handing Ring query parameters, which are either strings or
collections of strings. (Really irritating.)"
  [x]
  (if (or (nil? x) (coll? x)) x [x]))
