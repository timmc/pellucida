(ns org.timmc.pellucida.res.tags
  "A tag cloud page."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (filter :as filter)
                        (layout :as lay)
                        (mode :as m)
                        (link :as ln))))

;;;; data

(defn tags-counted
  "Yield table of :cat, :tag, :count."
  [mode]
  (let [filters (filter/apply-mode [] mode)
        [fsql fparams] (reduce filter/sql-wrap nil filters)
        sql (str "select cat, tag, count(imageID) as count "
                 "from imagetags "
                 (when fsql
                   (str "where imageID in ( " fsql  " ) "))
                 "group by cat, tag")
        params fparams]
    (db/read (db/jdbc-psql [sql params]))))

(defn compare-str-casefold
  "Compare two strings after casefolding."
  [s1 s2]
  (.compareTo (.toLowerCase s1) (.toLowerCase s2)))

(defn tags-by-cat
  "Given counted tags data from DB, yield seq of pairs of category and
text-sorted map of tag to count. [[category, {tag: count...}]...]."
  [ctag-rows]
  (sort-by first
           (for [[cat rows] (group-by :cat ctag-rows)]
             [cat (into (sorted-map-by compare-str-casefold)
                        (map (juxt :tag :count) rows))])))

;;;; html

(def pg (e/html-resource "org/timmc/pellucida/html/tags.html"))

(defn say-count
  [cnt]
  (str cnt " photo" (when-not (= 1 cnt) "s")))

(defn one-tag
  "Transformation for a .tgc-tag node using a category, tag, and count."
  [mode cat tag cnt]
  (let [filts [{:type :tt, :cat cat, :tag tag}]
        bracket (Math/max 0 (Math/min 7 (long (* 1.6 (Math/log cnt)))))]
    (e/transformation
     [:a.tgc-link] (e/do->
                    (e/content tag)
                    (e/set-attr :href (ln/listing mode filts 0))
                    (e/set-attr :title (say-count cnt))
                    (e/add-class (str "bracket-" bracket))))))

(defn one-cat
  "Transformation for a .tgc-cat node using a category name and a map
of tags to counts."
  [mode cat tags]
  (e/transformation
   [:h2] (e/content cat)
   [:.tgc-tag] (e/clone-for [[tag cnt] tags]
                            (one-tag mode cat tag cnt))))

(defn tags-page "Render a listing of recent photos."
  [mode]
  (let [bycat (tags-by-cat (tags-counted mode))]
    (lay/standard
     pg
     (e/transformation
      [:.tgc-bycat :.tgc-cat] (e/clone-for [[cat tags] bycat]
                                           (one-cat mode cat tags)))
     {:doc-title "Tag cloud"
      :page-title "Tags, weighted by usage"
      :mode mode})))

(defroutes tags-routes
  (GET "/v2/tags" r
       (let [mode (m/from-request r)]
         (lay/render (tags-page mode)))))
