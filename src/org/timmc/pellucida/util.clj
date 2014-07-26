(ns org.timmc.pellucida.util
  "Utility catch-all ns.")

(defn always-coll
  "If input is not a collection (or nil), wrap it in one.
Useful for handing Ring query parameters, which are either strings or
collections of strings. (Really irritating.)"
  [x]
  (if (or (nil? x) (coll? x)) x [x]))

(defn enc-pathc
  "Encode a URI path component."
  [s]
  ;; TODO replace with johnny URL lib
  (.replace (java.net.URLEncoder/encode s) "+" "%20"))

(defn enc-queryc
  "Encode a URI query component."
  [s]
  ;; TODO replace with johnny URL lib
  (.replace (java.net.URLEncoder/encode s) "+" "%20"))

(defn dec-pathc
  "Decode a URI path component."
  [r]
  ;; TODO replace with johnny URL lib
  (java.net.URLDecoder/decode (.replace r "+" "%2B")))

(defn dec-queryc
  "Decode a URI query component."
  [r]
  ;; TODO replace with johnny URL lib
  ;; We allow \+ to decode as \space only in query components.
  (java.net.URLDecoder/decode r))
