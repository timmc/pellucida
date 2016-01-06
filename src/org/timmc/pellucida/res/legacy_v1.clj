(ns org.timmc.pellucida.res.legacy-v1
  "Legacy routes for version 1 of the gallery. Most of these form
redirects."
  (:require
   [clojure.java.jdbc :as sql]
   [compojure.core :refer [defroutes GET]]
   [net.cgrand.enlive-html :as e]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (mode :as m)
                        (filter :as filter)
                        (link :as ln)
                        (util :as u))))

(defn extract-filter-info
  "Extract a map of :filters and :page from a v1 filter page's
querystring."
  [^String qs]
  (loop [kvs (.split qs "&")
         filters []
         page 0]
    (if (empty? kvs)
      {:filters (distinct filters)
       :page page}
      (let [[k v] (map u/dec-queryc (.split (first kvs) "=" 2))]
        (cond
         (nil? v)
         (recur (rest kvs) filters page)

         (= k "page")
         (recur (rest kvs)
                filters
                (if (re-matches #"[0-9]+" v)
                  (Integer/parseInt v)
                  page))

         :else
         (let [[_ cat] (re-find #"^tag\[([a-zA-Z]+)\]\[\]$" k)]
           (if cat
             (recur (rest kvs)
                    (conj filters {:type :tt
                                   :cat cat
                                   :tag v})
                    page)
             (recur (rest kvs) filters page))))))))

(defn find-legacy-image
  "Given a supposed MD5 hash of an image, return the image ID or nil
if not found."
  [md5]
  (db/read
   (sql/with-query-results r
     ["SELECT imageID FROM image WHERE md5 = ? LIMIT 1" md5]
     (:imageID (first r)))))

(defn redirect
  "Return ring 302 redirect to URL."
  [url]
  {:status 302
   :headers {"Location" url
             "Content-Type" "text/plain; charset=UTF-8"}
   :body "Redirecting to v2 website URL..."})

(defroutes legacy-v1-routes
  (GET "/unfiltered" r
       (redirect (ln/listing (m/modes "raw") [] 0)))
  (GET "/filter/run" r
       (let [{:keys [filters page]} (extract-filter-info (:query-string r))]
         (redirect (ln/listing (m/modes "raw") [] 0
                               filters
                               page))))
  (GET ["/view/:md5"] [md5 :as r]
       (if-let [id (find-legacy-image md5)]
         (redirect (ln/single (m/modes "raw") id))
         {:status 404
          :headers {"Content-Type" "text/html; charset=UTF-8"}
          :body "TODO"}))
  (GET "/tags" r
       (redirect (ln/tags (m/modes "raw"))))
  (GET "/about/stats" r
       ;; TODO
       "TODO"))
