(ns predict3.layout.treatments-panel
  (:require                                                 ;[org.martinklepsch.derivatives :as derivs :refer [drv]]
    [rum.core :as rum]
    [clojure.string :as s :refer [replace lower-case]]
    [cljs-css-modules.macro :refer-macros [defstyle]]
    [graphics.simple-icons :refer [icon]]
    [predict3.mixins :refer [sizing-mixin]]
    [predict3.state.run-time :refer [model results-cursor input-cursor input-label h10-latch-cursor help-key-change]]
    [predict3.components.button :refer [small-help-button]]
    [predict3.state.load-config :refer [live-keys-by-model render-widget]]
    [predict3.state.local-storage :refer [get-settings!]]
    [predict3.mixins :refer [treatment-monitor]]
    [predict3.components.helpful-form-groups :refer [helpful-input form-entry]]
    [pubsub.feeds :refer [publish]]
    [interop.jsx :refer [jsx]]))


(defstyle treatments-style
  ["div" {:font-size "12px"}]
  [".treatments-header"
   {:background-color "rgba(255, 140, 0, 1) !important"
    :color            "white !important"
    :font-size        "10px !important"}
   ["form" {:border "1px solid red"}]])

(defstyle treatment-input-style
  [".closer" {:margin-bottom "10px"}])

(rum/defc dummy-input < rum/reactive rum/static
  [{:keys [label help-id key error] :as props} children]

  [:div {:key   key :data-key key
         :class (str "form-group ")
         :style {:vertical-align "top"
                 :width          "100%"
                 :display        "inline-block"}}

   [:div {:style {:display        "inline-block"
                  :margin-left    "10px"
                  :width          "30px"
                  :vertical-align "middle"}}
    (when help-id (small-help-button {:help-id (s/replace (lower-case help-id) " " "-")}))]

   [:div {:style {:display        "inline-block"
                  :vertical-align "middle"
                  :width          "100px"}}
    [:label.control-label {:style {:width      "100%"
                                   :text-align "left"
                                   :padding    "1px 5px"} :for (name key)} label]]

   [:div {:style {:display        "inline-block"
                  :margin-left    "10px"
                  :text-align     "left"
                  :width          "auto"                    ;"calc(100% - 150px)"
                  :vertical-align "middle"}}
    [:div {:style {:padding-left   0
                   :display        "inline-block"
                   :vertical-align "middle"}} children]]])

