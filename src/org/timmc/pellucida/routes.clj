(ns org.timmc.pellucida.routes
  "Main entry for Pellucida (wrapped by launch.clj)."
  (:require
   ring.middleware.reload-modified
   (compojure (route :as route)
              (handler :as handler)
              (core :refer [defroutes]))
   (org.timmc.pellucida (settings :as settings))
   (org.timmc.pellucida.res (main :refer [main-routes])
                            (listing :refer [listing-routes])
                            (single :refer [single-routes])
                            (tags :refer [tags-routes])
                            (proxy-images :refer [proxy-image-routes])
                            (legacy-v1 :refer [legacy-v1-routes])
                            (stats :refer [stats-routes])
                            (acme :refer [acme-routes]))
   [ring.adapter.jetty :as jetty]))

(def reloadable-src-dirs
  ["src"])

(defn wrap-reload-modified
  "Runtime configurable wrapper for ring's wrap-reload-modified"
  [handler]
  ;; Don't even chain with the reloader middleware, just give it a
  ;; fake handler now and a fake request when we want to reload.
  ;; TODO: This is a terrible hack, maybe just replicate the reloading
  ;; logic myself later.
  (let [reloader (ring.middleware.reload-modified/wrap-reload-modified
                  identity
                  reloadable-src-dirs)]
    (fn wrap-reload-shim-inner [request]
      (when (settings/dev-mode?)
        (reloader nil))
      (handler request))))

(defroutes all-routes
  (route/resources "/" {:root "public"})
  #'main-routes
  #'proxy-image-routes
  #'listing-routes
  #'single-routes
  #'tags-routes
  #'legacy-v1-routes
  #'stats-routes
  #'acme-routes
  )

(def app "Server entrance point."
  (-> (handler/site all-routes)
      ;; various middlewares to go here
      (wrap-reload-modified)))

(defn start-server
  "Start server. Call .stop on return value to stop server."
  [port]
  (let [port (or port (@settings/config :port) 3000)]
    (println "Running pellucida on port" port)
    (jetty/run-jetty #'app {:port port
                            :join? false})))

(defn -main [& [port & _args]]
  (start-server (and port (Integer/parseInt port))))
