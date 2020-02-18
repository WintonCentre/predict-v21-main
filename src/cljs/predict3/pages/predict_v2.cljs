(ns predict3.pages.predict-v2
  (:require [rum.core :as rum]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [predict3.components.bs3-modal :refer [top-modal editor-modal settings-modal print-modal
                                                   new-language-modal]]
            [predict3.components.button :refer [settings-button print-button]]
            [bide.core :as r]
            [graphics.simple-icons :as simple]
            [predict3.router :refer [router]]
            [predict3.content-reader :refer [section all-subsections]]
            [predict3.layout.input-panels :refer [inputs-row]]
            [predict3.layout.treatments-panel :refer [treatments-options]]
            [predict3.layout.result-panel :refer [results]]
            [predict3.layout.header :refer [header header-banner footer]]
            [predict3.state.mutations :refer [clear-inputs]]
            [predict3.state.run-time :refer [input-cursor results-cursor help-key-change media-cursor
                                             hide-warning-change hide-warning-cursor
                                             route-change print-change]]
            [predict3.results.util :refer [alison-blue-1 alison-blue-2 alison-blue-3 alison-blue-4 alison-blue-5]]
            [interop.bundled :refer [on-screen?]]
            [pubsub.feeds :refer [publish]]
            [interop.utils :refer [scrollTo]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            ))

;;;
;; Main layout
;;;

(rum/defc results-footer < rum/reactive
  [ttt]
  (let [results (rum/react results-cursor)
        results? (and results (not (empty? results)))]
    (when results?
    [:.row {:style {:background-color alison-blue-1
                    :margin-top       20
                    :padding-bottom   20}}

     [:.col-md-6.col-md-offset-1
        (all-subsections ttt "tool-postamble")]
     [:.col-md-4.text-center {:style {:margin-top "20px"}}
      [:img {:src         "/assets/faq-icon.png"
             :alt         "faq icon"
             :aria-hidden true}]
      [:h3 "Looking for advice?"]
      [:button.btn.btn-primary.btn-lg
       {:on-click #(do
                     (scrollTo 0)
                     (publish route-change [:about {:page :faqs} nil])
                     )}
       "See the FAQs"]
      ]]
    [:button.btn.screen-only {:type        "button"
                              :on-click    #(publish print-change "print")
                              :on-key-down #(when (= "Enter" (.. % -nativeEvent -code))
                                              (publish print-change "print"))
                              :style       {:width                     70
                                            :opacity                   0.5
                                            :position                  "fixed"
                                            :top                       300
                                            :right                     -1
                                            :color                     "#ffffff"
                                            :background-color          "#444466"
                                            :font-size                 16
                                            :padding                   "15px 5px 15px 5px"
                                            :border-top-left-radius    10
                                            :border-bottom-left-radius 10
                                              }} (simple/icon {:family :fa} "print") (ttt [:tool/print-button-label " Print"])]
      )))

(rum/defc treatments-with-results < rum/reactive [ttt]
  (let [r (rum/react results-cursor)
        dcis (rum/react (input-cursor :dcis))]
    (when (not= :yes dcis)
      (if (or (not (seq r)) (nil? r))
        [:.row
         [:.col-sm-10.col-sm-offset-1.col-xs-12
          [:div {:style {:background-color alison-blue-1
                         :padding          "10px 10px 3px 10px"
                         :margin-bottom    20}}
           [:div {:style {:color     alison-blue-2
                          :font-size "20px"}}

            [:p {:style {:padding-bottom 0}}
             (simple/icon {:family :fa :style {:font-size 35 :padding-right 8}} "info-circle")

             (ttt [:tools/results-appear " Treatment options and results will appear here when you have filled in all the information needed above."])]]]]]
        [:div
         [:.row
          [:.col-md-6.clearfix
           [:h3 (ttt [:tool/tr-options "Treatment Options"])]
           (treatments-options ttt)

           #_[:.hidden-xs.hidden-sm
              (print-button)]
           ]
          [:.col-md-6.screen-only
           (results {:ttt ttt :printable (= :print (rum/react media-cursor))})]

          #_[:.hidden-md.hidden-lg
             (print-button)]]

         ]))))

(rum/defc v2 [ttt]
  (let [[_ & preamble] (section ttt "tool-preamble")]
    [:.container-fluid
     (header ttt)
     #_(header-banner ttt "tool-preamble")
     [:#main-content.row {:tab-index -1
                          :style     {:margin-left  -30
                                      :margin-right -30}}
      [:.col-xs-12
       [:div {:style {:position         "relative"
                      :width            "100%"
                      :background-color alison-blue-1
                      :overflow         "hidden"
                      }}
        [:div {:style {:position   "absolute"
                       :width      "100%"
                       :top        0
                       :bottom     "20%"
                       :opacity    0.25
                       :background "linear-gradient(rgba(255,255,255,0), #fff)"
                       }}]
        [:.row.screen-only
         [:.col-sm-4.col-sm-offset-2
          [:img.img-responsive {:src         "/assets/tool-banner.png"
                                :alt         "Predict tool banner imagery"
                                :aria-hidden true
                                :style       {:margin-left "5%" :zoom 0.7}}]]
         [:.col-sm-6
          [:.row
           [:.col-xs-8
            preamble
            #_[:p {:style {:margin "40px 20px 20px" :font-size "20px"}}
               ]]
           [:.col-xs-4
            [:div {:style {:margin "40px 0 0 0px"}} (settings-button ttt)]]]]]
        [:.row.print-only
         [:.col-sm-10.col-sm-offset-1
          preamble]]
        ]]]
     [:.row.screen-only
      [:.col-md-10.col-md-offset-1
       [:.row {:key 2}
        [:.col-xs-12 {:style {:margin-bottom 20}}
         (inputs-row ttt)]]]]
     [:.row.screen-only
      [:.col-sm-12 {:style {:background-color alison-blue-4}}
       [:.row {:key 3}
        [:.col-sm-10.col-sm-offset-1 {:key 2}
         (treatments-with-results ttt)
         ]]]]
     (scrollTo 0)
     [:.screen-only
      (results-footer ttt)
      (footer ttt)]
     (top-modal ttt)
     (settings-modal ttt)
     (print-modal ttt)
     (new-language-modal)
     (editor-modal)
     ]))





