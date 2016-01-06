(ns org.timmc.pellucida.settings
  (:require [clojure.set :as set]))

(def keys-required
  {:thumbs-link-base
   {:doc "Base URL for photo links, including trailing slash.
To use the filesystem proxy, use /proxy-image/."
    :validate string?}

   :gallery-db
   {:doc "Valid path to a SQLite3 database containing the gallery data."
    :validate #(and (string? %)
                    (.exists (java.io.File. %)))}})

(def keys-optional
  {:port
   {:doc "Port to serve website on. May be overriden at command line."
    :validate #(and (integer? %) (not (neg? %)))}

   :dev
   {:doc "Boolean: Turn on for auto-reloading and any other dev features."
    :validate #(instance? Boolean %)}

   :thumbs-proxy-base
   {:doc (str "Base path for images on filesystem, if proxying."
              " Include trailing slash.")
    :validate string?}

   :gmaps-api-key
   {:doc "Google Maps v2 API key"
    :validate string?}
   :btc-donate-addr
   {:doc "Bitcoin donation address"
    :validate string?}})

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
    (when-let [cv (get cnf ok)] ;; allow nil values
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

(defonce ^{:doc "Delay: :thumbs-proxy-base, :thumbs-link-base, :gallery-db"}
  config
  (delay (load-config)))
