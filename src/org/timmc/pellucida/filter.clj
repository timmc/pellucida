(ns org.timmc.pellucida.filter
  "Filtering operations."
  (:require
   (org.timmc.pellucida (util :as u))
   [clojure.set :as set]))

;; TODO: Enforce max number of filters?

;; Filters:
;; * Transitive tag match: {:type :tt, :cat "Content", :tag "color"}

;;;; Parsing

(defn parse-request
  "Produce a collection of filters from a Ring request. Each filter
takes the form of a map of:

- :type, one of: #{:tt}
- :cat, a category name string
- :tag, a tag name string"
  [r]
  (filter (complement nil?)
          (for [raw-tt (u/always-coll (get-in r [:query-params "tt"]))
                :let [[cat tag] (.split raw-tt "=" 2)]]
            (if (not tag)
              (throw (RuntimeException. (str "Invalid tt filter: " raw-tt)))
              {:type :tt, :cat cat, :tag tag}))))

(defn apply-mode
  "Given a filter collection and a mode, produce the effective filter
collection."
  [filters mode]
  (set/union (set filters)
             (set (:filters mode))))

;;;; URL building

(defmulti qsc "Produce querystring component for a filter" :type)
(defmethod qsc :tt [f]
  (str "tt=" (u/enc-queryc (:cat f)) "=" (u/enc-queryc (:tag f))))

;;;; Descriptions

(defmulti nat-lang "Natural language description of filter" :type)
(defmethod nat-lang :tt [f]
  (format "Tagged with \"%s: %s\" or a subtag"
          (:cat f) (:tag f)))

;;;; SQL emitting

(defmulti sql-wrap
  "Narrow a SQL query for image IDs based on a filter. `psql` is a
valid input to db/jdbc-psql or nil to form the base of a query.
The return value is a valid input to db/jdbc-psql."
  (fn [psql flt] (:type flt)))

(defmethod sql-wrap :tt [[isql iparams :as psql] {:keys [cat tag]}]
  (let [osql "select imageID
from image natural join imagetags
where cat = ? and tag = ?"
        oparams [cat tag]]
    (if psql
      [(str osql " and imageID in ( " isql " )")
       (concat oparams iparams)]
      [osql oparams])))
