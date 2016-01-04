(ns org.timmc.pellucida.db
  "Database helpers."
  (:refer-clojure :exclude (read))
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [cheshire.core :as json]
            [org.timmc.pellucida.settings :as settings]))

;;;; Connection spec

(defonce ^:dynamic *db-spec*
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   ;; :subname "FILL IN"
   })

;;;; Version checking

(def acceptable-gallery-versions
  #{2})

(defonce
  ^{:doc
    "Results of last check. Includes:

- :ts Long: timestamp of check (millis since UNIX epoch)
- :errors Coll|nil: Empty if DB is in a usable state
- :version Long: DB version
- :consistent Boolean: True iff DB declares itself consistent
- :config Data: Parsed configuration map from metadata table

Config data includes:

- filenameVariants: Map of image variant names to variant portions of
  image filenames."}
  last-check
  (atom {:ts 0
         :errors nil
         :version nil
         :consistent nil
         :config nil}))

(def min-check-interval
  "Minimum version check interval, in millis."
  (* 15 1000))

(defn interpret-db-status
  [db-meta now]
  (let [{:keys [version, consistent, config]} db-meta
        ok-ver (contains? acceptable-gallery-versions version)
        consistent? (= consistent 1)
        config-parsed (try (json/parse-string config)
                           (catch Exception e
                             (println "Cannot parse DB stored config:" e)
                             nil))
        errors (seq
                (concat (when-not ok-ver
                          [(str "Unexpected DB version: " version)])
                        (when-not consistent?
                          ["DB not in consistent state"])
                        (when-not config-parsed
                          ["Could not parse DB's stored config"])))]
    {:ts now, :errors errors,
     :version version, :consistent consistent?, :config config-parsed}))

(defn fetch-status
  []
  (sql/with-query-results r
    ["SELECT * FROM metadata LIMIT 1"]
    (first r)))

(defn check-db
  "Check DB validity status if we haven't checked recently."
  []
  (let [status @last-check
        now (System/currentTimeMillis)
        ;; Possibly update status (including our locally bound copy.)
        ;; Last check could be in future if clock has been
        ;; changed. That's cool.
        {:keys [errors]}
        (if (< (Math/abs (- (:ts status) now)) min-check-interval)
          status
          (reset! last-check (interpret-db-status (fetch-status) now)))]
    (when-not (empty? errors)
      (throw (RuntimeException.
              (str "DB failed check: " (str/join "; " errors)))))))

;;;; --

(defmacro read ;; TODO make connection read-only
  "Run body inside a dynamic extent with a SQL connection. The value
of #'last-check is usable from the body and after this macro is run."
  [& body]
  `(binding [sql/*as-key* str]
     (sql/with-connection
       (assoc *db-spec* :subname (:gallery-db @settings/config))
       (check-db)
       ~@body)))

(defn jdbc-psql
  "Format parameterized SQL + params for JDBC.

Input is a vector [sql-string, params-coll], output is [sql-string & params]."
  [psql]
  (let [[sql params] psql]
    (apply vector sql params)))
