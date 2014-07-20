(ns org.timmc.pellucida.tags
  "A tag cloud page."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

;;;; data

(defn tags-counted
  "Yield table of :cat, :tag, :count."
  []
  (let [sql "select cat, tag, count(imageID) as count
             from imagetags
             group by cat, tag"]
    (db/read
     (sql/with-query-results r
       (db/jdbc-psql [sql])
       (doall r)))))

(defn tags-by-cat
  "Given counted tags data from DB, yield seq of pairs of category and
text-sorted map of tag to count. [[category, {tag:count...}]...]."
  [ctag-rows]
  (sort-by first
           (for [[cat rows] (group-by :cat ctag-rows)]
             [cat (into (sorted-map) (map (juxt :tag :count) rows))])))

;;;; html

(defn pg [] (e/html-resource "org/timmc/pellucida/html/tags.html"))

(defn say-count
  [cnt]
  (str cnt " photo" (when-not (= 1 cnt) "s")))

(defn one-tag
  "Transformation for a .tgc-tag node using a category, tag, and count."
  [cat tag cnt]
  (let [filts [{:type :tt, :cat cat, :tag tag}]
        bracket (Math/max 0 (Math/min 7 (long (* 1.6 (Math/log cnt)))))]
    (e/transformation
     [:a.tgc-link] (e/do->
                    (e/content tag)
                    (e/set-attr :href (ln/listing filts 0))
                    (e/set-attr :title (say-count cnt))
                    (e/add-class (str "bracket-" bracket))))))

(defn one-cat
  "Transformation for a .tgc-cat node using a category name and a map
of tags to counts."
  [cat tags]
  (e/transformation
   [:h2] (e/content cat)
   [:.tgc-tag] (e/clone-for [[tag cnt] tags]
                            (one-tag cat tag cnt))))

(defn tags-page "Render a listing of recent photos."
  []
  (let [bycat (tags-by-cat (tags-counted))]
    (lay/standard
     (pg)
     (e/transformation
      [:.tgc-bycat :.tgc-cat] (e/clone-for [[cat tags] bycat]
                                           (one-cat cat tags)))
     {:doc-title "Tag cloud"
      :page-title "Tags, weighted by usage"})))

(defn maybe-param
  [request param parse default]
  (if-let [s (get-in request [:params param])]
    (parse s)
    default))

(defroutes tags-routes
  (GET "/tags" [:as r]
       (lay/render (tags-page))))
