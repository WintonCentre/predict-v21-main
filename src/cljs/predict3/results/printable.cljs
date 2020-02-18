(ns predict3.results.printable
  (:require [clojure.string :refer [split join capitalize]]
            [rum.core :as rum]
            [predict3.components.helpful-form-groups :refer [form-entry]]
            [predict3.state.config :refer [input-groups get-input-default]]
            [predict3.content-reader :refer [section]]
            [predict3.results.common :refer [common-results-text population-text stacked-yearly-values stacked-bar-yearly-props]]
            [predict3.state.run-time :refer [input-cursor input-change input-label year-selected ttt-cursor]]
            [predict3.components.button :refer [year-picker]]
            [predict3.results.util :refer [treatment-keys avoid-decimals benefit% benefits%]]
            [predict3.results.marshalling :refer [additional-benefit-map]]
            [predict3.mixins :refer [set-default]]
            [predict3.layout.header :refer [footer]]
            [pubsub.feeds :refer [publish]]

            [predict3.results.table :refer [results-in-table]]
            [predict3.results.curves :refer [results-in-curves]]
            [predict3.results.chart :refer [results-in-chart]]
            [predict3.results.text :refer [results-in-text]]
            [predict3.results.icons :refer [results-in-icons]]
            ))

(defn avoid-break [& content]
  (reduce conj [:div {:style {:break-inside "avoid"}}] content))


(defn break-before [& content]
  (reduce conj [:div {:style {:break-inside "avoid"}}] content))


(rum/defc inputs-in-print < rum/reactive [ttt]
  [:.row
   [:.col-sm-8.col-sm-offset-2
    [:table.table.table-bordered.table-responsive {:style {:font-size "16px"}}
     [:thead
      [:tr
       [:th (ttt [:print/input "Input"])]
       [:th (ttt [:print/value "Value"])]]]
     [:tbody
      [:tr
       [:td (input-label :age)]
       [:td (rum/react (input-cursor :age))]]
      [:tr
       [:td (input-label :post-meno)]
       [:td ({:post (ttt [:yes "Yes"]) :pre (ttt [:no "No"]) :unknown (ttt [:unknown "Unknown"])}
             (rum/react (input-cursor :post-meno)))]]
      [:tr
       [:td (input-label :er-status)]
       [:td ({:yes (ttt [:pos "Positive"]) :no (ttt [:neg "Negative"])} (rum/react (input-cursor :er-status)))]]
      [:tr
       [:td (input-label :her2-status)]
       [:td ({:yes (ttt [:pos "Positive"]) :no (ttt [:neg "Negative"]) :unknown (ttt [:unknown "Unknown"])} (rum/react (input-cursor :her2-status)))]]
      [:tr
       [:td (input-label :ki67-status)]
       [:td ({:yes (ttt [:pos "Positive"]) :no (ttt [:neg "Negative"]) :unknown (ttt [:unknown "Unknown"])} (rum/react (input-cursor :ki67-status)))]]
      [:tr
       [:td (input-label :size)]
       [:td (rum/react (input-cursor :size))]]
      [:tr
       [:td (input-label :grade)]
       [:td (-> (rum/react (input-cursor :grade)) str last)]]
      [:tr
       [:td (input-label :mode)]
       [:td ({:symptomatic (ttt [:symptoms "Symptoms"])
              :screen (ttt [:screening "Screening"])
              :unknown (ttt [:unknown "Unknown"])} (rum/react (input-cursor :mode)))]]
      [:tr
       [:td (input-label :nodes)]
       [:td (rum/react (input-cursor :nodes))]]
      (when-not (= :disabled (rum/react (input-cursor :micromets)))
        [:tr
         [:td (input-label :micromets)]
         [:td (capitalize (name (rum/react (input-cursor :micromets))))]])]]]])

(rum/defc treatment-note [title content]

  [:.col-xs-12
   (avoid-break
     [:h4 title]
     content)]
  )

