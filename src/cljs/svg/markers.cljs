(ns svg.markers
  (:require [rum.core :as rum]
            [cljs-css-modules.macro :refer-macros [defstyle]]))

(defstyle styles
          [".open" {:fill         "#fff"
                    :stroke       "#000"
                    :stroke-width 1
                    :opacity      0.5
                    :cursor       "pointer"}
           ]
          [".filled" {:fill         "#000"
                      :stroke       "#fff"
                      :stroke-width 2
                      :opacity      0.5
                      :cursor       "pointer"}
           ]
          [".halo" {:r              10
                    :fill           "none"
                    :stroke-width   5
                    :pointer-events "none"
                    ;:stroke "#fc0"
                    :stroke         "#08f"
                    ;:opacity      0.3
                    }]
          [".inactive .hover" {:opacity 0}]
          [".active .hover" {:opacity 1}]
          )

(defn op [c-name]
  (if c-name c-name :filled))

(defn set-class
  [state key]
  #(.setAttribute (rum/dom-node state) "class" (key styles)))

(defn marker-hovers
  [state]
  (letfn [(show-hover [new-state] (reset! (::hover state) true))
          (no-hover [new-state] (reset! (::hover state) false))]
    {:on-mouse-over  show-hover
     :on-mouse-out   no-hover
     :on-mouse-leave no-hover
     :on-touch-start show-hover
     :on-touch-stop  no-hover}))

(rum/defc halo < rum/static
  [cx cy active]
  [:circle (merge {:cx      cx
                   :cy      cy
                   :opacity (if active 1 0)}
                  {:class-name (:halo styles)})])

(rum/defcs dot < (rum/local nil ::hover)
  [state r cx cy & [c-name]]
  [:g
   (when @(::hover state) (halo cx cy) true)
   (halo cx cy @(::hover state))
   [:circle (merge {:class-name ((op c-name) styles)
                    :r          r
                    :cx         cx
                    :cy         cy}
                   (marker-hovers state))]])

(defn ring [r cx cy]
  (dot r cx cy :open))

(def odot ring)

(rum/defcs square < (rum/local nil ::hover)
  [state r cx cy & [c-name]]
  [:g
   ;(when @(::hover state) (halo cx cy))
   (halo cx cy @(::hover state))
   [:g
    [:rect (merge (marker-hovers state)
                  {:class-name ((op c-name) styles)
                   :key        :mark
                   :x          (- cx r)
                   :y          (- cy r)
                   :width      (* 2 r)
                   :height     (* 2 r)})]]])

(defn osquare [r cx cy]
  (square r cx cy :open))

(rum/defcs diamond < (rum/local nil ::hover)
  [state r cx cy & [c-name]]
  [:g
   ;(when @(::hover state) (halo cx cy))
   (halo cx cy @(::hover state))
   [:g {:transform (str "translate(" cx "," cy ")")}
    [:rect (merge (marker-hovers state)
                  {:class-name ((op c-name) styles)
                   :x          (- r)
                   :y          (- r)
                   :width      (* 2 r)
                   :height     (* 2 r)
                   :transform  "rotate(45)"
                   })]]])

(defn odiamond [r cx cy]
  (diamond r cx cy :open))

(rum/defcs plus < (rum/local nil ::hover)
  [state r cx cy]
  [:g
   ;(when @(::hover state) (halo cx cy))
   (halo cx cy @(::hover state))
   [:g (marker-hovers state)
    [:line {:class-name (:open styles)
            :x1         cx
            :y1         (- cy r)
            :x2         cx
            :y2         (+ cy r)
            }]
    [:line {:class-name (:open styles)
            :x1         (- cx r)
            :y1         cy
            :x2         (+ cx r)
            :y2         cy
            }]]])

(rum/defc cross [r cx cy]
  [:g {:transform (str "translate(" cx "," cy ")")}
   [:g {:transform "rotate(45)"}
    (plus r 0 0)]])