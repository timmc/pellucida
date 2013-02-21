(defproject org.timmc/pellucida "1.0.0-SNAPSHOT"
  :description "Web gallery fed by kpawebgen, a KPhotoAlbum database munger."
  :url "https://github.com/timmc/pellucida"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.0"]
                 [org.xerial/sqlite-jdbc "3.6.16"]
                 [org.clojure/java.jdbc "0.1.1"]
                 [enlive "1.0.1"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler org.timmc.pellucida/app
         :init org.timmc.pellucida/server-init!
         :open-browser? false})
