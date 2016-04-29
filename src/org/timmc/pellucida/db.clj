(ns org.timmc.pellucida.db
  "Database helpers."
  (:refer-clojure :exclude (read))
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [cheshire.core :as json]
            [org.timmc.pellucida.settings :as settings]))

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

(def std-query-opts
  "Standard options for sql/query.

- Don't casefold column names"
  {:identifiers str})

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
  [db-con]
  (first
   (sql/query db-con ["SELECT * FROM metadata LIMIT 1"] std-query-opts)))

(defn check-db
  "Check DB validity status if we haven't checked recently."
  [db-con]
  (let [status @last-check
        now (System/currentTimeMillis)
        ;; Possibly update status (including our locally bound copy.)
        ;; Last check could be in future if clock has been
        ;; changed. That's cool.
        {:keys [errors]}
        (if (< (Math/abs (- (:ts status) now)) min-check-interval)
          status
          (reset! last-check (interpret-db-status (fetch-status db-con) now)))]
    (when-not (empty? errors)
      (throw (RuntimeException.
              (str "DB failed check: " (str/join "; " errors)))))))

;;;; --

(defn read ;; TODO make connection read-only
  "Query DB with SQL params (vector of paramaterized query +
parameters) and yield a realized result collection. The value of
#'last-check is usable after this function completes."
  [sql-params]
  (sql/with-db-connection
    [db-con {:classname "org.sqlite.JDBC"
             :subprotocol "sqlite"
             :subname (:gallery-db @settings/config)}]
    (check-db db-con)
    (sql/query db-con sql-params std-query-opts)))

(defn jdbc-psql
  "Format parameterized SQL + params for JDBC.

Input is a vector [sql-string, params-coll], output is [sql-string & params]."
  [psql]
  (let [[sql params] psql]
    (apply vector sql params)))
