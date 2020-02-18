(ns predict3.layout.result-panel
  (:require [clojure.string :refer [capitalize]]
            [rum.core :as rum]
            [predict3.state.run-time :refer [active-results-pane active-results-change input-cursor
                                             rounded-cursor rounded-change]]
            [pubsub.feeds :refer [publish]]
            [interop.jsx :refer [jsx]]
            [predict3.results.table :refer [results-in-table]]
            [predict3.results.curves :refer [results-in-curves]]
            [predict3.mixins :refer [sizing-mixin]]
            [predict3.results.chart :refer [results-in-chart]]
            [predict3.results.icons :refer [results-in-icons]]
            [predict3.results.text :refer [results-in-text]]
            [predict3.results.printable :refer [results-in-print]]
            [predict3.state.local-storage :refer [get-settings!]]
            [predict3.components.button :refer [small-help-button]]
            [graphics.simple-icons :refer [icon]]))

(defn tab-label-key
  "Lookup/edit key for a tab given a label or a key for that label
  ; (tab-label-key :curve)
  ; => [:tab-label/curve \"Curve\"]
  "
  [label-key]
  (let [label (name label-key)]
    [(keyword (str "tab-label/" label)) (capitalize label)]))

(defn safe-check
  "Check whether kn1 and kn2 share same names (2-arity) or some other property f (3-arity)."
  ([kn1 kn2]
   (safe-check kn1 kn2 name))
  ([kn1 kn2 f]
   (= (f kn1) (f kn2))))

(rum/defc result-tab-button < rum/reactive [ttt label-key]
  [:li {:role     "presentation"
        :class    (if (safe-check (rum/react active-results-pane) label-key) "active" nil)
        :on-click #(publish active-results-change label-key)
        :style    {:cursor           "pointer"
                   :border-radius    "3px"
                   :background-color "#def"}
        ;:key      label-key
        }
   [:a {:aria-controls (name label-key) :role "tab"} (ttt (tab-label-key label-key))]])

(rum/defc result-tab-pane < rum/reactive [ttt label content]
  [:div {:id    label :role "tabpanel"
         :class (str "tab-pane " (if (safe-check (rum/react active-results-pane) label) "active" nil))}

   (when (safe-check (rum/react active-results-pane) label)
     (if content
       (do
         (try
           (when (.-ga js/window)
             (.ga js/window "send" "event" "Results Tab" label))
           (catch js/Error e
             (.log js/console e)
             ))

         (content {:ttt ttt :width (.-innerWidth js/window)}))
       [:p "No content yet"]))])

(def tabs [:table :curves :chart :texts :icons])
(defn tab-panes [ttt]
  {:table  (result-tab-pane ttt "table" results-in-table)
   :curves (result-tab-pane ttt "curves" results-in-curves)
   :chart  (result-tab-pane ttt "chart" results-in-chart)
   :icons  (result-tab-pane ttt "icons" results-in-icons)
   :texts  (result-tab-pane ttt "texts" results-in-text)})

(defn to-front
  "If item is inside collection, return the collection with item first, else nil."
  [coll item]
  (if (some #(= item %) coll)
    (cons item (filter #(not= item %) coll))
    nil))

(defn reordered-tabs []
  (to-front tabs (:default-tab (get-settings! {:default-tab :table}))))

(defn reordered-tab-names [ttt]
  (into [] (map #(ttt (tab-label-key %)) (reordered-tabs))))

(defn reordered-tab-panes [ttt]
  (into [:.tab-content] (map #(get (tab-panes ttt) %)) (reordered-tabs)))

(rum/defc result-tabs < rum/static rum/reactive [ttt]
  (let [_ (rum/react (input-cursor :default-tab))
        rounded? (rum/react rounded-cursor)]
    [:div
     [:ul.nav.nav-pills {:role  "tablist"
                         :style {:font-size "16px"}}
      (map-indexed #(rum/with-key (result-tab-button ttt %2) %1) (reordered-tabs))]]))

(rum/defc result-panes < rum/static rum/reactive [{:keys [ttt printable]}]
  (let [_ (rum/react (input-cursor :default-tab))]
    (if-not printable
      [:row
       [:.col-xs-12 (reordered-tab-panes ttt)]
       [:.col-xs-12 {:style {:margin-bottom  20
                             :font-size      12
                             :color          "#686868"
                             :vertical-align "top"}}
        [:div
         (small-help-button {:help-id "when-i-add"})
         [:span {:style {:margin-left 10}}
          (ttt [:info/when-i-add "When I add or remove one treatment, why do the results for others sometimes change?"])]
         (when (and                                         ;(= :ys5 (rum/react (input-cursor :delay)))
                 (= :h10 (rum/react (input-cursor :horm)))
                 (not= 15 (rum/react (input-cursor :result-year)))
                 (not= :curves (rum/react active-results-pane)))
           [:div
            (small-help-button {:help-id "h10-already-warning"})
            [:span {:style {:margin-left 10}}
             (ttt [:results/why-no-ee-benefit "Why am I seeing no apparent benefit of extending hormone therapy?"])]])]]]
      (results-in-print ttt))))

(rum/defc result-panel < rum/reactive [{:keys [ttt printable] :as props}]
  (let [hd (and (= :ys5 (rum/react (input-cursor :delay))) (rum/react (input-cursor :horm)))]
    [:div#results
     [:h3 (ttt [:tool/results "Results"])]
     (when hd
       [:div {:style {:color       "#686868"
                      :margin-left "0px"
                      :margin-top  -5}}
        [:i.fa.fa-exclamation-triangle {:aria-hidden "true"
                                        :style       {:color         "red"
                                                      :padding-right 5}}]
        (ttt [:tool/h10-red-alert
              "These results are only relevant for someone who has already received 5 years of hormone therapy."])
        (small-help-button {:help-id "h10-already-warning"})])
     (if (not printable) (result-tabs (:ttt props)))
     (result-panes props)]))


(rum/defc results < rum/reactive [{:keys [container? printable] :as props}]
  [:div (when container? {:class-name " container "})
   [:.row
    [:.col-md-12
     (result-panel props)]]])

