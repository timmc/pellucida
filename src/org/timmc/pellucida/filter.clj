(ns org.timmc.pellucida.filter
  "Filtering operations."
  (:require
   (org.timmc.pellucida (util :as util))
   [clojure.java.jdbc :as sql])
  (:import (java.net URLEncoder)))

;; TODO: Enforce max number of filters?

;; Filters:
;; * Transitive tag match: {:type :tt, :cat "Content", :tag "color"}

;;;; Parsing

(defn parse-request
  "Produce a collection of filters from a Ring request."
  [r]
  (filter (complement nil?)
          (for [raw-tt (util/always-coll (get-in r [:query-params "tt"]))
                :let [[cat tag] (.split raw-tt "=" 2)]]
            (if (not tag)
              (throw (RuntimeException. (str "Invalid tt filter: " raw-tt)))
              {:type :tt, :cat cat, :tag tag}))))

;;;; URL building

(defmulti qsc "Produce querystring component for a filter" :type)
(defmethod qsc :tt [f]
  (str "tt=" (URLEncoder/encode (:cat f)) "=" (URLEncoder/encode (:tag f))))

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
