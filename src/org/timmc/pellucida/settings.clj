(ns org.timmc.pellucida.settings
  (:require [clojure.set :as set]))

(def keys-required
  {:thumbs-link-base
   {:doc "Base URL for photo links, including trailing slash."
    :validate string?}
   :thumbs-proxy-base
   {:doc "Base path for images on filesystem, or nil if not proxying.
Include trailing slash."
    :validate #(or (string? %) (nil? %))}
   :gallery-db
   {:doc "Valid path to a SQLite3 database containing the gallery data."
    :validate (every-pred string? #(.exists (java.io.File. %)))}})

(def keys-optional
  {:dev
   {:doc "Turn on for auto-reloading and any other dev features."
    :validate (partial instance? Boolean)}
   :port
   {:doc "Port to serve website on. May be overriden at command line."
    :validate (every-pred number? (complement neg?))}})

(def ^:internal known-keys
  (set/union (set (keys keys-required)) (set (keys keys-optional))))

(defn validate "Check config and return it (or throw)."
  [cnf]
  ;; check for required keys
  (doseq [[rk rv] keys-required]
    (if-let [[_ cv] (find cnf rk)]
      (when-not ((:validate rv) cv)
        (throw (RuntimeException.
                (format "ERROR: Bad value for config key %s.\nDoc: %s"
                        rk (:doc rv)))))
      (throw (RuntimeException.
              (format "ERROR: Missing required config key: %s\nDoc: %s"
                      rk (:doc rv))))))
  ;; validate optional keys
  (doseq [[ok ov] keys-optional]
    (when-let [[_ cv] (find cnf ok)]
      (when-not ((:validate ov) cv)
        (throw (RuntimeException.
                (format "ERROR: Bad value for config key %s.\nDoc: %s"
                        ok (:doc ov)))))))
  ;; check for unknown keys
  (when-let [remaining (not-empty (set/difference (set (keys cnf)) known-keys))]
    (println "WARN: Unexpected config keys:" remaining))
  cnf)

(defn load-config
  []
  (if-let [cnf-path (or (System/getenv "PELL_CONFIG") "conf/production.clj")]
    (binding [*read-eval* false]
      (validate (read-string (slurp cnf-path))))
    (throw (RuntimeException. "Missing PELL_CONFIG environment variable."))))

(defonce ^{:doc ":thumbs-proxy-base, :thumbs-link-base, :gallery-db"}
  config (load-config))
