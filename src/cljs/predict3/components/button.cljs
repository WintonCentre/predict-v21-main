(ns predict3.components.button
  (:require [rum.core :as rum]
            [predict3.results.util :refer [data-fill callout-fill alison-blue-3]]
            [predict3.state.run-time :refer [unknown input-cursor input-change help-key-change
                                             route-change settings-change print-change]]
            [graphics.simple-icons :as simple]
            [pubsub.feeds :refer [publish]]
            [clojure.pprint :refer [cl-format]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            ))

;; Simple text only component that only echoes text
(rum/defc information < rum/reactive [{:keys [key values ttt ttt-key]} group-cursor]
  [:div
   [:div
    [:div (ttt [ttt-key values])]]])

(defn button-label
  [t k label]
  [(keyword (str "button/" (name (:topic t)) "-" (if (keyword? k) (name k) (if (number? k) (str "n" k) k))))
   label])

;; Generic toggle
(rum/defc toggle-button < rum/static [{:keys [ttt key value topic disabled]
                                       :or   {disabled false}} label]
  (when (nil? ttt) (println "toggle-button (nil ttt) " (:topic topic) key))

  (let [handler #(when (not= key value) (publish topic key))]

    [:button {:class-name  (str "btn btn-default btn-sm custom " (if (= key value) " active" ""))
              :style       {:padding-right 4 :padding-left 4}
              :disabled    disabled
              :type        "button"
              :key         label
              :on-key-down #(when (= (.. % -nativeEvent -code) "Enter") (handler))
              :onClick     handler
              }
     (if (= :result-year (:topic topic))
       [:b label]
       (ttt (button-label topic key label))
       )]))

(rum/defc radio-button-group < rum/reactive [{:keys [:ttt ttt key aria-label aria-describedby values unknowable vertical]} group-cursor]
  (let [group-value (rum/react group-cursor)]
    ;; extra divs are necessary for correct error state outlining
    ;;
    ;; Apart from valid values, the value may also be :disabled
    [:div
     [:div
      [:div {:role       "group" :aria-label aria-label :aria-describedby aria-describedby
             :id         (name key)                         ;EXPERIMENTAL
             :class-name (str "btn-group" (if vertical "-vertical" ""))}

       (map (fn [[val label]]
              (rum/with-key
                (toggle-button {:ttt ttt :key val :value group-value :topic (input-change key) :disabled (= group-value :disabled)} label)
                label))
            values)

       (when unknowable
         (toggle-button {:ttt ttt :key :unknown :value group-value :topic (input-change key) :disabled (= group-value :disabled)}
                        (unknown)))
       ]]]))

(rum/defc horiz-radio-button-group < rum/reactive [{:keys [ttt key aria-label aria-describedby values unknowable]} group-cursor]
  (let [group-value (rum/react group-cursor)]
    [:div {:id         (name key)                           ;EXPERIMENTAL
           :role       "group" :aria-label aria-label :aria-describedby aria-describedby
           :style      {:display "inline-block"}
           :class-name (str "btn-group")}

     (map (fn [[val label & [disabled]]]
            (rum/with-key
              (if disabled
                (toggle-button {:ttt ttt :key val :value group-value :topic (input-change key) :disabled "true"} label)
                (toggle-button {:ttt ttt :key val :value group-value :topic (input-change key)} label))
              label))
          values)

     (when unknowable
       (toggle-button {:ttt ttt :key :unknown :value group-value :topic (input-change key)} (unknown)))
     ]))

(rum/defc year-picker < rum/reactive [ttt]
  (let [disable-5 (= :ys5 (rum/react (input-cursor :delay)))
        ten-year false                                      ;(= 10 (rum/react (input-cursor :ten-fifteen)))
        ]

    (if ten-year
      (horiz-radio-button-group {:ttt    ttt
                                 :key    :result-year
                                 :values [[5 "5" disable-5]
                                          [10 "10"]]} (input-cursor :result-year))
      (horiz-radio-button-group {:ttt    ttt
                                 :key    :result-year
                                 :values [[5 "5" disable-5]
                                          [10 "10"]
                                          [15 "15"]]} (input-cursor :result-year)))
    ))

(rum/defc h5-h10-picker < rum/reactive [ttt printable]
  (if printable
    [:span (condp = (rum/react (input-cursor :horm))
             nil ""
             :h5 "5-years"
             :h10 "10-years")]
    (horiz-radio-button-group {:ttt    ttt
                               :key    :horm
                               :values [[:h5 "5-years"]
                                        [:h10 "10-years"]]} (input-cursor :horm))))

(rum/defc radio-button-group-vertical
  [props group-cursor]
  (radio-button-group (merge props {:vertical true}) group-cursor))

;;;
;; Buttons invoking modals
;;;

(rum/defc small-help-button < rum/static [{:keys [help-id icon-name title text] :as props}]
  [:button.btn.btn-info.btn-sm.screen-only
   {:type         "button"
    :tabIndex     "0"
    :data-toggle  "modal"
    ;:data-target  "#infoModal"
    :data-target  "#topModal"
    :aria-label   (str "show help on " title)
    :title        title
    :data-content "Help TBD"
    :on-click     #(publish help-key-change help-id)
    :on-key-down  #(when (= "Enter" (.. % -nativeEvent -code))
                     (publish help-key-change help-id))
    :style        {:cursor        "pointer"
                   :padding       "0px 11px"
                   :font-size     "20px"
                   :border-radius 15}
    }
   (simple/icon {:family :fa} "info") ""])

(rum/defc settings-button < rum/static [ttt]
  (let [settings "settings"]
    [:button.btn.btn-default.screen-only
     {:type         "button"
      :role         "button"
      :aria-label   "show settings"
      :tab-index    "0"
      :data-toggle  "modal"
      :data-target  "#settingsModal"
      :title        "Settings"
      :data-content "Settings content"
      :on-click     #(do (.stopPropagation (.. % -nativeEvent))
                         (.preventDefault (.. % -nativeEvent))
                         (publish settings-change settings))
      :on-key-down  #(when (= "Enter" (.. % -nativeEvent -code))
                       (publish settings-change settings))
      :style        {:min-width "100px"}
      }
     (simple/icon {:family :fa} "cog") (ttt [:tools/settings " Settings"])]))

(rum/defc print-button < rum/static []
  [:button.btn.btn-danger.btn-lg.screen-only.pull-right
   {:type         "button"
    :role         "button"
    :aria-label   "show printable results"
    :tab-index    "0"
    :data-toggle  "modal"
    :data-target  "#printModal"
    :title        "Print Results"
    :data-content "Print content"
    :on-click     #(publish print-change "print")
    :style        {:margin-right 15}
    :on-key-down  #(when (= "Enter" (.. % -nativeEvent -code))
                     (publish print-change "print"))
    }
   (simple/icon {:family :fa} "print") " Print"])

(defn start-button [ttt]
  [:div
   [:button.btn.btn-primary.btn-lg {:style      {:margin 20}
                                    :aria-label "go to predict tool"
                                    :type       "button"
                                    :on-click   #(publish route-change [:tool nil nil])}
    (simple/icon {:family :fa} "chevron-right") (ttt [:home/start-button " Start Predict"])]])
