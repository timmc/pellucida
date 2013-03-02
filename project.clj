(defproject org.timmc/pellucida "1.0.0-SNAPSHOT"
  :description "Web gallery fed by kpawebgen, a KPhotoAlbum database munger."
  :url "https://github.com/timmc/pellucida"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot org.timmc.pellucida
  :min-lein-version "2"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.timmc/handy "1.4.0"]
                 [compojure "1.1.3"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [ring-reload-modified "0.1.1"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [enlive "1.1.1"]]
  :plugins [[org.timmc/lein-otf "2.0.0"]])
