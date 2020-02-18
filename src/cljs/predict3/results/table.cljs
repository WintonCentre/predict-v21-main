(ns predict3.results.table
  (:require [clojure.string :refer [split join]]
            [rum.core :as rum]
            [predict3.components.helpful-form-groups :refer [form-entry]]
            [predict3.state.config :refer [input-groups get-input-default]]
            [predict3.results.common :refer [common-results-text population-text stacked-yearly-values stacked-bar-yearly-props result-display-title press-and-hold]]
            [predict3.state.run-time :refer [input-cursor input-change input-label rounded-cursor
                                             results-cursor h-cache-cursor year-selected]]
            [predict3.components.button :refer [year-picker small-help-button h5-h10-picker]]
            [predict3.results.util :refer [treatment-keys avoid-decimals benefit% benefits% benefits-1dp% larger-overall-survival
                                           h10-benefit without-h10] :as util]
            [predict3.results.marshalling :refer [additional-benefit-map]]
            [predict3.mixins :refer [set-default]]
            [pubsub.feeds :refer [publish]]))

;;
;;
;; Addition of radiotherapy requires further work
;;
;;
(comment

  (def all-results @results-cursor)
  (def delay @(input-cursor :delay))
  (def horm @(input-cursor :horm))
  (def hd (and horm (= :ys5 delay)))
  (def result-year @(input-cursor :result-year))
  (def results (nth all-results result-year))
  (def all-cached @h-cache-cursor)
  (def cached (nth all-cached result-year))
  (def benefit (* 100 (h10-benefit all-results all-cached result-year)))
  (def without (* 100 (without-h10 all-cached result-year)))
  )
