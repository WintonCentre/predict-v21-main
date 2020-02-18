(ns graphics.icon-array
  (:require [rum.core :as rum]
            [cljs-css-modules.macro :refer-macros [defstyle]]))

(def unit 60)
#_(defn ex [w] (str (* 10 unit w) "%"))
(defn ex [w] (str (* unit w) "px"))

(def default-icon-box {:background-color "white" :position "relative" :display "inline-block"})
(def d1-style {:color "red" :font-size (ex 0.9)})
(def d2-style {:color "blue" :font-size (ex 0.8)})
(def d3-style {:color "orange" :font-size (ex 0.5)})
(def d4-style {:color "purple" :font-size (ex 0.5)})

(defstyle prestyle
  [".icon" default-icon-box
   ["i" {:position "absolute" :top "50%" :left "50%" :transform "translate(-50%,-50%)" :z-index 1}]
   [".blend" {:background-color "rgba(230,230,230,1)"
              :mix-blend-mode   "lighten"
              :position         "absolute"
              :transform        "translateX(100%)"
              :width            "100%"
              :height           "50%"
              :z-index          2
              }]
   [".d1" d1-style]
   [".d2" d2-style]
   [".d3" d3-style]
   [".d4" d4-style]
   ])

(def block-icon-box {:background-color "rgba(100,100,0,0.1)" :display "inline-block" })

(defstyle blockstyle
  [".icon" block-icon-box
   ["i" {:position "relative" :float "left" :top "50%" :left "50%" :transform "translate(-50%,-50%)"}]
   [".d1" d1-style]
   [".d2" d2-style]
   [".d3" d3-style]
   [".d4" d4-style]
   ])

(comment

  (:icon prestyle)
  => "icon--G__33832"

  (:icon blockstyle)
  => "icon--G__33861"
  )


;;
;; Icons are published in families - each one needing a particular string prefix
;;
(def families {:ionicon "icon "
               :fa      "fa fa-"})

(rum/defc icon
  ([name]
   (icon {} name))
  ([{:keys [family theme data-class style icon-width]
     :or   {family     :ionicon
            theme      (:icon prestyle)
            data-class "d1"
            icon-width 40
            style      {:display "inline"}}
     :as   props} name]
   [:div {:class-name theme
          :style      {:width (ex 0.4) :height (ex 0.9)}}
    [:i {:class-name  (str data-class " " (family families) name " ")
         :style       style
         :aria-hidden "true"}]
    [:.blend]]))

(rum/defc scalable-icon
  ([{:keys [family theme data-class style icon-width hw-ratio box-ratio name]
     :or   {family     :ionicon
            theme      (:icon prestyle)
            data-class "d1"
            icon-width 40
            box-ratio  2.25
            hw-ratio   2.25
            }
     :as   props}]
   [:div {:class-name theme
          :style      {:width (str (* icon-width) "px")
                       :height (str (* box-ratio icon-width) "px")}}
    [:i {:key         1 :class-name (str data-class " " (family families) name " ")
         :style       (merge  style {:display "inline" :fontSize (str (* hw-ratio icon-width) "px")})
         :aria-hidden "true"}]
    [:.blend {:key 2}]]))



(rum/defc random-icons []
  [:div
   [:span {:key 1}
    (icon "ion-man")
    (icon {:data-class "d2"} "ion-woman")
    (icon "ion-woman")
    (icon {:data-class "d3" :family :fa} "female")
    (icon {:data-class "d4" :family :fa} "male")]
   [:span {:key 2}
    (icon "ion-woman")
    (icon {:data-class "d4"} "ion-man")
    (icon "ion-man")
    (icon {:data-class "d3" :family :fa :style {:color "green"}} "male")
    (icon {:data-class "d4" :family :fa} "female")]])


(def sizing-mixin
  (when (exists? js/window)
  {:will-mount   (fn [state] (assoc state ::row-width (atom 10) ::resizer (atom nil)))
   :did-mount    (fn [state]
                   (let [resize #(reset! (::row-width state) (.-clientWidth (js/ReactDOM.findDOMNode (:rum/react-component state))))]
                     (.addEventListener js/window "resize" resize)
                     (reset! (::resizer state) resize)
                     (resize)
                     state))
   :will-unmount (fn [state]
                   (.removeEventListener js/window "resize" (::resizer state))
                     state)}))

(rum/defc icon-row [icons]
  [:div {:style {:line-height 0}}
   (map-indexed #(rum/with-key %2 %1) icons)])

(rum/defc icon-array [arr]
  [:div
   (map-indexed #(rum/with-key (icon-row %2) %1) arr)])


(defn responsive-row-of-n-men [row-width n]
  (map-indexed #(rum/with-key (scalable-icon {:theme (:icon blockstyle) :icon-width (/ row-width n) :name "ion-man"}) %2) (range n)))

(rum/defcs responsive-icon-row < rum/reactive sizing-mixin [state]
  (let [row-width-atom (::row-width state)]
    [:div {:style {:width "100%"}}
     (responsive-row-of-n-men (rum/react row-width-atom) 20)]))

(comment                                                    ; later!

  ;;
  ;; Make some preferred icons
  ;;
  (def man (make-icon "ion-man"))

  )
