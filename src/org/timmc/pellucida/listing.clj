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
     [(format "SELECT *
               FROM image natural join image_meta
               order by imageID desc
               limit %d"
              limit)]
     (doall r))))

(defn lst-one "Render from a photo's data map."
  [p]
  [:div.lst-one
   [:a {:href (format "/image/%d" (:imageID p))} ;; TODO: use URL formatter
    (h (:label p))
    [:br]
    [:img {:src (format "/image/%d/dl/thumb" (:imageID p))}]]
   [:br]
   (h (:added p))])

(defn list-page "Render a listing of recent photos."
  []
  (html5
   [:html
    [:head
     [:title "Listing of photos"]
     [:link {:rel "stylesheet" :href "/css/listing.css"}]]
    [:body
     (for [p (recent-photos {})]
       (lst-one p))]]))

(defroutes listing-routes
  (GET "/" [] (list-page)))
