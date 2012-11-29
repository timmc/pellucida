(ns org.timmc.pellucida.db
  "Database helpers."
  (:require [org.timmc.pellucida.settings :as settings]
            [clojure.java.jdbc :as sql]
            clojure.java.jdbc.internal))

(def ^:dynamic *db-spec*
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   ;; :subname "FILL IN"
   })

(defmacro read-db ;; TODO make connection read-only
  [& body]
  `(binding [clojure.java.jdbc.internal/*as-key* str]
     (sql/with-connection
       (assoc *db-spec* :subname (:gallery-db @settings/config))
       ~@body)))
