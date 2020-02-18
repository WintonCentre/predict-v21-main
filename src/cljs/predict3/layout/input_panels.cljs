(ns predict3.layout.input-panels
  (:require [rum.core :as rum]
            [predict3.state.run-time :refer [model
                                             input-cursor
                                             input-change
                                             help-key-change
                                             ]]
            [predict3.router :refer [navigate-to]]
    ;[predict3.layout.input-panels :refer [clear-all-button]]
            [predict3.layout.navbar :refer [clear-all-button]]
            [predict3.state.load-config :refer [live-keys-by-model]]
            [predict3.state.mutations :refer [clear-inputs]]
            [predict3.state.local-storage :refer [get-settings!]]
            [predict3.components.helpful-form-groups :refer [form-entry]]
            [predict3.components.helpful-form-groups :refer [helpful-input form-entry]]
            [predict3.components.bs-mixins :refer [tt-mixin]]
            [graphics.simple-icons :as simple]
            [pubsub.feeds :refer [publish]]
            ))


(defn key-event
  "prevent undesirable reload behaviour on form events - particularly in IE10 - due to unwanted onSubmits"
  [e]
  (let [nev (.-nativeEvent e)]
    (when (= "Enter" (.-key nev))
      (.preventDefault nev))))

(defn submit-event
  "prevent undesirable reload behaviour on form events - particularly in IE10 - due to unwanted onSubmits"
  [e]
  (let [nev (.-nativeEvent e)]
    (.preventDefault nev)
    (.stopPropagation nev)
    ))


