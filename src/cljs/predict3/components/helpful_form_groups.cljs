(ns predict3.components.helpful-form-groups
  (:require [clojure.string :as str  :refer [replace lower-case capitalize]]
            [rum.core :as rum]
            [predict3.components.select :refer [picker]]
            [predict3.state.run-time :refer [input-cursor input-change input-label ttt-cursor]]
            [predict3.state.load-config :refer [widget-options make-widget render-widget]]
            [predict3.mixins :refer [active-monitor]]
            [predict3.components.button :refer [small-help-button]]
            [predict3.components.util :refer [widget-ttt]]
            [predict3.models.runner :refer [error?]]
            [pubsub.feeds :refer [publish]]
            ))

(rum/defc helpful-input < rum/static rum/reactive [{:keys [ttt label key error help-id] :as props} children]
  [:div {:key   key :data-key key
         :class (str "form-group" (if error " has-error" ""))
         :style {:vertical-align "top"
                 :width          "100%"
                 :display        "inline-block"}}

   [:div {:style {:display        "inline-block"
                  :vertical-align "middle"
                  :width          "100px"}}
    [:label.control-label {:style (merge {:textAlign "left"
                                          :padding   "1px 5px"}
                                         (when (= (rum/react (input-cursor key)) :disabled) {:color "#999"}))
                           :for   (name key)}
     ;label

     (widget-ttt ttt "help" key label)
     ]]

   [:div {:style {:display        "inline-block"
                  :margin-left    "10px"
                  :width          "30px"
                  :vertical-align "middle"}}
    (when help-id (small-help-button {:help-id help-id}))]



   [:div {:style {:display        "inline-block"
                  :margin-left    "10px"
                  :text-align     "left"
                  :width          "auto"                    ;"calc(100% - 150px)"
                  :vertical-align "middle"
                  }}
    [:div {:style {:padding-left   0
                   :display        "inline-block"
                   :vertical-align "middle"
                   }} children]]])

; This allows us to use different widgets and different labels in different models
(rum/defc form-entry < rum/reactive active-monitor [{:keys [ttt label key] :as props}]
  [:div
   [:.screen-only
    (helpful-input {:ttt     (if ttt ttt (rum/react ttt-cursor))
                    :label   (input-label key)              ;label
                    :key     key
                    :help-id (if label (str/replace (lower-case label) " " "-"))
                    :error   (error? (rum/react (input-cursor key)))}
                   (render-widget ttt key)
                   )]])