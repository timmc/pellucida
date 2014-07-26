(ns org.timmc.pellucida
  "PELL_CONFIG=path/to/config.clj lein ring server-headless"
  (:require
   [ring.middleware.reload-modified :refer [wrap-reload-modified]]
   (compojure (route :as route)
              (handler :as handler)
              (core :refer [defroutes]))
   (org.timmc.pellucida (settings :as settings))
   (org.timmc.pellucida.res (main :refer [main-routes])
                            (listing :refer [listing-routes])
                            (single :refer [single-routes])
                            (tags :refer [tags-routes])
                            (proxy-images :refer [proxy-image-routes]))
   [ring.adapter.jetty :refer [run-jetty]]))

(defroutes all-routes
  (route/resources "/" {:root "public"})
  #'main-routes
  #'proxy-image-routes
  #'listing-routes
  #'single-routes
  #'tags-routes)

(defn dev-wrap
  [handler]
  (if (@settings/config :dev)
    (-> handler
        ;; dev-only middlewares
        (wrap-reload-modified ["src"]))
    handler))

(def app "Server entrance point."
  (-> (handler/site all-routes)
      ;; various middlewares to go here
      (dev-wrap)))

(defn start-server
  "Start server. Call .stop on return value to stop server."
  [port]
  (let [port (or port (@settings/config :port) 3000)]
    (println "Running pellucida on port" port)
    (run-jetty #'app {:port port
                      :join? false})))

(defn -main [& [port & args]]
  (start-server (and port (Integer/parseInt port))))
