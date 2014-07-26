(ns org.timmc.pellucida.db
  "Database helpers."
  (:refer-clojure :exclude (read))
  (:require [org.timmc.pellucida.settings :as settings]
            [clojure.java.jdbc :as sql]))

;;;; Connection spec

(defonce ^:dynamic *db-spec*
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   ;; :subname "FILL IN"
   })

;;;; Version checking

(def acceptable-gallery-versions
  #{1})

(def last-check
  "Results of last check."
  (atom {:ts 0 :valid true :version nil}))

(def min-check-interval
  "Minimum version check interval, in millis."
  (* 1 60 1000))

(defn check-version
  "Check version if we haven't checked recently."
  []
  (let [{:keys [ts valid version]} @last-check
        now (System/currentTimeMillis)]
    ;; Last check could be in future if clock has been changed. That's cool.
    (if (< min-check-interval (Math/abs (- ts now)))
      (let [cur-ver (sql/with-query-results r
                      ["SELECT version FROM metadata"]
                      (:version (first r)))]
        (if (contains? acceptable-gallery-versions cur-ver)
          (reset! last-check {:ts now :valid true :version cur-ver})
          (do (reset! last-check {:ts now :valid false :version cur-ver})
              (throw (RuntimeException.
                      (str "Unexpected DB version, halting: " cur-ver))))))
      (when-not valid
        (throw (RuntimeException.
                (str "Unexpected DB version on last check: " version)))))))

;;;; --

(defmacro read ;; TODO make connection read-only
  [& body]
  `(binding [sql/*as-key* str]
     (sql/with-connection
       (assoc *db-spec* :subname (:gallery-db @settings/config))
       (check-version)
       ~@body)))

(defn jdbc-psql
  "Format parameterized SQL + params for JDBC.

Input is a vector [sql-string, params-coll], output is [sql-string & params]."
  [psql]
  (let [[sql params] psql]
    (apply vector sql params)))
