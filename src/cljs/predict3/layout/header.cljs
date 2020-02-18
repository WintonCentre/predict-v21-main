(ns predict3.layout.header
  (:require [rum.core :as rum]
            [bide.core :as r]
            [predict3.router :refer [router]]
            [predict3.state.run-time :refer [input-cursor route-change ttt-cursor]]
            [predict3.components.bs3-navbar :refer [hamburger-navbar]]
            [predict3.components.button :refer [start-button settings-button]]
            [predict3.content-reader :refer [section]]
            [predict3.results.util :refer [NHS-blue alison-blue-1 alison-blue-2 alison-blue-1]]
            [interop.utils :refer [scrollTo]]
            [pubsub.feeds :refer [publish]]
            ))

(rum/defc header-banner [ttt banner-id]
  (let [[_ & preamble] (section ttt banner-id)]
    [:.row {:style {:margin-left  -30
                    :margin-right -30}}
     [:.col-xs-12
      [:div {:style {:position         "relative"
                     :width            "100%"
                     :background-color alison-blue-1}}

       [:div {:style {:position   "absolute"
                      :width      "100%"
                      :top        0
                      :bottom     "20%"
                      :opacity    0.25
                      :background "linear-gradient(rgba(255,255,255,0), #fff)"}}]

       [:.row.screen-only
        [:.col-sm-12
         [:div {:style {:background-color alison-blue-1
                        :height           30
                        :width            "100%"}}]]]]]]))


(rum/defc footer-banner [ttt]
  [:.row.screen-only {:style {:background-color alison-blue-1
                              :padding-top      20
                              :padding-bottom   20
                              :margin-top       20
                              :margin-bottom    0}}
   [:.col-md-3.col-md-offset-2.text-center
    [:img {:src "/assets/tool-icon.png" :alt "tool-icon" :aria-hidden true}]
    [:h3 (ttt [:footer/wtup-1 "Want to use Predict?"])]
    [:p (ttt [:footer/wtup-2 "This tool helps to understand how treatments for breast cancer can improve survival rates after surgery."])]
    (start-button ttt)
    ]
   [:.col-md-3..col-md-offset-2.text-center {:style {:margin-top "20px"}}
    [:img {:src "/assets/faq-icon.png" :alt "faq-icon" :aria-hidden true}]
    [:h3 (ttt [:footer/sttt-1 "Someone to talk to?"])]
    [:p (ttt [:footer/sttt-2 " if you are fighting cancer, it’s often easier with support. Here, you can find further information and links."])]
    [:button.btn.btn-danger.btn-lg
     {:on-click #(do
                   (publish route-change [:about {:page :faqs} nil])
                   (scrollTo 0)
                   )}
     (ttt [:footer/support "Support Links"]) #_"See the FAQs"]
    ]])

(defn skip-to [content-id]
  [:.row.screen-only
   [:.col-sm-11.col-xs-12.skip
    [:a.pull-right {:tab-index    -1
                    :style        {:cursor "pointer"}
                    :on-key-press #(if (= (.. % -nativeEvent -key) "Enter")
                                     (do (.focus (js/$ content-id)) false))
                    :on-click     #(do (.focus (js/$ content-id)) false)} "Skip to main content"]]])


(rum/defc header
  "Render header"
  [ttt]
  ;
  ; Do not convert the reactive ttt to a passed parameter, as the navbar fails when given a react component rather than
  ; a function


  ;(banner)
  [:.row
   [:.col-sm-10.col-sm-offset-1.col-xs-12
    [:img.img-responsive.pull-right {:src   "/assets/NHS.jpg"
                                     :alt   "NHS logo"
                                     :style {:width         "85px"
                                             :margin-top    "30px"
                                             :margin-bottom "38px"}}]
    [:img.img-responsive {:src   "/assets/logo-pos-al.png"
                          :alt   "Predict breast cancer logo"
                          :style {:width         "180px"
                                  :margin-top    "13px"
                                  :margin-bottom "6px"
                                  }}]]
   [:.col-xs-12
    (skip-to "#main-content")
    (hamburger-navbar ttt)
    ]])

(rum/defc footer
  ([ttt] (footer ttt nil))
  ([ttt preview]
   "Site footer"
   (let [mw 200]
     [:div {:style {:margin-top 20}}
      ;(when preview {:class-name "print-only"})
      [:.row.print-only
       [:.col-xs-12
        [:img {:src "/assets/print-footer.png"}]]]
      [:.row.screen-only {:style {:clear "both"
                                  :color "white"
                                  }}
       [:.col-sm-12
        {:style {
                 :min-height    "200px" :background-color alison-blue-2
                 :border-radius "0px"
                 :margin-left   "0px"
                 :margin-right  "0px"
                 :padding       "60px"
                 :z-index       "1"}}
        [:.row
         [:.col-sm-4
          [:img.img-responsive.screen-only {:src   "/assets/white-logo.png"
                                            :alt   "Predict breast cancer logo"
                                            :style {:margin-top   10
                                                    ;:margin-bottom "3ex"
                                                    :margin-left  "auto"
                                                    :margin-right "auto"
                                                    :max-width    "175px" #_"90%"}
                                            #_{:margin-bottom "3ex"
                                               :max-width     mw}}]
          [:img.img-responsive.print-only {:src   "/assets/logo-pos-al.png"
                                           :alt   "Predict breast cancer logo"
                                           :style {:margin-bottom "3ex"
                                                   :max-width     mw}}]]

         [:.col-sm-4
          [:a.screen-only {:href "https://wintoncentre.maths.cam.ac.uk"}
           [:img.img-responsive {:src   "/assets/ucs-winton-transparent.png"
                                 :alt   "Winton Centre logo"
                                 :style {:margin-top   10
                                         :margin-left  "auto"
                                         :margin-right "auto"
                                         ;:margin-bottom "3ex"
                                         :max-width    "175px"}
                                 #_{:margin-bottom "3ex"
                                    :max-width     mw
                                    :cursor        mw}}]]
          [:img.img-responsive.print-only {:src   "/assets/ucs-winton-blue.png"
                                           :alt   "Winton Centre logo"
                                           :style {:margin-bottom "3ex"
                                                   :max-width     mw
                                                   :cursor        mw}}]]
         [:.col-sm-4
          [:img.img-responsive.screen-only {:src   "/assets/phe-neg.png"
                                            :alt   "Public Health England logo"
                                            :style {:margin-top   10
                                                    :margin-left  "auto"
                                                    :margin-right "auto"
                                                    :max-width    "120px" #_"70%"}}]
          [:img.img-responsive.print-only {:src   "/assets/phe-pos.png"
                                           :alt   "Public Health England logo"
                                           :style {:margin-bottom "4ex"
                                                   :max-width     mw}}]]

         ]]
       [:.row.copy-footer-container
        [:.col-lg-9.col-md-6.col-md-offset-3.col-lg-offset-1.copy-footer
         (str "Copyright Ⓒ " (.getFullYear (js/Date.)) " University of Cambridge. All Rights Reserved | ")
         [:a {:on-click #(publish route-change [:legal {:page "privacy"} nil]) :href "javascript:void(0)"} "Privacy & Data Protection"]
         " | "
         [:a {:on-click #(publish route-change [:legal {:page "disclaimer"} nil]) :href "javascript:void(0)"} "Disclaimer"]
         ]
        [:.col-lg-2.col-md-3.build-version
         "Build: v0.0-dev-#000-hash"
         ]
        ]
       ]])))