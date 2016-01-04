(ns org.timmc.pellucida.res.main
  "Main page. Maybe show a few photos."
  (:require
   [net.cgrand.enlive-html :as e]
   [compojure.core :refer [defroutes GET]]
   (org.timmc.pellucida (db :as db)
                        (layout :as lay)
                        (mode :as m)
                        (filter :as filter)
                        (link :as ln))
   [org.timmc.pellucida.res.listing :as listing]
   [org.timmc.handy :as handy]))

(defn pg [] (e/html-resource "org/timmc/pellucida/html/main.html"))

(defn photos-teaser
  "Produces a small listing of photos."
  [mode]
  (let [num 3
        pagination (handy/paging num 0 num)
        filters (filter/apply-mode nil mode)]
    (listing/recent-photos pagination filters)))

(defn main-page
  [mode]
  (lay/standard
   (pg)
   (e/transformation
    [:#teaser :.imglink]
    (e/clone-for [p (photos-teaser mode)]
                 (e/transformation
                  [:a] (e/set-attr :href (ln/single mode (:imageID p)))
                  [:img] (e/set-attr :src (ln/photo
                                           (:filenameInvariant p)
                                           (get-in @db/last-check
                                                   [:config "filenameVariants"])
                                           :thumb))))

    [:.go-to-gallery :a]
    (e/set-attr :href (ln/listing mode [] 0))

    [:.go-to-tags :a]
    (e/set-attr :href (ln/tags mode)))

   {:doc-title "Tim McCormack's photo gallery"
    :page-title "Tim McCormack's photo gallery"
    :mode mode}))

;; Main page does not have /v2 prefix.
(defroutes main-routes
  (GET "/" r
       (let [mode (m/from-request r)]
         (lay/render (main-page mode)))))
