(ns predict3.results.icons
  (:require [rum.core :as rum]
            [predict3.results.util :refer [treatment-keys* treatment-fills avoid-decimals benefit% benefits% hex-palette dp1 without-h10 h10-benefit] :as util]
            [predict3.results.marshalling :refer [additional-benefit-map additional-benefit-kvs]]


            [predict3.state.run-time :refer [input-cursor results-cursor year-selected h-cache-cursor rounded-cursor]]
            [predict3.results.common :refer [common-results-text stacked-yearly-values result-scroll-height population-text result-display-title press-and-hold]]
            [predict3.components.button :refer [year-picker h5-h10-picker]]
            [predict3.components.helpful-form-groups :refer [form-entry]]
            [predict3.components.primitives :refer [pic pic-dead open-icon filled-icon]]
            [pubsub.feeds :refer [publish]]
            ))

(def br-deaths-fill "#fcc")
(def oth-deaths-fill "#888")

(defn add-s [n]
  (if (= n 1) "" "s"))

(defn plural [n s p]
  (if (= n 1) s p))

(defn get-style
  ([keys index hd]
   (let [treatment-key (keys index)]
     (conj (cond
             (< index 6) {:color (hex-palette (if (and hd (= :bis treatment-key))
                                                :other-treatments
                                                treatment-key)) :filled true}
             (= index 6) {:color (hex-palette :br) :filled false}
             :else {:color (hex-palette :oth) :filled false})
           [:treatment-key treatment-key])))
  ([keys index] (get-style keys index false)))

(defn style-picker [treatment-keys data cum-counts n hd]
  (cond
    (< n (cum-counts 0)) (get-style treatment-keys 0 hd)
    (< n (cum-counts 1)) (get-style treatment-keys 1 hd)
    (< n (cum-counts 2)) (get-style treatment-keys 2 hd)
    (< n (cum-counts 3)) (get-style treatment-keys 3 hd)
    (< n (cum-counts 4)) (get-style treatment-keys 4 hd)
    (< n (cum-counts 5)) (get-style treatment-keys 5 hd)
    (< n (- 100 (second (nth data 7)))) (get-style treatment-keys 6 hd)
    :else (get-style treatment-keys 7 hd)))


(rum/defc placed-icons [data hd]
  (let [data (concat data (repeat 4 [:a 0]))
        cum-counts (into [] (reductions + (map second data)))]

    [:div
     (for [y (range 10)
           x (range 10)
           :let [n (+ x (* 10 y))
                 style (style-picker treatment-keys* data cum-counts n hd)
                 shape (if (:filled style) pic pic-dead)
                 treatment-key (:treatment-key style)]]

       (rum/with-key
         (shape {:treatment-key treatment-key :style (dissoc style :filled :treatment-key) :x x :y y}) n)
       )]

    ))

(defn deaths [ttt n]
  (plural n (ttt [:icons/death "death"]) (ttt [:icons/deaths " deaths"])))

(defn survivors [ttt n]
  (plural n (ttt [:icons/survivor "survivor"]) (ttt [:icons/survivors " survivors"])))

(defn extra [ttt] (ttt [:icons/extra " extra"]))

(defn minimise-abs-error
  "Minimise the absolute error by adjusting either rounded other or rounded breast-related deaths.
  oth - unrounded other deaths
  br - unrounded breast-related deaths
  excess - excess over 100 if we made no adjustment. We must subtract this amount from (+ (rounded oth) (rounded br))
  returns [adj-oth adj-br] - the amounts to add to (round oth) and (round br) respectively"
  [oth br excess]
  (let [se (if (pos? excess) 1 -1)]
    (let [abs-err (fn [n] (let [adj-oth n
                                adj-br (- excess n)
                                new-oth (+ adj-oth (Math/round oth))
                                new-br (+ adj-br (Math/round br))
                                ]
                            {:err (+ (Math/abs (- oth new-oth))
                                     (Math/abs (- br new-br)))
                             :oth adj-oth
                             :br  adj-br}))]
      (->> (range 0 (* se (inc (* se excess))) se)
           (map abs-err)
           (sort-by :err)
           (first)))))