(rum/defc treatments-in-print < rum/reactive [ttt]
  (let [horm (rum/react (input-cursor :horm))
        horm-label (ttt [:print/horm (input-label :horm)])
        chemo-label (ttt [:print/chemo (input-label :chemo)])
        tra-label (ttt [:print/tra (input-label :tra)])
        bis-label (ttt [:print/bis (input-label :bis)])
        chemo (rum/react (input-cursor :chemo))
        tra (rum/react (input-cursor :tra))
        bis (= :yes (rum/react (input-cursor :bis)))]
    (if-not (or horm chemo tra bis)
      [:h2 (ttt [:print/no-txs "No Treatments Selected after Surgery"])]
      [:.row
       [:.col-sm-12
        [:h2 (ttt [:print/txs "Treatments after Surgery"])]
        [:section.print-bigger
         (ttt [:print/selected-txs "Selected treatments after surgery are:"])
         (when horm [:li horm-label])
         (when chemo [:li chemo-label " (" ({:3rd  (ttt [:print/chemo-3 "3rd generation"])
                                             :2nd  (ttt [:print/chemo-2 "2nd generation"])
                                             :none (ttt [:print/none "None"])} (rum/react (input-cursor :chemo))) ")"])
         (when tra [:li tra-label])
         (when bis [:li bis-label])]
        [:.row.print-smaller
         (when horm (treatment-note horm-label (when horm [:div (rest (section ttt "hormone-therapy"))])))
         (when chemo (treatment-note chemo-label (when chemo [:div (rest (section ttt "chemotherapy"))])))
         (when tra (treatment-note tra-label (when tra [:div (rest (section ttt "trastuzumab"))])))
         (when bis (treatment-note bis-label (when bis [:div (rest (section ttt "bisphosphonates"))])))
         ]]])))

(rum/defc results-in-print
          < rum/reactive (set-default :result-year)
          [ttt]
  (let [delay (rum/react (input-cursor :delay))]
    [:.row
     [:.col-sm-12

      (avoid-break
        [:h2 (ttt [:print/inputs "Inputs"])]
        (inputs-in-print ttt))

      (avoid-break
        [:h2 (ttt [:print/results "Results"])]
        [:p {:style {:margin-top "15px"}}
         (population-text ttt delay true)
         (ttt [:print/basis ", and are based on the information you provided."])]
        [:h3 (ttt [:print/table "Survival table - "]) (rum/react (year-selected)) (ttt [:print/years-after " years after surgery."])]
        [:div {:style {:max-width "60%" :margin-left "20%"}}
         (results-in-table {:ttt ttt :printable true})])

      (avoid-break
        [:h3 (ttt [:print/curve "Survival curve"])]
        [:p (ttt [:print/curve-1 "This graph shows the percentage of women surviving up to "])
         15 #_(rum/react (input-cursor :ten-fifteen)) (ttt [:print/curve-2 " years."])]
        [:div {:style {:max-width "100%" :margin-left "0%"}}
         (results-in-curves {:ttt ttt :printable true :width 600})])

      (avoid-break
        [:h3 (ttt [:print/chart-1 "Overall survival"])]
        [:p (ttt [:print/chart-2 "This chart shows the percentage of women surviving "])
         (rum/react (year-selected)) (ttt [:print/years-2 " years after surgery."])]
        (results-in-chart {:ttt ttt :printable true}))

      (avoid-break
        [:div.clearfix]
        [:h3 (ttt [:print/summary "In Summary"])]
        (results-in-text {:ttt ttt :printable true}))

      (avoid-break
        [:h3 (ttt [:print/icons1 " "]) (rum/react (year-selected)) (ttt [:print/icons " year outcomes for 100 women"])]
        (results-in-icons {:ttt ttt :printable true}))
      (avoid-break
        (treatments-in-print ttt))

      (footer ttt)]]))

