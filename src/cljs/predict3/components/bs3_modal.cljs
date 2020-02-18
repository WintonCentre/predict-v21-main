(ns predict3.components.bs3-modal
  (:require [rum.core :as rum]
            [clojure.string :as str]
            [predict3.content-reader :refer [section]]
            [predict3.state.run-time :refer [help-key-cursor results-cursor print-cursor t-state-cursor
                                             ttt-cursor edit-cursor text-change add-language active-modal-cursor new-text-change new-text-cursor]]
            [predict3.state.load-config :refer [render-widget]]
            [predict3.results.printable :refer [results-in-print]]
            [graphics.simple-icons :as simple]
            [pubsub.feeds :refer [publish]]))

(defn some-text?
  [text]
  (when (and (some? text) (not= "" (str/trim text)))
    text))

(rum/defc editor-modal < rum/reactive                       ;(rum/local "no text yet" ::text-buffer)
  []
  (let [edit-m (rum/react edit-cursor)
        edit-key (:edit-key edit-m)
        old-text (:text edit-m)
        new-text (:new-text edit-m)
        submit (fn []
                 (if-let [new-text* (some-text? new-text)]
                   (do
                     ;(println "updating" edit-key "new-text=" new-text*)
                     (publish text-change [edit-key new-text*]))
                   old-text)
                 (.modal (js/$ "#editorModal") "hide"))]

    [:#editorModal.modal.fade {:role        "dialog"
                               :tab-index   -1
                               :aria-hidden "true"}
     [:.modal-dialog
      [:.modal-content
       [:.modal-header
        [:button.close {:type                    "button "
                        :on-click                submit
                        :aria-hidden             true
                        :dangerouslySetInnerHTML {:__html "&times;"}}]
        [:h4.modal-title (str edit-key)]]
       [:.modal-body
        [:h5 "English text"]
        old-text
        [:hr]
        [:h5 "Last translation"]
        ((rum/react ttt-cursor) [edit-key])
        [:hr]
        [:h5 "New translation"]
        [:textarea {:style     {:width "100%" :height "100%"}
                    :value     (if (nil? new-text) (if (nil? old-text) "" old-text) new-text)
                    :on-change (fn [e] (publish new-text-change [edit-key (.. e -currentTarget -value)]))}]]
       [:.modal-footer
        [:button.btn.btn-default {:type     "button"
                                  :on-click submit}
         "Close"]]]]]))

(rum/defcs new-language-modal < rum/reactive (rum/local "" ::new-lang)
  [state]
  (let [new-lang-ref (::new-lang state)
        new-lang (rum/react new-lang-ref)
        submit #(when (= 2 (count new-lang))
                  ;(println "new language is" new-lang (string? @new-lang-ref))
                  (publish add-language [new-lang])
                  (.modal (js/$ "#newLanguageModal") "hide"))]
    [:#newLanguageModal.modal.fade {:role        "dialog"
                                    :tab-index   -1
                                    :aria-hidden "true"
                                    ;:z-index 10000
                                    }
     [:.modal-dialog
      [:.modal-content
       [:.modal-header
        [:button.close {:type                    "button "
                        :on-click                submit
                        :aria-hidden             true
                        :dangerouslySetInnerHTML {:__html "&times;"}}]
        [:h4.modal-title "Add a new language"]]
       [:.modal-body
        [:h5 "Enter new language code: "]
        [:input {:value     new-lang
                 :on-change (fn [e] (reset! new-lang-ref (.. e -currentTarget -value)))}]
        [:p {:style {:color "#bbb"}} "Use the 2-character ISO 639-1 code"]
        ]
       [:.modal-footer
        [:button.btn.btn-default {:type     "button"
                                  :on-click #(.modal (js/$ "#newLanguageModal") "hide")}
         "Cancel"]
        [:button.btn.btn-default {:type     "button"
                                  :on-click submit}
         "OK"]]]]]))



