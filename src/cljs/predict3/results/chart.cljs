(ns predict3.results.chart
  (:require [rum.core :as rum]
            [predict3.mixins :refer [sizing-mixin]]
            [predict3.results.util :refer [treatment-keys* treatment-map*
                                           avoid-decimals min-label-percent
                                           fill data-fill fill-data-url hex-palette data-palette
                                           dashed-stroke
                                           h10-benefit without-h10] :as util]
            [predict3.results.common :refer [available-treatment? common-results-text population-text result-display-title press-and-hold]]
            [predict3.state.run-time :refer [model input-cursor input-label
                                             enabled-treatments results-cursor on-screen-treatments-cursor
                                             rounded-cursor h-cache-cursor]]
            [predict3.results.marshalling :refer [additional-benefit-kvs]]

            [predict3.components.primitives :refer [filled-icon]]
            [predict3.components.button :refer [year-picker small-help-button h5-h10-picker]]
            [pubsub.feeds :refer [publish]]
            [clojure.string :refer [join]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [cljs.pprint :refer [pp]]))



(defn border [fill] (str "1px solid " fill))
(defn arrow [fill] (str "2ex solid " fill))
(def arrow-color [220 150 0])
(def arrow-fill (arrow (str "rgb(" (join "," arrow-color) ")")))

(def z-front 20)
(def z-back 1)

(defstyle stacked-bar-chart-style
  [".chart" {:page-break-before "always"}
   [".chart-wrapper" {:background-color "white" :color "black" :position "relative" :margin-top "0ex"}]

   [".stacked-bar" {:position "absolute" :bottom "8ex" :top "2ex" :left "16%"
                    :right    "16%" :background-color "whitesmoke" :color "black"}

    [".h-tick" {:position "absolute" :width "100%" :height "0%"}
     [".h-label" {:position "absolute" :top 0 :color "#888" :left "-3em" :width "calc(100% + 6em)"}
      [".left" {:position "absolute" :left "-0.5em" :width "3em" :text-align "right" :top "-1ex"}]
      [".right" {:position "absolute" :right "-0.5em" :width "3em" :text-align "left" :top "-1ex"}]]
     [".line" {:border-bottom "2px solid #CCC"}]]

    [".bar" {:position "absolute" :background-color "white" :border "1px solid #CCC" :border-bottom "none"}
     [".bar-label" {:position   "absolute" :color "black" :left "0%" :width "100%"
                    :text-align "center" :font-size "1.2em"}]

     [".bar-item" {:position           "absolute" :width "100%" :left 0
                   :transition         "height 300ms, bottom 300ms, opacity 3000ms" :transition-timing-function "ease-out"
                   :-webkit-transition "height 300ms, bottom 300ms, opacity 300ms" :-webkit-transition-timing-function "ease-out"
                   :-moz-transition    "height 300ms, bottom 300ms, opacity 300ms" :-moz-transition-timing-function "ease-out"}
      [".bar-item-label" {:position "absolute" :width "100%" :text-align "center"
                          :bottom   "1.37ex" :border "1px none red"}]]]

    [".callout" {:position   "absolute"
                 :transition "height 300ms, bottom 300ms" :transition-timing-function "ease-out"}
     [".box" {:width   "7em" :height "10ex" :position "absolute" :bottom "-4.7ex"
              :padding "0.5ex 1ex 0.3ex 0.5ex" :text-align "right" :color "white" :border-radius "0.5ex"}
      [".total" {:position "absolute" :left "0.6ex" :bottom "1.3ex" :color "white" :font-size "1.2em"}]]
     #_[".box" {:width   "7em" :height "6.65ex" :position "absolute" :bottom "-3.25ex"
                :padding "0.5ex 1ex 0.3ex 0.5ex" :text-align "right" :color "white" :border-radius "0.5ex"}
        [".total" {:position "absolute" :left "0.6ex" :bottom "1.3ex" :color "white" :font-size "1.2em"}]]
     [".arrow" {:position   "absolute" :bottom "-1ex" :width 0 :height 0
                :border-top "1ex solid transparent" :border-bottom "1ex solid transparent"}]]]])

