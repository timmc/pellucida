(ns org.timmc.pellucida.db
  "Database helpers."
  (:refer-clojure :exclude (read))
  (:require [org.timmc.pellucida.settings :as settings]
            [clojure.java.jdbc :as sql]))

(defonce ^:dynamic *db-spec*
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   ;; :subname "FILL IN"
   })

(defmacro read ;; TODO make connection read-only
  [& body]
  `(binding [sql/*as-key* str]
     (sql/with-connection
       (assoc *db-spec* :subname (:gallery-db settings/config))
       ~@body)))

(defn jdbc-psql
  "Format parameterized SQL + params for JDBC.

Input is a vector [sql-string, params-coll], output is [sql-string & params]."
  [psql]
  (let [[sql params] psql]
    (apply vector sql params)))