(defn apply-adjustments
  "minimise the absolute error in rounded data by applying adjustments to oth and/or br values"
  [rounded-data adjustments]
  (if adjustments
    (let [[adj-oth adj-br] (map #(max 0 (+ (% adjustments) (% rounded-data))) [:oth :br])]
      (assoc rounded-data
        :oth adj-oth
        :br adj-br))
    rounded-data))

(rum/defc render-icons
  [ttt data hd rounded?]
  (let [oth (:oth data)
        br (:br data)
        excess (- 100 (apply + (map Math/round (vals data))))
        adjustments (if (zero? excess) nil (minimise-abs-error oth br excess))
        rounded-data (into {} (map (fn [[k v]] [k (Math/round v)]) data))
        adjusted (apply-adjustments rounded-data adjustments)
        legend-style {:font-size "16px" :margin-bottom 4}
        ]

    [:.row {:style {:clear "both"}}
     [:.col-md-6 {:style {:position       "relative"
                          :height         230
                          :top            -230
                          :pointer-events "none"
                          }}
      (placed-icons (apply-adjustments rounded-data adjustments) hd)]

     ; legend
     [:#legend.col-md-6 {:style {:padding-top 15 :padding-left 25}}
      (when (pos? oth)
        (let [s (if rounded? (:oth adjusted) (dp1 oth))]
          [:p.other {:style legend-style}
           (open-icon oth-deaths-fill) " "
           s " "
           (deaths ttt s)
           (ttt [:icons/other-causes " due to other causes"])]))

      (when (pos? br)
        (let [s (if rounded? (:br adjusted) (dp1 br))]
          [:p.bc {:style legend-style}
           (open-icon br-deaths-fill) " "
           s " "
           (deaths ttt br)
           " " (ttt [:icons/bc-related-1 "related to breast cancer"]) " "]))

      (when (pos? (:bis data))
        [:p.bis {:style legend-style} (filled-icon (hex-palette :bis)) " " (util/round (:bis data) rounded?) " "
         (extra ttt) " " (survivors ttt (Math/round (:bis data)))
         " " (ttt [:icons/due-to "due to"]) " "
         (if hd (ttt [:icons-already-1 "treatments already received"]) (ttt [:icons/bis "bisphosphonates"]))])

      (if hd
        [:div
         [:p.horm {:style legend-style} (filled-icon (hex-palette :horm)) " " (util/round (:horm data) rounded?) " "
          (extra ttt) " " (survivors ttt (Math/round (:horm data))) " " (ttt [:icons/due-to "due to"]) " "
          (when hd (ttt [:icons/fuerther "further "])) (ttt [:icons/horm "hormone therapy"])]
         (when (pos? (:surgery data))
           [:p.surgery {:style legend-style} (filled-icon (hex-palette :surgery)) " "
            (util/round (:surgery data) rounded?) " "
            (survivors ttt (Math/round (:surgery data))) " " (ttt [:icons/no-h10 "without further therapy"])])]
        [:div
         (when (pos? (:tra data))
           [:p.tra {:style legend-style} (filled-icon (hex-palette :tra)) " " (util/round (:tra data) rounded?) " "
            (extra ttt) " " (survivors ttt (Math/round (:tra data))) " " (ttt [:icons/tra "due to trastuzumab"])])
         (when (pos? (:chemo data))
           [:p.chemo {:style legend-style} (filled-icon (hex-palette :chemo)) " " (util/round (:chemo data) rounded?) " "
            (extra ttt) (survivors ttt (Math/round (:chemo data))) " " (ttt [:icons/chemo "due to chemotherapy"])])
         (when (pos? (:horm data))
           [:p.horm {:style legend-style} (filled-icon (hex-palette :horm)) " " (util/round (:horm data) rounded?) " "
            (extra ttt) " " (survivors ttt (Math/round (:horm data))) " " (ttt [:icons/due-to "due to"]) " "
            (when hd (ttt [:icons/fuerther "further "])) (ttt [:icons/horm "hormone therapy"])])
         [:p.surgery {:style legend-style} (filled-icon (hex-palette :surgery)) " "
          " " (util/round (:surgery data) rounded?) " " (survivors ttt (Math/round (:horm data))) " "
          (ttt [:icons/surgery-alone "with surgery alone"])]
         ])]]))

(rum/defc results-in-icons < rum/reactive
  [{:keys [ttt printable]}]
  (let [delay (rum/react (input-cursor :delay))
        horm (rum/react (input-cursor :horm))
        hd (and horm (= :ys5 delay))
        year (rum/react (year-selected))
        cache (rum/react h-cache-cursor)
        results (rum/react results-cursor)
        add-m (into {} (additional-benefit-kvs {:annual-benefits (rum/react results-cursor)
                                                :year            year
                                                :tks             treatment-keys*}))
        data (if hd
               (assoc (into {} add-m)
                 :surgery (* 100 (without-h10 cache year))  ;#_(apply + ((juxt :surgery :radio :chemo :tra :bis :horm) h5))
                 :horm (* 100 (h10-benefit results cache year))
                 :bis 0
                 :radio 0
                 :chemo 0
                 :tra 0)
               add-m)

        ; data is unrounded
        data (assoc data :br (- 100 (:oth data) (:tra data) (:chemo data) (:horm data) (:surgery data) (:bis data)))]

    [:div
     [:.row
      (when-not printable
        [:.col-sm-12 {:style {:margin-top "15px" :font-size 16}}
         (result-display-title ttt (ttt [:results/view-type "This display"])
                               year
                               delay
                               horm
                               [:results/display-title-number "number "])
         ])
      [:.col-sm-12 {:style {:margin-bottom "15px"}}
       (render-icons ttt data hd (rum/react rounded-cursor))
       (when-not printable (press-and-hold ttt))]]]))