(rum/defc <-n%-text-> < rum/reactive
                        "Left or Right callout"
  [{:keys [->? percent text fill]
    :or   {->? true percent 50 text "half" fill "red"}}]

  [:.callout {:style {:left (if ->? 0 nil) :right (if ->? nil 0) :bottom (str percent "%")}}
   [:img.box {:src         (apply fill-data-url arrow-color) ;(data-fill 4 3)
              :alt         ""
              :aria-hidden true
              :style       {:padding    0
                            :text-align "center"
                            :left       (if ->? "-15.5ex" nil)
                            :right      (if ->? nil "-15.5ex")}}]

   [:.box {:style {:text-align "center"
                   :left       (if ->? "-15.5ex" nil)
                   :right      (if ->? nil "-15.5ex")}}

    [:span {:style {:font-size "1.2em"}} (str (if (rum/react rounded-cursor)
                                                (avoid-decimals (js/Number percent))
                                                (util/round (js/Number percent) false)) "% ")] text]

   [:.arrow {:style {:border-left  (if ->? arrow-fill nil)
                     :border-right (if ->? nil arrow-fill)
                     :left         (if ->? nil 0)
                     :right        (if ->? 0 nil)}}]])



(rum/defc n%-text->
  "Left callout pointing right"
  [props fill]
  (<-n%-text-> (assoc props :->? true :fill fill)))


(rum/defc <-n%-text
  "Right callout pointing left"
  [props fill]
  (<-n%-text-> (assoc props :->? false :fill fill)))


(rum/defc h-tick-line [h]
  [:.h-tick {:style {:position "absolute" :bottom h}}
   [:.line {:key 1}]
   [:.h-label {:key 2}
    [:.left {:key 1} h]
    [:.right {:key 2} h]]])


(rum/defc bar-item-label
  "A white label for a dark background which also prints in white.
  SVG is about the only way to force printers to emit text in white."
  [height]
  [:.bar-item-label {:style {:width "100%" :height "50%"}}
   [:svg {:height 30 :width "100%"}
    [:text {:x "50%" :y 13 :fill "#ffffff" :text-anchor "middle"}
     (str height "%")]]
   ])


(rum/defcs bar-item < rum/static
                      "A stacked bar item"
  [state {:keys [key bottom height fill background-url label callout-text ?above treatment-key rounded?]
          :or   {key          1 bottom 0 height 0 fill "red" background-url ""
                 callout-text "Label here" ?above true}}]

  (when fill
    [:div {:key key}
     [:img.bar-item {:src         background-url
                     :alt         ""
                     :aria-hidden true
                     :style       {:height     (str height "%")
                                   :border-top "#fff 1px solid"
                                   :bottom     (str bottom "%")}}]

     [:.bar-item {:key   1
                  ;:tab-index 0
                  :style {:background-color fill
                          :border-top       "#fff 1px solid"
                          :height           (str height "%")
                          :bottom           (str bottom "%")
                          }}

      ; todo: Recheck the types in following code - they look a little sloppy

      ;; internal value label
      (let [height (if rounded?
                     (avoid-decimals (js/parseFloat height))
                     (util/round (js/parseFloat height) false))] ; (js/parseInt height)

        (when (>= height min-label-percent)
          (bar-item-label height)))]]))

(rum/defc bar-label [{:keys [key text top?]}]
  "A label centred above or below the bar"
  [:.bar-label {:key   key
                :style (if top? {:top "-3.5ex"} {:bottom "-3.5ex"})} text])

(defn get-fill
  [hd negative? tk]
  (if negative?
    :invalid
    (if (and hd (= :bis tk))
      :other-treatments
      tk)))

(rum/defc bar < rum/static
                "Render a bar and its callouts.
                Callouts are currently specific to a treatments view rather than a cause of death view."
  [{:keys [key left right width label-under dataset negative? sums oth callout rounded? hd]
    :as   params}]

  (let [n (count dataset)
        inline-style (merge {:height "100%"}
                            {:left left :right right :width width})]
    [:.bar {:key key :style inline-style}
     #_(bar-label {:key 2 :text label-under :top? false})
     (map-indexed (fn [index [tk v]]
                    (when (pos? v)
                      (rum/with-key
                        (bar-item {:bottom         (sums index)
                                   :height         v
                                   :fill           (hex-palette (get-fill hd negative? tk))
                                   :background-url (data-palette (if negative? :invalid tk))
                                   :treatment-key  tk
                                   :callout        callout
                                   :?above         (nil? right)
                                   :rounded?       rounded?})
                        (+ index 4))))
                  dataset)

     [:div {:style {:position       "absolute"
                    :top            (str "calc(" oth "% - 2px)")
                    :bottom         0
                    :left           "-5px"
                    :right          "-5px"
                    :z-index        10
                    :pointer-events "none"
                    :border-top     "4px dashed #FA0"}}]
     (when callout (rum/with-key (callout (fill (dec n))) 3))]))