(rum/defc treatment-input < rum/reactive rum/static treatment-monitor
  [{:keys [ttt label help-id key]} children]
  (let [tk key]
    [:div {:key   key :data-key key
           :class (str "form-group ")
           :style {:vertical-align "top"
                   :width          "100%"
                   :display        "inline-block"}}

     [:div {:style {:display        "inline-block"
                    :vertical-align "middle"
                    :width          "100px"}}
      [:label.control-label {:style (merge {:width     "100px"
                                            :textAlign "left"
                                            :padding   "1px 5px"}
                                           (when (= :disabled (rum/react (input-cursor key)))
                                             {:color "#CCC"}))
                             :for   (name key)}
       (ttt [(keyword (str "treatment/" (name key))) label])]]

     [:div {:style {:display        "inline-block"
                    :margin-left    "10px"
                    :width          "30px"
                    :vertical-align "middle"}}
      (when help-id (small-help-button {:help-id (s/replace (lower-case (str help-id)) " " "-")}))]


     [:div {:style {:display        "inline-block"
                    :margin-left    "10px"
                    :text-align     "left"
                    :width          "auto"                  ;"calc(100% - 150px)"
                    :vertical-align "middle"
                    }}
      [:div {:style {:padding-left   0
                     :display        "inline-block"
                     :vertical-align "middle"
                     }} children]
      ]
     (cond
       (= :tra tk) [:div {:style {:color "#686868" :margin-top "0" :margin-left "155px"}}
                    (ttt [:tra/when-her2 "Available with chemotherapy when HER2 status is positive"])]
       (= :bis tk) [:div {:style {:color "#686868" :margin-top "0" :margin-left "155px"}}
                    (ttt [:bis/post-meno "Available for post-menopausal women"])]
       (= :horm tk) [:div {:style {:color "#686868" :margin-top "0" :margin-left "155px"}}
                     (ttt [:horm/erplus-1-1 "Hormone (endocrine) therapy"])
                     (when (= :h10 (rum/react (input-cursor :horm)))
                       [:span
                        (ttt [:horm/erplus-3-1 " - using data only from the"])
                        [:span {:style       {:color "#337AB7" :cursor "pointer"}
                                :on-click    #(publish help-key-change "h10-already-warning")
                                :on-key-down #(when (= "Enter" (.. % -nativeEvent -code))
                                                (publish help-key-change "h10-already-warning"))}
                         " "
                         (ttt [:horm/erplus-3-2 "tamoxifen trials"])]])
                     [:br]
                     (ttt [:horm/erplus-2 "Available when ER-status is positive"])]
       (= :delay tk)
       (when (rum/react (input-cursor :horm))
         (if (= :ys5 (rum/react (input-cursor :delay)))
           [:div
            {:style {:color "#686868" :margin-top " -15px " :margin-left " 155px "}}
            [:i.fa.fa-exclamation-triangle {:aria-hidden "true"
                                            :style       {:color         "orange"
                                                          :padding-right 5}}]
            (ttt [:tool/info-prompt-yes "Select ‘Yes’ only if 5 years of hormone therapy have already
            been received. Please enter details of other therapies received. "])

            #_(ttt [:tool/h10-only "This decision is only about whether to have an extra 5 years tamoxifen therapy.
            Please also select treatments already received in the previous 5 years. Click the info button for more help."])
            ]
           [:div
            {:style {:color "#686868" :margin-top " -15px " :margin-left " 155px "}}
            [:i.fa.fa-exclamation-triangle {:aria-hidden "true"
                                            :style       {:color         "orange"
                                                          :padding-right 5}}]
            (ttt [:tool/info-prompt-no "Select ‘No’ only if you are considering therapy options immediately after surgery."])
            ]

           )))
     ])
  )


(rum/defc labelled-treatment < rum/reactive
                               "Creates a labelled treatment input widget for the given treatment option and key"
  ([ttt label key]
   (treatment-input {:ttt     ttt
                     :label   (input-label key)             ; (if-let [lab (input-label key-n)] lab label)
                     :help-id label
                     :key     key}
                    (render-widget ttt key)))
  ([ttt help-id label key]
   (treatment-input {:ttt     ttt
                     :label   label                         ; (if-let [lab (input-label key-n)] lab label)
                     :help-id help-id
                     :key     key}
                    (render-widget ttt key))))

(rum/defc already-received-h5
  " Show already treated question if hormone selected and h10 has been selected since reset "
  [ttt horm h10-since-reset]
  (when (and (#{:h5 :h10} horm) h10-since-reset)
    (labelled-treatment ttt "Already received 5 years hormone therapy" :delay)))

(rum/defc treatments-form < rum/reactive
  [ttt]
  (let [horm (rum/react (input-cursor :horm))
        delay (= :ys5 (rum/react (input-cursor :delay)))
        hd (and horm delay)
        h10-since-reset (rum/react h10-latch-cursor)
        hormone-therapy "Hormone-therapy"                   ;(ttt [:hormone-therapy "Hormone therapy"])
        chemotherapy "Chemotherapy"                         ;(ttt [:chemotherapy "Chemotherapy"])
        trastuzumab "Trastuzumab"                           ;(ttt [:trastuzumab "Trastuzumab"])
        bisphosphonates "Bisphosphonates"                   ;(ttt [:bisphosphonates "Bisphosphonates"])
        radiotherapy "Radiotherapy"                         ;(ttt [:radiotherapy "Radiotherapy"])
        received " received"                                ;(ttt [:received " received"])
        ]
    [:form.form-horizontal
     [:div
      (if-not delay
        [:div
         (labelled-treatment ttt hormone-therapy :horm)
         (already-received-h5 ttt horm h10-since-reset)]
        [:div {:style {:position "relative"}} (labelled-treatment ttt hormone-therapy :horm-delay)
         [:div {:style {:position "absolute" :top 25 :left 145}} (ttt [:horm-delayed "for 5 more years - using data only from the "])
          [:span {:style       {:color "#337AB7" :cursor "pointer"}
                  :on-click    #(publish help-key-change "h10-already-warning")
                  :on-key-down #(when (= "Enter" (.. % -nativeEvent -code))
                                  (publish help-key-change "h10-already-warning"))}
           " "
           (ttt [:horm/erplus-3-2 "tamoxifen trials"])]]])
      (when hd
        [:div
         [:h3 "Treatments already received"]
         (already-received-h5 ttt horm h10-since-reset)])

      (labelled-treatment ttt chemotherapy
                          (str chemotherapy (if hd received nil))
                          :chemo)
      (labelled-treatment ttt trastuzumab
                          (str trastuzumab (if hd received nil))
                          :tra)
      (when (= :yes (rum/react (input-cursor :enable-bis)))
        (labelled-treatment ttt bisphosphonates
                            (str bisphosphonates (if hd received nil)) :bis))
      (when (= :yes (rum/react (input-cursor :enable-radio)))
        (labelled-treatment ttt radiotherapy
                            (str radiotherapy (if hd received nil)) :bis:radio))
      ]]))



(rum/defc treatments-options < rum/reactive
  [ttt]
  (when (rum/react results-cursor)
    (treatments-form ttt))
  )


(comment

  (live-keys-by-model model)
  (:chemo (live-keys-by-model model))
  )