(ns org.timmc.pellucida.res.listing
  "A tabular, filterable listing view showing thumbnails of many photos."
  (:require
   [org.timmc.handy :as handy]
   [net.cgrand.enlive-html :as e]
   [org.timmc.pellucida.enlive-utils :as eu]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (pager :as pager)
                        (mode :as m)
                        (filter :as filter)
                        (link :as ln)
                        (util :as u))))

;;;; data

(defn total-count
  [filters]
  (let [[fsql fparams] (reduce filter/sql-wrap nil filters)
        sql (str "select count(*) as cnt from "
                 (if fsql
                   (str "( " fsql " )")
                   "image"))
        params fparams]
    (-> (db/read (db/jdbc-psql [sql params]))
        first
        :cnt)))

(defn recent-photos
  [pag filters]
  (let [[fsql fparams] (reduce filter/sql-wrap nil filters)
        sql (str "select * from image "
                 (when fsql
                   (str " where imageID in ( " fsql " ) "))
                 (format " order by imageID desc limit %d offset %d"
                         (:per-page pag), (cast Long (:first-record pag))))
        params fparams]
    (db/read (db/jdbc-psql [sql params]))))

;;;; html

(def pg (e/html-resource "org/timmc/pellucida/html/listing.html"))

(defn ths-one "Transformation for a .ths-one node using a photo record."
  [mode p]
  (e/transformation
   ;; TODO: use URL formatter
   [:a.ths-goto] (e/set-attr :href (ln/single mode (:imageID p)))
   [:.ths-title] (e/content (u/image-title p))
   [:img.ths-solo] (e/set-attr :src (ln/photo
                                     (:basename p)
                                     (get-in @db/last-check
                                             [:config "sizeSuffixes"])
                                     :thumb))
   [:.ths-meta] (e/content (:added p))))

(defn filter-one "Transformation for a filter in the filterbox."
  [mode user-filters page f]
  (e/transformation
   [:.fbx-describe] (e/content (filter/nat-lang f))
   [:.fbx-remove] (e/set-attr :href
                              (ln/listing mode
                                          (remove #(= % f) user-filters)
                                          0))))

(def per-page 30)

(defn list-page "Render a listing of recent photos."
  [mode user-filters cur-page]
  (let [filters (filter/apply-mode user-filters mode)
        pag (handy/paging (total-count filters) cur-page per-page)
        photos (when (:cur-valid pag)
                 (recent-photos pag filters))
        pager-node (pager/build-pager pag #(ln/listing mode user-filters %))]
    (lay/standard
     pg
     (e/transformation
      [:head] (if (seq user-filters)
                (e/append (e/html [:meta {:name "robots",
                                          :content "noindex,nofollow"}]))
                eu/no-op)
      ;; Delete the existing-filters part if no user filters applied.
      [:.fbx-existing] (when (seq user-filters) identity)
      [:.fbx-existing :ul :li] (e/clone-for [f user-filters]
                                 (filter-one mode user-filters cur-page f))
      [:.fbx-tags-link] (e/set-attr :href (ln/tags mode))
      ;; TODO: Better out-of-bounds and no-results pages
      [:.ths-container :.ths-one] (e/clone-for [p photos]
                                               (ths-one mode p))
      [:.pgr-container] (e/content pager-node)
      [:.total-count] (e/content (str (total-count filters))))
     {:doc-title "Listing of photos"
      :page-title "Recent photos"
      :mode mode})))

(defn maybe-param
  [request param parse default]
  (if-let [s (get-in request [:params param])]
    (parse s) ;; TODO catch?
    default))

(defroutes listing-routes
  (GET "/v2/list" r
       (let [mode (m/from-request r)
             filters (filter/parse-request r)
             page (maybe-param r :page #(Integer/parseInt %) 0)]
         (lay/render (list-page mode filters page)))))

;; TODO:
;; - canonical link removing mode, filters
