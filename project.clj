(defproject org.timmc/pellucida "1.0.0-SNAPSHOT"
  :description "Web gallery fed by kpawebgen, a KPhotoAlbum database munger."
  :url "https://github.com/timmc/pellucida"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main org.timmc.pellucida.launch
  :aot [org.timmc.pellucida.launch] ;; Sacrificial AOT stub
  :repl-options {:init-ns org.timmc.pellucida.routes} ;; Real main
  :min-lein-version "2"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.timmc/handy "1.7.1"]
                 [compojure "1.5.2"]
                 [ring/ring-jetty-adapter "1.5.1"]
                 [ring-reload-modified "0.1.1"]
                 [org.xerial/sqlite-jdbc "3.15.1"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [enlive "1.1.6"]
                 [cheshire "5.6.3"]
                 ]
  :jvm-opts ["-Xmx60m"]
  )
