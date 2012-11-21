(ns org.timmc.pellucida.db
  "Database helpers."
  (:require [clojure.java.jdbc :as sql]
            clojure.java.jdbc.internal))

(def ^:dynamic *db-spec*
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   ;; FIXME: take from environment
   :subname "/home/timmc/photos/web/kpawebgen.db3"})

(defmacro read-db ;; TODO make connection read-only
  [& body]
  `(binding [clojure.java.jdbc.internal/*as-key* str]
     (sql/with-connection *db-spec* ~@body)))
