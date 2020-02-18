(ns predict3.pages.home
  (:require [rum.core :as rum]
            [clojure.string :refer [join]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [predict3.components.button :refer [start-button]]
            [predict3.layout.header :refer [header footer]]
            [predict3.content-reader :refer [section]]
            [predict3.state.run-time :refer [route-change ttt-cursor]]
            [predict3.results.util :refer [alison-blue-1 alison-blue-2]]
            [graphics.simple-icons :refer [icon]]
            [pubsub.feeds :refer [publish]]
            [interop.utils :refer [scrollTo]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            ))

(rum/defc centred-block [ttt {:keys [icon section-id extra]}]

  (let [[title & content] (section ttt section-id)]
    [:div.text-center {:key   section-id
                       :style {:display "inline-block" :margin-top 0 :margin-left 15 :width "100%"}}
     [:div {:key 1 :style {:height "70px" :margin-top 20}} icon]
     [:h3 {:key 2} title]
     [:div {:key 3} content]
     (while extra extra)]))

(rum/defc home < rum/static rum/reactive [ttt]

  [:.container-fluid
   (header ttt)
   [:#main-content.row {:tab-index -1
                        :style     {:margin-left  -30
                                    :margin-right -30}}
    [:.col-xs-12
     [:div {:style {:position         "relative"
                    :width            "100%"
                    :background-color alison-blue-1
                    }}
      [:div {:style {:position   "absolute"
                     :width      "100%"
                     :top        0
                     :bottom     "20%"
                     :opacity    0.25
                     :background "linear-gradient(rgba(255,255,255,0), #fff)"
                     }}]
      [:.row
       [:.col-sm-5.col-sm-offset-1
        [:h1.alison-blue-2 {:style {:margin "15px" :margin-top 30}} (ttt [:home/what-is-predict "What is Predict?"])]
        (let [[title [el1 _ p1] [el2 _ p2]] (section ttt "home-what-is")]
          [:div
           [el1 {:style {:font-size 20 :margin-left 15}} p1]
           [el2 {:style {:font-size 14 :margin-left 15}} p2]])
        (start-button ttt)

        [:p {:style {:margin-left 15}} [:i (ttt [:home/dymtv "Did you mean to visit "])] [:a {:href "https://prostate.predict.nhs.uk"} "Predict Prostate"] (ttt [:home/qmark "?"])]
        ]
       [:.col-sm-6
        [:img.img-responsive {:src         "/assets/icon-imagery.png"
                              :alt         "banner imagery"
                              :aria-hidden true
                              :style       {:margin-left "5%" :width "90%"}}]]]]]]
   [:.row {:style {:margin "0px -30px 15px"}}
    [::.col-sm-10.col-sm-offset-1
     [:.row

      [:.col-md-4
       (rum/with-key (centred-block ttt {:icon       [:img {:src         "/assets/graph-icon.png"
                                                            :alt         "patient icon"
                                                            :aria-hidden true
                                                            :style       {:margin-top 20 :margin-left 15}}]
                                         :section-id "home-how-use"}) 1)]
      [:.col-md-4
       (rum/with-key (centred-block ttt {:icon       [:img {:src         "/assets/patient-icon.png"
                                                            :alt         "graph icon"
                                                            :aria-hidden true
                                                            :style       {:margin-top 20 :margin-left 15}}]
                                         :section-id "home-what-tell"}) 2)]
      [:.col-md-4
       [:div.text-center
        (rum/with-key (centred-block ttt {:icon       [:img {:src         "/assets/book-icon.png"
                                                             :alt         "book icon"
                                                             :aria-hidden true
                                                             :style       {:margin-top 20 :margin-left 15}}]
                                          :section-id "home-more-info"}) 3)
        [:.visible-sm-block (start-button ttt)]
        ]]

      ]]]
   (editor-modal)
   (scrollTo 0)
   (footer ttt)
   ])


(comment
  {:style {:border "1px solid grey" :padding "10px" :height "370px"}}
  {:style {:border "1px solid grey" :padding "10px" :height "180px"}}
  [:div
   [:button.btn.btn-primary.btn-lg. {:style    {:margin-right  "5px"
                                                :margin-bottom "5px"}
                                     :on-click #(publish route-change [:tool nil nil])}


    (icon {:family :ionicon} "ion-stats-bars") " Predict Tool"]

   [:button.btn.btn-default.btn-lg {:style    {:margin-right  "5px"
                                               :margin-bottom "5px"}
                                    :on-click #(publish route-change [:patient nil nil])}

    (icon {:family :ionicon} "ion-ios-information-outline") " Patient Information"]]
  )