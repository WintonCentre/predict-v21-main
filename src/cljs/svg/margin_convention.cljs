(ns svg.margin-convention
  (:require [rum.core :as rum]
            [clojure.string :as s]
            [cljs.pprint :refer [cl-format]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [svg.axis :refer [axisBottom axisTop axisLeft axisRight]]
            [svg.scales :refer [->Identity nice-linear i->o o->i in out ticks]]
            [svg.mixins :refer [patch-svg-attrs]]
            ))

(defstyle styles
          [".outer" {:fill   "none"
                     :stroke "#000"}]
          [".inner" {:fill             "#ccc"
                     :stroke           "#000"
                     :stroke-width     0.5
                     :stroke-dasharray "3, 4"}]
          [".annotation" {
                          :font-size "10pt"
                          }]
          [".arrow" {
                     :stroke       "#000"
                     :stroke-width "1.5px"
                     }])

(def patch-marker {:did-mount (patch-svg-attrs {"refX" 10
                                                "refY" 5
                                                "markerWidth" 6
                                                "markerHeight" 6
                                                "orient" "auto"})})

(rum/defc start-marker < patch-marker []
  [:marker {:id            "triangle-start"
            :view-box      "0 0 10 10"
            :ref-X         10                               ; react discards this
            :ref-Y         5                                ; react discards this
            :marker-width  6                                ; react discards this
            :marker-height 6                                ; react discards this
            :orient        "auto"                           ; react discards this
            }
   [:path {:d "M 0 0 L 10 5 L 0 10 z"}]])

(rum/defc end-marker < patch-marker []
  [:marker {:id            "triangle-end"
            :view-box      "0 0 10 10"
            :ref-X         10                               ; react discards this
            :ref-Y         5                                ; react discards this
            :marker-width  6                                ; react discards this
            :marker-height 6                                ; react discards this
            :orient        "auto"                           ; react discards this
            }
   [:path {:d "M 0 0 L 10 5 L 0 10 z"}]])

(rum/defc margins [{:keys [outer margin inner padding width height x y]}]
  (let [inner (if (nil? inner) {:width  (- (:width outer) (:left margin) (:right margin))
                                :height (- (:height outer) (:top margin) (:bottom margin))}
                               inner)
        width (if (nil? width) (- (:width inner) (:left padding) (:right padding)) width)
        height (if (nil? height) (- (:height inner) (:top padding) (:bottom padding)) height)
        x (if (nil? x) (->Identity [0 width] 10) x)
        x-ticks (ticks x)                                   ;(if (nil? x-ticks) (ticks 0 width 10) x-ticks)
        y (if (nil? y) (->Identity [0 height] 10) y)
        y-ticks (ticks y)                                   ;(if (nil? y-ticks) (ticks 0 height 5) y-ticks)
        ]

    [:svg {:width  (:width outer)
           :height (:height outer)}

     [:g {:key       0
          :transform (str "translate(" (:left margin) ", " (:top margin) ")")}

      [:defs {:key 0}
       (rum/with-key (start-marker) 0)
       (rum/with-key (end-marker) 1)]

      [:rect {:key        1
              :class-name (:outer styles)
              :width      (:width inner)
              :height     (:height inner)}]

      ;;
      ;; define the coordinate system
      ;;
      [:g {:key       2
           :transform (str "translate(" (:left padding) "," (:top padding) ")")}
       [:rect {:key        1
               :class-name (:inner styles)
               :width      width
               :height     height}]

       ;; test axes on all edges
       [:g {:key       "bottom"
            ;:class-name ".xaxis"
            :transform (str "translate(0," (+ (first (out y)) 10) ")")}
        (axisBottom {:scale x :ticks x-ticks})]
       [:g {:key       "top"
            :transform (str "translate(0," (- (second (out y)) 10) ")")}
        (axisTop {:scale x :ticks x-ticks})]
       [:g {:key       "left"
            :transform (str "translate(" (- (first (out x)) 10) ",0)")}
        (axisLeft {:scale y :ticks y-ticks})]
       [:g {:key       "right"
            :transform (str "translate(" (+ (second (out x)) 10) ",0)")}
        (axisRight {:scale y :ticks y-ticks})]

       [:text {:key "note"
               :class-name (:annotation styles)
               :x          "-30px"
               :y          "-40px"}
        "translate by (" (:left padding) ", " (:top padding) ")"]
       ]

      ;; add in arrows
      [:g {:key 3}
       [:line {:key 0
               :class-name (:arrow styles)
               :x2         (:left padding)
               :y2         (:top padding)
               :marker-end "url(#triangle-end)"}]
       [:line {:key 1
               :class-name (:arrow styles)
               :x2         (/ (:width inner) 2)
               :x1         (/ (:width inner) 2)
               :y2         (- (:height inner) (:bottom padding))
               :y1         (:height inner)
               :marker-end "url(#triangle-end)"}]
       [:line {:key 2
               :class-name (:arrow styles)
               :x2         (:left padding)
               :y1         (/ (:height inner) 2)
               :y2         (/ (:height inner) 2)
               :marker-end "url(#triangle-start)"}]
       [:line {:key 3
               :class-name (:arrow styles)
               :x1         (:width inner)
               :x2         (- (:width inner) (:right padding))
               :y1         (/ (:height inner) 2)
               :y2         (/ (:height inner) 2)
               :marker-end "url(#triangle-end)"}]
       [:text {:key 4
               :class-name (:annotation styles)
               :x          0
               :y          -8} "origin"]
       [:circle {:key 5
                 :class-name (:origin styles)
                 :r          4.5}]
       ]]]))

