(ns org.timmc.pellucida.settings)

(def config ":thumbs-proxy-base, :thumbs-link-base, :gallery-db"
  (atom nil))

(defn setup!
  [cnf]
  (reset! config cnf))
