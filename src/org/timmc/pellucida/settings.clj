(ns org.timmc.pellucida.settings)

(def config ":thumbs-base, :gallery-db"
  (atom nil))

(defn setup!
  [cnf]
  (reset! config cnf))
