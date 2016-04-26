(ns org.timmc.pellucida.launch
  "AOT-prevention dynamic loader for `org.timmc.pellucida.routes`.")

(defn -main
  "Chain to routes.clj"
  [& args]
  (let [main-ns 'org.timmc.pellucida.routes]
    (require main-ns)
    (apply (ns-resolve main-ns '-main) args)))
