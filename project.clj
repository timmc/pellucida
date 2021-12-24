(defproject org.timmc/pellucida "1.0.0-SNAPSHOT"
  :description "Web gallery fed by kpawebgen, a KPhotoAlbum database munger."
  :url "https://github.com/timmc/pellucida"
  :license {:name "Eclipse Public License"
            :url "https://www.eclipse.org/legal/epl-v10.html"}
  :main org.timmc.pellucida.launch
  :aot [org.timmc.pellucida.launch] ;; Sacrificial AOT stub
  :repl-options {:init-ns org.timmc.pellucida.routes} ;; Real main
  :min-lein-version "2"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.timmc/handy "1.7.2"]
                 [compojure "1.6.2"]
                 [ring/ring-jetty-adapter "1.9.4"]
                 ;; wrap-reload
                 [ring/ring-devel "1.9.4"]
                 [org.xerial/sqlite-jdbc "3.36.0.3"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [enlive "1.1.6"]
                 [cheshire "5.10.1"]
                 ]
  :jvm-opts ["-Xmx60m"]
  )
