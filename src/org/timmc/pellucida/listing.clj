(ns org.timmc.pellucida.listing
  "A tabular, filterable listing view showing thumbnails of many photos."
  (:require
   (hiccup (core :refer (h))
           (page :refer (html5)))
   [compojure.core :refer (defroutes GET)]
   [org.timmc.pellucida.db :refer (read-db)]
   [clojure.java.jdbc :as sql]))

(defn recent-photos
  [{:keys [limit filters] :or {limit 30, filters []}}]
  {:pre [(integer? limit)]}
  (read-db
   (sql/with-query-results r
     [(format "SELECT * FROM image order by added desc limit %d" limit)]
     r)))

(defn list-page "Render a listing of recent photos."
  []
  (html5
   [:html
    [:head [:title "Listing of photos"]]
    [:body
     (interpose
      " "
      (for [p (recent-photos {})]
        [:a {:href (format "/image/%d" (:imageID p))}
         [:img {:src (format "%s.thumb.jpg" (:imageID p))}]
         (h (:label p))]))]]))

(defroutes listing-routes
  (GET "/list" [] (list-page)))
