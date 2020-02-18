(ns predict3.results.common
  (:require [rum.core :as rum]
            [predict3.results.util :refer [lookup ->Item]]
            [predict3.state.run-time :refer [input-cursor rounded-cursor rounded-change]]
            [predict3.components.button :refer [year-picker small-help-button]]
            [pubsub.feeds :refer [publish]]))


(defn available-treatment?
  [treatment-key]
  (condp = treatment-key
    :surgery true
    :horm (#{:h5 :h10} @(input-cursor :horm))
    :radio (and @(input-cursor :enable-radio) @(input-cursor :radio))
    :chemo @(input-cursor :chemo)
    :tra (= :yes @(input-cursor :tra))
    :bis (= :yes @(input-cursor :bis))                      ;(and (not (= :pre @(input-cursor :post-meno))) @(input-cursor :bis))
    ))


(def common-results-text
  "These results are for women who have already had surgery. ")

(rum/defc population-text
  [ttt delay & [no-stop?]]
  [:span {:style {:font-size 16}}
     (ttt [:table/warn1 "These results are for women who have already had surgery"])
     (when (= :ys5 delay) [:b (ttt [:table/warn2 " and have already received 5 years of hormone therapy"])])
     (when-not no-stop? ". ")])

(def result-scroll-height "850px")

(defn stacked-yearly-values
  "Lookup treatments and create a stacked bar dataset from model results, indexed by year.
  Each data-item is a key value pair ordered in the same way as incoming treatments vector."
  [{:keys [model treatments horm-yes tra-yes results]} year]

  (mapv (fn [key]
          [key (lookup {:model      model
                        :treatments treatments
                        :horm-yes   horm-yes
                        :tra-yes    tra-yes
                        :key        key
                        :result     (get results year)})])
        treatments))

(defn stacked-bar-yearly-props
  "Lookup treatments and create a stacked bar dataset from model results, indexed by year.
  Each data-item is an Item with :treatment-key and :value"
  [{:keys [model treatments horm-yes tra-yes results] :as result-data} year]
  {year
   (mapv (fn [[key value]]
           (->Item key value))
         (stacked-yearly-values result-data year))})



(defn pub-rounded
  [rounded? event]
  (do
    (publish rounded-change rounded?)
    (.stopPropagation event)
    (.preventDefault event)
    )
  )

(def round-on (partial pub-rounded true))
(def round-off (partial pub-rounded false))

(rum/defc press-and-hold < rum/static rum/reactive
                           "Provides a button which shows an extra decimal place when pressed."
  [ttt]
  (let [rounded? (rum/react rounded-cursor)]
    [:div {:style {:margin-top 10}}
     [:button.btn-default.btn-sm
      {:style         {
                       :background-color (if rounded? "white" "#cceef8")
                       :border-radius    20
                       :color            "#686868"
                       :outline          "none"
                       }
       :on-mouse-down round-off                             ;#(publish rounded-change false)
       :on-touch-start round-off                             ;#(publish rounded-change false)
       :on-mouse-up   round-on                              ;#(publish rounded-change true)
       :on-touch-end   round-on                              ;#(publish rounded-change true)
       :on-mouse-out  round-on                              ;#(publish rounded-change true)
       :on-key-press  round-off                             ;#(publish rounded-change false)
       :on-key-up     round-on}                             ;#(publish rounded-change true)

      (ttt [:results/press-and-hold "Press and hold"])
      ]
     (ttt [:results/press-and-hold-text " for another decimal place to see how the numbers add up."])]))

(rum/defc labelled-year-picker
  [ttt]
  [:div {:style {:color     "#333333"
                 :font-size 16}}
   (ttt [:results/year-picker-label "Select number of years since surgery you wish to consider:"])
   [:br]
   [:div {:style {:margin-left "1em"}} (year-picker ttt)]])

(rum/defc result-display-title
  ([ttt display result-year delay horm]
   (result-display-title ttt display result-year delay horm [:results/display-title-percentage "percentage "]))
  ([ttt display result-year delay horm number-v]
   [:div {:style {:color     "#333333" :font-size 16}}
    (labelled-year-picker ttt)
    display
    (ttt [:results/display-title-1-1 " shows the "])
    (ttt number-v)
    (ttt [:results/display-title-1-2 "of women who survive at least "])
    result-year
    (ttt [:results/display-title-3 " years after surgery"])
    (if (and (= :ys5 delay) horm)
      [:span " "
       [:b
        (ttt [:table/year-pick-2-2 "(that is, "])
        (- result-year 5)
        (ttt [:table/year-pick-2-3 " years from now). "])]]
      ".")]))