(rum/defc top-modal < rum/reactive
                      "Note that we are assuming the _single_ modal dialog is mounted on #topModal since we
                      are using jQuery to locate it."
  [ttt]
  (let [help-key (rum/react help-key-cursor)
        help (section ttt help-key)
        [help-header & help-content] (if (seq help)
                                       help
                                       ["help header" [:p "help content"]])
        help-text (into [] (cons :div help-content))
        ]

    [:#topModal.modal.fade {:role        "dialog"
                            :tab-index   -1
                            :aria-hidden "true"}
     [:.modal-dialog
      [:.modal-content
       [:.modal-header
        [:button.close {:type                    "button "
                        :on-click                #(.modal (js/$ "#topModal") "hide")
                        :aria-hidden             true
                        :dangerouslySetInnerHTML {:__html "&times;"}}]
        [:h4.modal-title help-header]]
       [:.modal-body help-text]
       [:.modal-footer
        [:button.btn.btn-default {:type     "button"
                                  :on-click #(.modal (js/$ "#topModal") "hide")}
         (ttt [:close "Close"])]]]]]))


(rum/defc cancel-or-print [ttt]
  [:div.pull-right
   [:button.btn.btn-default {:type     "button"
                             :on-click #(do
                                          (.modal (js/$ "#printModal") "hide")
                                          )}
    (ttt [:cancel "Cancel"])]
   " "
   [:button.btn.btn-primary {:type     "button"
                             :on-click #(do
                                          (.modal (js/$ "#printModal") "hide")
                                          (js/print)
                                          )}

    (simple/icon {:family :fa} "print") (ttt [:print " Print"])]]
  )

(rum/defc print-modal < rum/reactive
                        "Note that we are assuming the _single_ modal dialog is mounted on #topModal since we
                        are using jQuery to locate it."
  [ttt]
  [:div
   [:#printModal.modal.fade {:role        "dialog"
                             :tab-index   -1
                             :aria-hidden "true"}
    [:.modal-dialog.screen-only
     [:.modal-content
      [:.modal-header (cancel-or-print ttt)]
      [:.modal-body (when (and (rum/react print-cursor) (rum/react results-cursor))
                      [:div
                       (rum/with-key (results-in-print ttt) 2)])]
      [:.modal-footer (cancel-or-print ttt)]]]]
   [:.print-only {::style {:margin "0 20px"}} (when (and (rum/react print-cursor) (rum/react results-cursor)) (results-in-print ttt))]])


(rum/defc settings-modal < rum/reactive
                           "Note that we are assuming a _single_ modal dialog is mounted on #settingsModal since we
                           are using jQuery to locate it."
  [ttt]

  [:#settingsModal.modal.fade {:role        "dialog"
                               :tab-index   -1
                               :aria-hidden "true"}
   [:.modal-dialog
    [:.modal-content
     [:.modal-header
      [:button.close {:type                    "button "
                      :on-click                #(.modal (js/$ "#settingsModal") "hide")
                      :aria-hidden             true
                      :dangerouslySetInnerHTML {:__html "&times;"}}]
      [:h4.modal-title "Settings"]]
     [:.modal-body
      [:p (ttt [:settings-1 "Should "]) [:strong (ttt [:settings-2 "bisphosphonates"])]
       (ttt [:settings-3 " be included as a treatment option in this tool? "])]
      (render-widget ttt :enable-bis)
      [:p (ttt [:settings-4 "If bisphosphonates are not available as a treatment in your area, you may wish to remove this treatment
      option from the tool."])]

      [:p (ttt [:settings-5 "The benefits or harms of radiotherapy are estimated from a 2011 study - see the clinician information for
       more detail. The harms and benefits we are able to present here are averaged over the whole population. They may differ
       markedly in individual cases due to factors such as radiation dose, location of the target area, and patient
       behaviours such as smoking"])]

      ;[:hr]
      #_[:p (ttt [:settings-6 "Should the tool cover "]) [:strong (ttt [:settings-7 "10 or 15 years"])]
       (ttt [:settings-8 " from surgery?"])]
      ;(render-widget ttt :ten-fifteen)
      ; (render-widget ttt :enable-dfs)

      [:hr]
      [:p (ttt [:settings-9 "Which "]) [:strong (ttt [:settings-10 "result tab"])] (ttt [:settings-11 " should appear first?"])]
      (render-widget ttt :default-tab)

      ]
     [:.modal-footer
      [:button.btn.btn-default {:type     "button"
                                :on-click #(.modal (js/$ "#settingsModal") "hide")}
       (ttt [:close "Close"])]]]]])



(comment
  (publish active-modal-change modal-key)

  )