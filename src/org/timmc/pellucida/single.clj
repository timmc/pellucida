(ns org.timmc.pellucida.single
  "Showing just one photo."
  (:require
   (hiccup (core :refer (h))
           (page :refer (html5)))
   [compojure.core :refer (defroutes GET)]
   (org.timmc.pellucida (db :refer (read-db))
                        (link :as ln))
   [clojure.java.jdbc :as sql]))

(defn photo-data
  [id]
  {:pre [(integer? id)]}
  (read-db
   (sql/with-query-results r
     ["SELECT * FROM image WHERE imageID = ?" id]
     (first r))))

(defn single-page "Render a page for a single photo."
  [id]
  {:pre [(integer? id)]}
  (if-let [data (photo-data id)]
    (html5
     [:html
      [:head
       [:title (h (:label data))]]
      [:body
       [:h1 (h (:label data))]
       [:a {:href (ln/photo id :fullsize)
            :title "View at original size"}
        [:img {:src (ln/photo id :solo)}]]
       [:dl
        [:dt "Date"] [:dd (h (:startDate data))]
        [:dt "Angle"] [:dd (h (:angle data))]
        [:dt "Dimensions"] [:dd (h (:width data)) " x " (h (:height data))]]
       [:p (h (:description data))]]])
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Image not found."}))

(defroutes single-routes
  (GET ["/image/:id", :id #"[0-9]+"]
       [id] (single-page (Long/parseLong id))))