(ns org.timmc.pellucida.routes
  "Main entry for Pellucida (wrapped by launch.clj)."
  (:require
   [compojure.core :refer [defroutes GET]]
   [ring.adapter.jetty :as jetty]))

(defroutes redirect-everything
  (GET "/*" r
    (let [path (:uri r)
          qs (:query-string r)
          urel (str path (when qs (str "?" qs)))]
      {:status 301
       :headers {"Location" (str "http://gallery.brainonfire.net" urel)
                 "Content-Type" "text/plain; charset=UTF-8"}
       :body "Site moved to gallery.brainonfire.net (this was a dev site)\n"})))

(defn start-server
  "Start server. Call .stop on return value to stop server."
  [port]
  (let [port (or port 3000)]
    (println "Running pellucida on port" port)
    (jetty/run-jetty redirect-everything {:port port
                                          :join? false})))

(defn -main [& [port & _args]]
  (start-server (and port (Integer/parseInt port))))