;;;
;; PATIENT RELATED
;;;
(rum/defc patient-related-panel < rum/reactive [ttt model-keys]
  (let [dcis (rum/react (input-cursor :dcis))]
    [:div
     [:form.form-horizontal {:on-key-press key-event
                             :on-submit    submit-event}
      (form-entry {:ttt ttt :label "DCIS or LCIS only?" :key :dcis})
      (when (#{:yes :unknown} dcis)
        [:div {:style {:color       "#686868"
                       :margin-left "145px"
                       :margin-top  -5}}
         [:i.fa.fa-exclamation-triangle {:aria-hidden "true"
                                         :style       {:color         (if (= :yes dcis) "red" "orange")
                                                       :padding-right 5}}]
         (ttt [:dcis/invasive "Predict should not be used for DCIS/LCIS unless an invasive tumour was also present."])
         (when (= :unknown dcis) (ttt [:dcis/if-unsure "If you're unsure use the data with caution and please consult your medical professional"]))])
      (when-not (= :yes dcis)
        [:div
         (when (model-keys :age)
           [:div
            (rum/with-key (form-entry {:ttt ttt :label "Age" :key :age}) :gooey)
            [:div {:style {:color       "#686868"
                           :margin-left "145px"
                           :margin-top  "-5px"}}
             (ttt [:age/valid-range "Age must be between 25 and 85"])]])

         (when (= :yes (rum/react (input-cursor :enable-bis)))
           (form-entry {:ttt ttt :label "Post Menopausal" :key :post-meno}))])
      ;[:hr]
      ]]))


;;;
;; TUMOUR RELATED
;;;

(rum/defc micromets-info-box-link [text]
  [:div {:style {:margin-left 145
                 :margin-top  -5}}
   [:a {:style    {:color  "#000" :text-decoration "underline"
                   :cursor "pointer"}
        :on-click #(publish help-key-change "micrometastases-only")} text]])

(rum/defc tumour-related-panel < rum/reactive rum/static [ttt model-keys]
  (let [nodes (rum/react (input-cursor :nodes))]
    [:form.form-horizontal {:on-key-press key-event
                            :on-submit    submit-event
                            ;:style        {:margin-top 60}
                            }
     (when (model-keys :size) (form-entry {:ttt ttt :label "Size" :key :size}))
     [:div {:style {:color       "#686868"
                    :margin-left "145px"
                    :margin-top  "-5px"}} (ttt [:tool/largest-size "If there was more than one tumour, enter the size of the largest tumour."])]
     (when (model-keys :grade) (form-entry {:ttt ttt :label "Grade" :key :grade}))
     (when (model-keys :mode) (form-entry {:ttt ttt :label "Detected by" :key :mode}))
     (when (= :screen (rum/react (input-cursor :mode)))
       [:div {:style {:color       "#686868"
                      :margin-left "145px"
                      :margin-top  "-5px"}}
        (ttt [:tool/detected-warn-1 "Detected as part of a preventive "])
        [:a {:href "https://www.nhs.uk/conditions/nhs-screening/" :target "_blank"}
         (ttt [:tool/detected-warn-2 "screening programme"])]])

     (when (model-keys :nodes)
       [:div
        (form-entry {:ttt ttt :label "Positive nodes" :key :nodes})
        (form-entry {:ttt ttt :label "Micrometastases only" :key :micromets})
        (when (not= "1" nodes)
          [:div {:style {:color       "#686868"
                         :margin-left "145px"
                         :margin-top  -5}} (ttt [:nodes/enabled-one "Enabled when positive nodes is 1. "])])
        (when (= "1" nodes)
          (micromets-info-box-link (ttt [:mmets/yes-means "“Yes” means the positive node has micrometastases only"])))
        (when (= "0" nodes)
          (micromets-info-box-link (ttt [:mmets/why-not "Why can't I enter micrometastases?"])))
        ])
     ;[:hr]
     ]))

(rum/defc hormone-form < rum/static [ttt model-keys]
  [:form.form-horizontal {:on-key-press key-event
                          :on-submit    submit-event}
   (when (model-keys :er-status) (form-entry {:ttt ttt :label "ER status" :key :er-status}))
   (when (model-keys :her2-status) (form-entry {:ttt ttt :label "HER2 status" :key :her2-status}))
   (when (model-keys :ki67-status)
     [:div
      (form-entry {:ttt ttt :label "Ki-67 status" :key :ki67-status})
      [:div {:style {:color       "#686868"
                     :margin-left "145px"
                     :margin-top  -5}} (ttt [:note/ki67 "Positive means more than 10%"])]])])


(rum/defc hormone-panel < rum/static [ttt model-keys]
  (hormone-form ttt model-keys)
  )

(rum/defc inputs-row < rum/reactive tt-mixin [ttt]
  (let [model-keys (live-keys-by-model model)
        dcis (= :yes (rum/react (input-cursor :dcis)))]
    [:div
     [:.row {:style {:vertical-align "middle"}}
      [:.col-xs-4.screen-only {:style {:margin-bottom 10
                                       :width         145
                                       }}
       (clear-all-button {:ttt ttt :on-click clear-inputs})]
      [:.col-xs-8 {:style {:padding-top 6 :margin-left 0}}
       [:p {:style {:color "#686868"}}
        (ttt [:tool/not-designed-for-all "Predict is not designed to be used in all cases. "])
        [:a {:on-click #(navigate-to [:about {:page :overview :section :whoisitfor}])
             :style    {:color           "black"
                        :text-decoration "underline"
                        :cursor          "pointer"}}
         (ttt [:tool/more-disclaimer-details "Click here for more details."])]
        [:br] (ttt [:tool/if-unsure "If you are unsure of any inputs or outputs, click on the "])
        [:span {:style {:padding          "0px 5px"
                        :font-size        "14px"
                        :background-color "#257ce1"
                        :color            "#ffffff"
                        :border-radius    15}} (simple/icon {:family :fa} "info")]
        (ttt [:tool/info-for-more-info " buttons for more information."])]]]
     [:row
      [:.col-sm-6.screen-only {:style {:padding-right 0}}
       (patient-related-panel ttt model-keys)
       (when-not dcis (hormone-panel ttt model-keys))]
      (when-not dcis
        [:.col-sm-6.screen-only {:style {:padding-right 0}}
         (tumour-related-panel ttt model-keys)]

        )]]
    ))