(rum/defc inner-stacked-bar
  "a single left or right stacked bar with callout left or right at top"
  < rum/static
  [{:keys [ttt dataset negative? sums style title subtitle-under right? year printable rounded? hd] :as chart-props}]

  [:div

   [:.stacked-bar {:key 1 :style style}
    (when title
      [:div
       [:h4 {:key "t2" :style {:position "absolute" :top "-4.2ex" :width "100%" :text-align "center"}}
        title]])

    (map-indexed #(rum/with-key (h-tick-line (str %2 "%")) (str "tick" %1)) (range 0 110 10))

    (let [data dataset
          callout (if right? <-n%-text n%-text->)]

      (do
        ; remove :br and :oth fields for bar plot
        (let [plot-data (-> data (butlast) (butlast))]
          (rum/with-key
            (bar {:label-under year
                  :dataset     plot-data
                  :negative?   negative?
                  :sums        sums
                  ; pass :oth field separately
                  :oth         (-> data (last) (second))
                  :left        "53%"
                  :width       "40%"
                  :total       (reduce + (mapv second data))
                  :callout     (partial callout {:percent (reduce + (mapv second plot-data))
                                                 :text    [:span (ttt [:chart/callout-1 "survive at least "])
                                                           year (ttt [:chart/callout-2 " years"])]})
                  :rounded?    rounded?
                  :hd          hd})
            2))))

    (when-not printable
      [:div {:key 3 :style {:position "absolute" :bottom "-6ex" :width "100%" :text-align "center" :font-size "16px"}}
       subtitle-under])]])

(rum/defc add-benefit < rum/reactive
  [ttt benefit treatment-key treatment]
  (let [bene (avoid-decimals (benefit treatment-key))]
    [:span (ttt [:chart/add-bene-1 " Additional benefit of "]) treatment
     (ttt [:chart/add-bene-2 " is "]) bene
     (ttt [:chart/add-bene-3 "% at "]) (rum/react (input-cursor :result-year))
     (ttt [:chart/add-bene-4 " years."])]))

(rum/defc add-benefit** < rum/reactive
  [ttt benefit treatment-key treatment]
  (let [rounded? (rum/react rounded-cursor)
        year (rum/react (input-cursor :result-year))
        bene (if rounded?
               (avoid-decimals (benefit treatment-key))
               (util/round (benefit treatment-key) false))]
    [:span (ttt [:chart/add-bene-1 " Additional benefit of "]) treatment
     (ttt [:chart/add-bene-2 " is "]) bene
     (ttt [:chart/add-bene-3 "% at "]) year
     (ttt [:chart/add-bene-4 " years."])]))

(rum/defcs stacked-bar < rum/reactive
  [state
   {:keys [ttt width h-over-w font-scale chart-style printable]
    :or   {width      100
           h-over-w   1
           font-scale 1}
    :as   props}]

  (let [side-by-side true
        year (rum/react (input-cursor :result-year))
        rounded? (rum/react rounded-cursor)


        ; bit of a hack - this assumes radiotherapy is the only possible negative (It is currently)
        delay (rum/react (input-cursor :delay))
        horm (rum/react (input-cursor :horm))
        hd (and horm (= :ys5 delay))
        results (rum/react results-cursor)
        cache (rum/react h-cache-cursor)

        additionals (additional-benefit-kvs {:annual-benefits results
                                             :year            year
                                             :tks             treatment-keys*})

        dataset (if hd
                  {:surgery (* 100 (without-h10 cache year)) ;#_(apply + ((juxt :surgery :radio :chemo :tra :bis :horm) h5))
                   :horm    (* 100 (h10-benefit results cache year))
                      :bis 0
                      :radio 0
                      :chemo 0
                   :tra     0}
                  additionals)
        negative? (and (= :yes (rum/react (input-cursor :radio))) (some #(->> % second neg?) dataset))

        benefit (into {} dataset)

        icon-warn (fn [tk] (filled-icon (hex-palette (if negative? :invalid tk))))
        ]

    [:div
     (when-not printable
       [:div {:style {:margin-top "15px" :font-size 16}}
        (result-display-title ttt (ttt [:results/view-type "This chart"])
                              year
                              delay
                              horm)])




     ;;
     ;; had width 40%
     ;;

     [:div {:class-name (str "col-xs-6 " (:chart chart-style))
            :style      {#_#_:width (str (if side-by-side width 100) "%")
                         :display    "inline-block"
                         :margin-top "3ex"}}


      [:.chart-wrapper {:style {:position    "relative"
                                :padding-top 350}}


       (rum/with-key (inner-stacked-bar {;:subtitle-over  "for women with breast cancer, 5 and 10 years after surgery"
                                         :ttt            ttt
                                         :subtitle-under [:span (ttt [:chart/subtitle-1 " "]) year (ttt [:chart/subtitle-2 " years after surgery"])]
                                         :dataset        (filter #(->> % second neg? not) dataset)
                                         :negative?      negative?
                                         :sums           (into [] (reductions + (cons 0 (map second dataset))))
                                         :year           year
                                         :right?         false
                                         :printable      printable
                                         :rounded?       rounded?
                                         :hd             hd
                                         }) 1)]]

     ;
     ; legend
     ;
     [:#legend.col-xs-6 {:style {:vertical-align "top"
                                 :padding-top    (if side-by-side "20px" "20px")
                                 :display        "inline-block"}}

      [:p]
      [:div {:style {:border-top     (str "4px dashed " dashed-stroke)
                     :width          "50px"
                     :display        "inline-block"
                     :margin-top     "15px"
                     :vertical-align "top"}}]
      [:div {:style {:display     "inline-block"
                     :margin-left "10px"
                     :width       "calc(100% - 60px)"}}
       [:p (ttt [:chart/legend-1 "Survival rate excluding deaths from breast cancer. "])
        [:span.screen-only (small-help-button {:style   {:display "block"}
                                               :help-id "dashed"})]]]

      (when (not hd)
        (when (available-treatment? :bis)
          [:p (icon-warn :bis) (add-benefit** ttt benefit :bis (ttt [:chart/legend-bis "bisphosphonates"]))]))
      (when (and (not hd) (available-treatment? :tra))
        [:p (icon-warn :tra) (add-benefit** ttt benefit :tra (ttt [:chart/legend-tra "trastuzumab"]))])
      (when (and (not hd) (available-treatment? :chemo))
        [:p (icon-warn :chemo) (add-benefit** ttt benefit :chemo (ttt [:chart/legend-chemo "chemotherapy"]))])
      (when (and (not hd) (available-treatment? :radio))
        [:p (icon-warn :radio) (add-benefit** ttt benefit :radio (ttt [:chart/legend-radio "radiotherapy"]))])
      (when (available-treatment? :horm)
        [:p (icon-warn :horm) (add-benefit** ttt benefit :horm [:span (when delay (ttt [:chart/legend-h10 "further"]))
                                                              " " (ttt [:chart/legend-horm "hormone therapy"])])]
        )

      [:p (icon-warn :surgery)
       (if hd
         (ttt [:chart/without " Survival without further therapy is "])
         (ttt [:chart/surgery-only " Surgery only survival is "]))

       (util/round (benefit :surgery year) rounded?)

       (ttt [:chart/legend-at "% at "]) year (ttt [:chart/legend-yrs " years" "."])]
      ]
     (when (not printable) [:col-xs-12 (press-and-hold ttt)])]))






(rum/defc results-in-chart [chart-options]
  "Content of the Charts tab, showing treatment options"
  [:div
   (stacked-bar (assoc chart-options :width 50
                                     :h-over-w 0.8
                                     :font-scale 1
                                     :chart-style stacked-bar-chart-style))
   ;(press-and-hold ttt)
   ])

