(ns org.timmc.pellucida.settings)

(defn load-config
  []
  (if-let [cnf-path (or (System/getenv "PELL_CONFIG") "conf/production.clj")]
    (binding [*read-eval* false]
      (read-string (slurp cnf-path)))
    (throw (RuntimeException. "Missing PELL_CONFIG environment variable."))))

(defonce ^{:doc ":thumbs-proxy-base, :thumbs-link-base, :gallery-db"}
  config (load-config))
