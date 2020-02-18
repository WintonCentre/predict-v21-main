(ns predict3.results.curves
  (:require [clojure.string :as str]
            [rum.core :as rum]
            [predict3.results.util :refer [treatment-keys* treatment-map* hex-palette lookup fill treatment-fills
                                           without-stroke dashed-stroke]]
            [predict3.state.run-time :refer [results-cursor h-cache-cursor input-cursor ttt-cursor]]
            [predict3.results.common :refer [common-results-text population-text available-treatment? stacked-yearly-values result-scroll-height result-display-title]]
            [predict3.components.primitives :refer [filled-icon]]
            [predict3.components.button :refer [small-help-button h5-h10-picker]]
            [predict3.results.marshalling :refer [additional-benefit-map]]
            [predict3.mixins :refer [sizing-mixin]]
            [translations.config :refer [translation-profile]]
            [pubsub.feeds :refer [publish]]
            [svg.space :refer [space]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [svg.axis :refer [axisBottom axisTop axisLeft axisRight]]
            [svg.scales :refer [->Identity nice-linear i->o o->i in out ticks tick-format-specifier]]
            [svg.mixins :refer [patch-svg-attrs]]
            [goog.object :as gobj :refer [getValueByKeys]]
    #_[com.rpl.specter :refer [select transform VAL ALL MAP-VALS nthpath walker]]
            ))


(enable-console-print!)

(defstyle styles
  [".outer" {:fill   "none"
             :stroke "none"}]
  [".inner" {:fill             "#fff"
             :stroke           "#000"
             :stroke-opacity   0
             :stroke-width     0.5
             :stroke-dasharray "3, 4"}]
  [".annotation" {
                  :font-size "14pt"
                  }]
  [".arrow" {
             :stroke       "#000"
             :stroke-width "1.5px"
             }])

(defn transpose [m]
  "transpose a 2D data matrix"
  (apply mapv vector m))

(comment
  (transpose [[1 2] [3 4]])
  ;=> [[1 3] [2 4]]

  (transpose [[1 2 3] [4 5 6]])
  ;=> [[1 4] [2 5] [3 6]]
  )

(defn other-as-delta [data]
  (into []
        (map (fn [m]
               (assoc m :oth (- (- 100 (:oth m)) (+ (:surgery m) (:radio m) (:horm m) (:chemo m) (:tra m) (:bis m)))))
             data)))

(defn format-year-data [transposed]
  (into [] (for [t transposed]
             (into [] (map-indexed (fn [i v] {:x i :y v}) t)))))

(comment

  (map (comp #(- 100 %) :oth) oth-data)

  (other-as-delta data)
  )


(rum/defc plot [{:keys [X Y]} data N hd]

  "Data should look something like this (for 10 years, but we are plotting 15):
   Each row is a plot layer
  ([{:x 0, :y 100} {:x 1, :y 98.89556593176486} ... {:x 9, :y 64.83779488900586} {:x 10, :y 60.8297996952587}]
   [{:x 0, :y 100} {:x 1, :y 98.89556593176486} ... {:x 9, :y 64.83779488900586} {:x 10, :y 60.8297996952587}]
   [{:x 0, :y 100} {:x 1, :y 98.89556593176486} ... {:x 9, :y 64.83779488900586} {:x 10, :y 60.8297996952587}]
   [{:x 0, :y 100} {:x 1, :y 98.89556593176486} ... {:x 9, :y 64.83779488900586} {:x 10, :y 60.8297996952587}]
   [{:x 0, :y 100} {:x 1, :y 99.93906220645762} ... {:x 9, :y 98.75403990843078} {:x 10, :y 98.5298358866154}])"

  (let [point (fn [x y] (str (X x) " " (Y y)))
        coord (fn [m] (point (:x m) (:y m)))
        rev-data (reverse data)]

    [:g
     (map-indexed (fn [i d]
                    (when d
                      (let [tk (treatment-keys* (- (count data) i 1))
                            fill (cond
                                   (= tk :oth) "#fff"
                                   (and hd (= tk :bis)) (hex-palette :other-treatments)
                                   :else (hex-palette tk))]
                        [:polygon {:id             (str "plot-" (name tk))
                                   :key            (str "p" i)
                                   :fill           fill
                                   :fill-opacity   1
                                   :stroke         "#fff"
                                   :stroke-opacity 1
                                   :points         (str/join ", " [
                                                                   (str/join ", " (map #(coord (d %)) (range (inc N))))
                                                                   (str/join ", "
                                                                             [(point N 0)
                                                                              (point 0 0)]
                                                                             )])}])))
                  rev-data)
     (map-indexed (fn [i d]
                    (when d
                      [:polyline {:key    (str "l" i)
                                  :fill   "none"
                                  :stroke dashed-stroke :strokeDasharray "8,8" :strokeWidth 5 :strokeLinecap "round"
                                  :points (map #(coord (d %)) (range (inc N)))
                                  }]))
                  [(first rev-data)])
     ]

    )
  )


(rum/defc curves-container [{:keys [outer margin inner padding width height x y N delay horm cache x-title y-title]} data]
  (let [inner (if (nil? inner) {:width  (- (:width outer) (:left margin) (:right margin))
                                :height (- (:height outer) (:top margin) (:bottom margin))}
                               inner)
        width (if (nil? width) (- (:width inner) (:left padding) (:right padding)) width)
        height (if (nil? height) (- (:height inner) (:top padding) (:bottom padding)) height)
        x (if (nil? x) (->Identity [0 width] N) x)
        x-ticks (ticks x)                                   ;(if (nil? x-ticks) (ticks 0 width 10) x-ticks)
        y (if (nil? y) (->Identity [0 height] 10) y)
        y-ticks (ticks y)                                   ;(if (nil? y-ticks) (ticks 0 height 5) y-ticks)
        X (i->o x)
        Y (i->o y)
        ]

    [:div {:style {:margin      "0 auto"
                   :width       "100%"
                   :height      0
                   :padding-top "100%"
                   :position    "relative"
                   }}
     [:svg {:style    {:position "absolute"
                       :top      0
                       :left     0}
            :view-box (str " 0 0 " (:width outer) " " (:height outer))}

      [:g {:key       0
           :transform (str "translate(" (:left margin) ", " (:top margin) ")")}

       [:rect {:key        1
               :class-name (:outer styles)
               :width      (:width inner)
               :height     (:height inner)}]

       ;;
       ;; define the coordinate system
       ;;
       [:g {:key       2
            :transform (str "translate(" (:left padding) "," (:top padding) ")")}
        [:rect {:key        1
                :class-name (:inner styles)
                :width      width
                :height     height}]

        ;; axes on all edges
        [:g {:key       "bottom"
             ;:class-name ".xaxis"
             :transform (str "translate(0," (+ (first (out y)) 10) ")")}
         (axisBottom {:scale x :ticks x-ticks})]

        [:g {:key       "left"
             :transform (str "translate(" (- (first (out x)) 10) ",0)")}
         (axisLeft {:scale y :ticks y-ticks :format-specifier (str (tick-format-specifier y) "%")})]


        [:g {:key       "y-title"
             :transform (str "translate(-62 0) rotate(-90 " (X 0) " " (Y 0) ")")}
         [:text {:key        "note"
                 :class-name (:annotation styles)
                 :x          (X 1)
                 :y          (Y 0)}
          y-title                                           ;"Percentage of women surviving"
          ]]

        [:g {:key "x-title" :transform (str "translate(0 50)")}
         [:text {:key        "note"
                 :class-name (:annotation styles)
                 :x          (X 2.5)
                 :y          (Y 0)}
          x-title                                           ;"Years after surgery"
          ]]

        (if (and horm (= :ys5 delay))
          (let [[h10-data h5-data] (if (< (:y ((nth cache (:bis treatment-map*)) 15))
                                         (:y ((nth data (:bis treatment-map*)) 15)))
                                     [cache data]
                                     [data cache])]
            ; (println "h10-data" h10-data)
            ;(println "h5-data" h5-data)
            (let [data* [(nth h10-data (:bis treatment-map*)) ;hormone
                         (nth h5-data (:bis treatment-map*)) ;surgery
                         nil                                ;aggregate radio
                         nil                                ;aggregate chemo
                         nil                                ;aggregate tra
                         nil                                ;(nth data (:bis treatment-map*))     ;bis polygon is the sum of all treatments to bis
                         nil                                ;br unused
                         (nth data (:oth treatment-map*))
                         ]]
              (rum/with-key (plot {:X X :Y Y} data* N true) "plot")))
          (rum/with-key (plot {:X X :Y Y} data N false) "plot"))

        ; Add 5 year mark
        (when (= :ys5 delay)
          (let [margin-left 0.75]
            [:g {:key "mark5"}
             [:rect {:x       (X 0)
                     :y       (Y 100)
                     :width   (- (X 5) (X 0)) :height (- (Y 0) (Y 100))
                     :fill    "#fff"
                     :opacity 0.8}]
             [:text {:key        "already"
                     :class-name (:annotation styles)
                     :style      {:font-weight "bold"}
                     :x          (X margin-left)
                     :y          (Y 56)}
              "Already"]
             [:text {:key        "survived"
                     :style      {:font-weight "bold"}
                     :class-name (:annotation styles)
                     :x          (X margin-left)
                     :y          (Y 50)}
              "survived"]
             [:text {:key        "five"
                     :class-name (:annotation styles)
                     :style      {:font-weight "bold"}
                     :x          (X margin-left)
                     :y          (Y 44)}
              "5 years"]]))

        ; Add grid overlay
        (map-indexed (fn [k x_k] [:line {:key              (str "x" x_k)
                                         :x1               (X x_k)
                                         :x2               (X x_k)
                                         :y1               (Y 0)
                                         :y2               (Y 100)
                                         :stroke           "#fff"
                                         :stroke-opacity   0.5
                                         :stroke-width     1
                                         :stroke-dasharray (if (if (= N 15)
                                                                 (zero? (mod (inc k) 5))
                                                                 (odd? k)) "5 5" "2 10")
                                         }])
                     (range 1 N))

        (map-indexed (fn [k y_k] [:line {:key              (str "y" y_k)
                                         :x1               (X 0)
                                         :x2               (X N)
                                         :y1               (Y y_k)
                                         :y2               (Y y_k)
                                         :stroke           "#fff"
                                         :stroke-opacity   0.5
                                         :stroke-width     (if (odd? k) 1 1)
                                         :stroke-dasharray (if (odd? k) "5 5" "2 10")}])
                     (range 10 100 10))]]]]))


(rum/defc curves < rum/reactive
  [{:keys [ttt cum-data cum-cache N delay horm]}]
  (let [margin {:top 10 :right 10 :bottom 0 :left 0}
        padding {:top 20 :right 0 :bottom 60 :left 80}
        outer {:width 400 :height 400}
        ttt* (rum/react ttt-cursor)
        x-title [:curves/x-axis "Years after surgery"]
        y-title [:curves/y-axis "Percentage of women surviving"]
        ]

    [:div

     ; todo: hide this in production profile
     ; Move it to an SVG editor
     (when (= translation-profile :edit)
       [:div {:style {:font-size 16}}
        (ttt y-title)])

     (curves-container
       (assoc (space outer margin padding [0 N] 5 [0 100] 5 N)
         :delay delay
         :horm horm
         :cache cum-cache
         :x-title (ttt* x-title)
         :y-title (ttt* y-title))
       cum-data)

     ; A translatable title. Not used in production version.
     (when (= translation-profile :edit)
       [:div {:style {:width "100%" :text-align "center" :font-size 16}}
        (ttt x-title)])
     ]))

(rum/defc legend
  "render legend for data. If hd then show in h10 delayed mode"
  [ttt data hd]
  [:div {:width "100%"}
   [:div {:style {:border-top     (str "4px dashed " dashed-stroke)
                  :width          "50px"
                  :display        "inline-block"
                  :margin-top     "15px"
                  :vertical-align "top"}}]
   [:div {:style {:display     "inline-block"
                  :margin-left "10px"
                  :width       "calc(100% - 60px)"}}
    [:p (ttt [:curve/legend-1 "Survival rate excluding deaths from breast cancer. "])
     [:span.screen-only (small-help-button {:style   {:display "block"}
                                            :help-id "dashed"})]]]
   (if hd
     [:div
      [:p (filled-icon (hex-palette :horm)) (ttt [:curev/legend-2 " Additional benefit of 5 years further hormone therapy"])]
      [:p (filled-icon (hex-palette :surgery)) (ttt [:curve/legend-3 " Survival without further hormone therapy"])]]
     [:div
      (when (available-treatment? :bis)
        [:p (filled-icon (hex-palette :bis)) (ttt [:curve/legend-4 " Additional benefit of bisphosphonates"])])
      (when (and (not hd) (available-treatment? :tra))
        [:p (filled-icon (hex-palette :tra)) (ttt [:curve/legend-5 " Additional benefit of trastuzumab"])])
      (when (and (not hd) (available-treatment? :chemo))
        [:p (filled-icon (hex-palette :chemo)) (ttt [:curve/legend-6 " Additional benefit of chemotherapy"])])
      (when (and (not hd) (available-treatment? :radio))
        [:p (filled-icon (hex-palette :radio)) (ttt [:curve/legend-7] " Additional benefit of radiotherapy")])
      (when (available-treatment? :horm)
        [:p (filled-icon (hex-palette :horm)) (ttt [:curve/legend-8 " Additional benefit of hormone therapy"])])
      [:p (filled-icon (hex-palette :surgery)) (ttt [:curve/legend-9 " Surgery only"])]])

   ])


(rum/defc results-in-curves < rum/static rum/reactive      ;sizing-mixin
  [{:keys [ttt printable]}]
  (let [                                                    ;width (rum/react (:width state))
        ;side-by-side (or printable (nil? width) (> width 500))
        ;width 300                                           ;(rum/react (:width state))
        ;narrow (and (not printable) width (<= width 500))
        N 15                                                ;(rum/react (input-cursor :ten-fifteen))
        data (other-as-delta (mapv
                               #(additional-benefit-map {:annual-benefits (rum/react results-cursor)
                                                         :year            %
                                                         :tks             treatment-keys*})
                               (range (inc N))))


        delay (rum/react (input-cursor :delay))
        horm (rum/react (input-cursor :horm))
        hd (and horm (= :ys5 delay))

        ; this line loses treatment keys!
        cum-data (format-year-data (transpose (map #(reductions + (vals %)) data)))

        cum-cache (if hd (format-year-data
                           (transpose
                             (map #(reductions + (vals %))
                                  (other-as-delta (mapv
                                                    #(additional-benefit-map {:annual-benefits (rum/react h-cache-cursor)
                                                                              :year            %
                                                                              :tks             treatment-keys*})
                                                    (range (inc N)))))))
                         nil)]

    [:div {:style {:position "relative"}}

     (when-not printable
       [:p {:style {:margin-top "15px"}}
        (if hd
          (ttt [:results/curves-title-1-1 "This graph shows the percentage of women who survive from now given that they
        received surgery 5 years ago."])
          [:span
           (ttt [:results/curves-title-1 "This graph shows the percentage of women who survive "])
           (ttt [:results/curves-title-3 "over time after surgery. "])])
        ])

     [:.row
      [:.col-lg-7.col-md-8.col-sm-8.col-xs-10
       [:div {:style {:padding "15px 40px 0px 0px"}}
        (curves {:ttt       ttt
                 :cum-data  cum-data
                 :cum-cache cum-cache
                 :N         N
                 :delay     delay
                 :horm      horm})]]
      [:.col-lg-5.col-md-4.col-sm-4.col-xs-10
       {:style {:padding-top    "30px"
                :vertical-align "top"
                :display        "inline-block"}}
       (legend ttt data (and horm (= :ys5 delay)))]]]))