(rum/defc tables
  "renders table in the table view given data in the form of
  a map of benefits by treatment key
  e.g. {:chemo 4.755178332351184, :chemo-high 6.624182477855545, :tra 5.4090280552278935, :surgery 64.02388341026368,
  :horm-low 5.363014324399085, :chemo-low 2.7164336698887293, :tra-low 3.7950966538692787, :br 0,
  :oth 2.452449549218527, :horm-high 11.57564406445567, :tra-high 7.544862101208761, :horm 9.236042018064127}"
  < rum/reactive
  [{:keys [ttt printable]}]

  (let [rounded? (rum/react rounded-cursor)
        all-results (rum/react results-cursor)
        delay (rum/react (input-cursor :delay))
        horm (rum/react (input-cursor :horm))
        hd (and horm (= :ys5 delay))
        data (additional-benefit-map {:annual-benefits all-results
                                      :year            (rum/react (year-selected))})
        uncertainty? (= :yes (rum/react (input-cursor :show-uncertainty)))
        tx (->> [[:surg true]
                 [:horm (and (= (rum/react (input-cursor :er-status)) :yes) (some? (rum/react (input-cursor :horm))))]
                 [:chemo (some? (rum/react (input-cursor :chemo)))]
                 [:tra (= :yes (rum/react (input-cursor :tra)))]
                 [:bis (= :yes (rum/react (input-cursor :bis)))]]
                (filter (fn [[k v]] (= true v))))
        txm (into {} tx)
        [last-tx-key _] (last tx)
        result-key-s {:surg  "z"
                      :horm  "h"
                      :chemo "hrc"
                      :tra   "hrct"
                      :bis   "hrctb"}
        append-key (fn [k s] (keyword (str (result-key-s k) s)))
        ]
    [:.table-responsive {:style {:margin-top "15px"
                                 :font-size  "1.2em"}}
     [:table.table.table-hover {:style {:padding 0 :margin 0 :font-size "16px"}}
      [:thead
       [:tr
        [:th (ttt [:table/col1-header "Treatment"])]
        [:th (ttt [:table/col2-header "Additional Benefit"])]
        [:th (ttt [:table/col3-header "Overall Survival %"])]]]

      (if hd
        (let [result-year (rum/react (input-cursor :result-year))
              results (nth all-results result-year)
              all-cached (rum/react h-cache-cursor)
              cached (nth all-cached result-year)
              benefit (* 100 (h10-benefit all-results all-cached result-year))
              without (* 100 (without-h10 all-cached result-year))

              ; we have to pick the low, mid and high results for the top active treatment to get the correct range
              [low-k mid-k high-k] [(append-key last-tx-key "-low") (keyword (result-key-s last-tx-key)) (append-key last-tx-key "-high")]

              [[h5-low h5 h5-high] [h10-low h10 h10-high]] (map
                                                             (fn [f]
                                                               (map
                                                                 #(* 100 (f %1 %2))
                                                                 ((juxt low-k mid-k high-k) results)
                                                                 ((juxt low-k mid-k high-k) cached)
                                                                 ))
                                                             [min max])]

          [:tbody
           [:tr
            [:td (ttt [:table/h5-already "Treatments already received"])]
            [:td "-"
             ]
            [:td (str (if rounded?
                        (Math/round without)
                        (util/dp1 without))
                      "%")]]

           [:tr
            [:td (ttt [:table/plus-5-more "+ 5 more years hormone therapy"])]
            [:td (str
                   (if (and (not uncertainty?) rounded?)
                     (Math/round benefit)
                     (util/dp1 (+ benefit)))
                   "%")

             (when uncertainty?
               (str " ("
                    (avoid-decimals (- h10-low h5) true)
                    "% – "
                    (avoid-decimals (- h10-high h5) true)
                    "%)"))]
            [:td (str (let [v (+ without benefit)]
                        (if rounded?
                          (Math/round v)
                          (util/dp1 v)))
                      "%")]]])

        [:tbody
         [:tr
          [:td (ttt [:table/surgery-only "Surgery only "])]
          [:td "-"]
          [:td#overall-surg ((if (= last-tx-key :surg) larger-overall-survival identity)
                             (util/rounded?benefit% data :surgery rounded?))]]

         (when (:horm txm)
           [:tr
            [:td (ttt [:table/plus-h-1 "+ Hormone therapy"])
             #_(when hd
                 [:span [:br] (ttt [:table/plus-h-2 "for "]) ({:h5 5 :h10 10} horm) (ttt [:table/plus-h-3 " years"])])]
            [:td#horm (util/rounded?benefit% data :horm (and (not uncertainty?) rounded?))
             (if uncertainty?
               (str " (" (benefit% data :horm-low uncertainty?) " – " (benefit% data :horm-high uncertainty?) ")")
               "")]
            [:td#overall-horm ((if (= last-tx-key :horm) larger-overall-survival identity)
                               (util/rounded?benefits% data rounded? :surgery :horm))]])

         (when (:chemo txm)
           [:tr
            [:td (ttt [:table/plus-c "+ Chemotherapy"])]
            [:td#chemo (util/rounded?benefit% data :chemo (and (not uncertainty?) rounded?))
             (if uncertainty?
               (str " (" (benefit% data :chemo-low uncertainty?) " – " (benefit% data :chemo-high uncertainty?) ")")
               "")]
            [:td#overall-chemo ((if (= last-tx-key :chemo) larger-overall-survival identity)
                                (util/rounded?benefits% data rounded? :surgery :horm :radio :chemo))]])

         (when (:tra txm)
           [:tr
            [:td (ttt [:table/plus-t "+ Trastuzumab"])]
            [:td#tra (util/rounded?benefit% data :tra (and (not uncertainty?) rounded?))
             (if uncertainty?
               (str " (" (benefit% data :tra-low uncertainty?) " – " (benefit% data :tra-high uncertainty?) ")")
               "")]
            [:td#overall-tra ((if (= last-tx-key :tra) larger-overall-survival identity)
                              (util/rounded?benefits% data rounded? :surgery :horm :radio :chemo :tra))]])

         (when (:bis txm)
           [:tr
            [:td (ttt [:table/plus-b "+ Bisphosphonates"])]
            [:td#bis (util/rounded?benefit% data :bis (and (not uncertainty?) rounded?))
             (if uncertainty?
               (str " (" (benefit% data :bis-low uncertainty?) " – " (benefit% data :bis-high uncertainty?) ")")
               "")]
            [:td#overall-bis ((if (= last-tx-key :bis) larger-overall-survival identity)
                              (util/rounded?benefits% data rounded? :surgery :horm :radio :chemo :tra :bis))]])])
      [:tbody
       [:tr
        [:td {:col-span 3}
         (ttt [:table/no-bc-death-1 "If death from breast cancer were excluded, "])
         [:span#nobc (util/round (- 100 (:oth data)) rounded?)]
         (ttt [:table/no-bc-death-2 "% would survive at least "])
         (rum/react (input-cursor :result-year))
         (ttt [:table/no-bc-death-4 " years, and "]) [:span#other (util/round (:oth data) rounded?)]
         (ttt [:table/no-bc-death-5 "% would die of other causes."])
         (small-help-button {:help-id "nobody"})]]]]]))


(comment
  (:annual-benefits @results-cursor)
  (delta-pc-benefits {:treatments treatment-keys})
  (additional-benefit-map {:annual-benefits (:annual-benefits @results-cursor)
                           :year            10}))

(rum/defc results-in-table
  "renders the table view of results"
  < rum/reactive (set-default :result-year)
  [{:keys [ttt printable]}]
  [:div
   [:.row
    (when-not printable [:.col-sm-12
                         [:div {:style {:margin-top "15px" :font-size 16}}
                          (result-display-title ttt (ttt [:results/view-type "This table"])
                                                (rum/react (input-cursor :result-year))
                                                (rum/react (input-cursor :delay))
                                                (rum/react (input-cursor :horm)))]])
    [:.col-sm-12 {:style {:margin-bottom "15px"}}
     (tables {:ttt ttt :printable printable})
     (when-not printable
       [:div
        (form-entry {:ttt ttt :key :show-uncertainty :label "show-ranges"})
        (press-and-hold ttt)])]]])

(comment
  (:benefits2-1 @results-cursor)
  (additional-benefit-map {:annual-benefits (:annual-benefits @results-cursor)
                           :year            @(year-selected)}))
