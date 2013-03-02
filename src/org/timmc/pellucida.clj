(ns org.timmc.pellucida
  "PELL_CONFIG=path/to/config.clj lein ring server"
  (:require
   (compojure (route :as route)
              (handler :as handler)
              (response :as response)
              (core :refer (defroutes GET)))
   (org.timmc.pellucida (settings :as settings)
                        (listing :refer (listing-routes))
                        (single :refer (single-routes))
                        (thumbs :refer (thumb-routes))
                        (db :refer (*db-spec*)))))

(defn server-init! "Server initialization -- call before first request."
  []
  (if-let [cnf-path (System/getenv "PELL_CONFIG")]
    (binding [*read-eval* false]
      (settings/setup! (read-string (slurp cnf-path))))
    (throw (RuntimeException. "Missing PELL_CONFIG environment variable."))))

(defroutes all-routes
  (route/resources "/" {:root "public"})
  #'thumb-routes
  #'listing-routes
  #'single-routes)

(def app "Server entrance point."
  (handler/site all-routes))
