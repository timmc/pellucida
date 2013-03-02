(ns org.timmc.pellucida.pager
  "Pager module."
  (:require [net.cgrand.enlive-html :as e]
            [org.timmc.pellucida.link :as ln]))

(defn ^:internal paging-segment
  "Compute a paging sequence between `lo` and `hi` (exclusive).

The result is a sequence of numbers, possibly with a nil in the middle,
standing in for >= :min-elide page numbers."
  [lo hi {:keys [neighborhood min-elide]}]
  {:pre [(integer? lo), (integer? hi), (<= 0 neighborhood), (<= 1 min-elide)]}
  (if (>= lo hi) []
      (let [left-l (inc lo)
            left-r (+ lo neighborhood)
            right-l (- hi neighborhood)
            right-r (dec hi)
            gap (dec (- right-l left-r))]
        (if (< gap min-elide) ;; may even be negative
          (range left-l (inc right-r))
          (concat (range left-l (inc left-r))
                  [nil]
                  (range right-l (inc right-r)))))))

(defn anchorhood
  "Compute a paging sequence from first page `fst` through current page
`cur` to last page `lst` (inclusive).

The result is a sequence of numbers containing up to two nils, each
standing in for >= :min-elide page numbers.

:neighborhood controls the minimum number of pages on either side of the
three input page numbers (where available)."
  ;; TODO: Generalize!
  ([fst lst {:keys [neighborhood min-elide] :as opts}]
     {:pre [(<= fst lst), (<= 0 neighborhood), (<= 1 min-elide)]}
     (concat
      (when (not= fst lst)
        [fst])
      (paging-segment fst lst opts)
      (when (not= fst lst)
        [lst])))
  ([fst cur lst {:keys [neighborhood min-elide] :as opts}]
     {:pre [(<= fst cur lst), (<= 0 neighborhood), (<= 1 min-elide)]}
     (concat
      (when (not= fst cur)
        [fst])
      (paging-segment fst cur opts)
      [cur]
      (paging-segment cur lst opts)
      (when (not= lst cur)
        [lst]))))

(defn build-pager
  [pag linker]
  (let [pg (e/html-resource "org/timmc/pellucida/html/pager.html")
        [prev gonum elide current next oob empty]
        (map #(first (e/select pg [%]))
             [:.pgr-prev :.pgr-num :.pgr-elide :.pgr-current
              :.pgr-next :.pgr-oob :.pgr-empty])
        go-nodes #(for [goto %]
                    (cond (nil? goto)
                          elide

                          (= goto (:cur-page pag))
                          (e/at current [:span] (e/content (str goto)))

                          :else
                          (e/at gonum
                                [:a] (e/set-attr :href (linker goto))
                                [:a] (e/content (str goto)))))]
    (cond (:cur-valid pag)
          (let [pages (anchorhood (:first-page pag)
                                  (:cur-page pag)
                                  (:last-page pag)
                                  {:neighborhood 2, :min-elide 2})]
            (concat
             (when (< (:first-page pag) (:cur-page pag))
               [((e/set-attr :href (linker (dec (:cur-page pag))))
                 prev)])
             (go-nodes pages)
             (when (> (:last-page pag) (:cur-page pag))
               [((e/set-attr :href (linker (inc (:cur-page pag))))
                 next)])))

          (:has-records pag)
          (let [pages (anchorhood (:first-page pag)
                                  (:last-page pag)
                                  {:neighborhood 2, :min-elide 2})]
            (concat
             (go-nodes pages)
             [(e/at oob [:.pgr-this] (e/content (str (:cur-page pag))))]))

          :else
          empty)))
