(ns svg.axis
  (:require [rum.core :as rum]
            [clojure.string :as s]
            [cljs.pprint :refer [cl-format]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [svg.scales :refer [->Identity ->Linear in out i->o o->i tick-format-specifier]]))

(def default-axis-color "#888")

(defstyle default-axis-style
          [".axis" {:stroke-width "0.15ex"
                    ;:shape-rendering "crispEdges"        ; may help sometimes
                    :font-size    "12pt"}
           ["line" {:stroke default-axis-color}]
           [".tick"
            ["line" {:stroke default-axis-color}]
            ["text" {;:stroke "none"
                     :fill default-axis-color}]]])

(rum/defc axisBottom [{:keys [scale ticks format-specifier styles]
                       :or   {scale  (->Identity [0 1] 10)
                              ticks  (range 0 1 0.1)
                              styles default-axis-style}}]
  (let [[x1 x2] (in scale)
        x (i->o scale)
        specifier (if (nil? format-specifier) (tick-format-specifier scale) format-specifier)]
    [:g {:key        "axis-b"
         :class-name (:axis styles)}
     [:line {:key "X"
             :x1  (x x1)
             :y1  0
             :x2  (x x2)
             :y2  0
             }]

     (for [tick ticks]
       [:g {:key   (gensym "K")
            :class "tick"}
        [:line {:key 1
                :x1  (x tick) :y1  0
                :x2  (x tick) :y2  "0.5ex"}]
        [:text {:key         2
                :x           (x tick)
                :dx          0
                :dy          "2.2ex"
                :text-anchor "middle"} (cl-format nil specifier tick)]])]))

(rum/defc axisTop [{:keys [scale ticks format-specifier styles]
                       :or   {scale  (->Identity [0 1] 10)
                              ticks  (range 0 1 0.1)
                              styles default-axis-style}}]
  (let [[x1 x2] (in scale)
        x (i->o scale)
        specifier (if (nil? format-specifier) (tick-format-specifier scale) format-specifier)]
    [:g {:key        "axis-b"
         :class-name (:axis styles)}
     [:line {:key "X"
             :x1  (x x1)
             :y1  0
             :x2  (x x2)
             :y2  0
             }]

     (for [tick ticks]
       [:g {:key   (gensym "K")
            :class "tick"}
        [:line {:key 1
                :x1  (x tick)
                :y1  0
                :x2  (x tick)
                :y2  "-0.5ex"
                }]
        [:text {:key         2
                :x           (x tick)
                :dx          0
                :dy          "-1ex"
                :text-anchor "middle"} (cl-format nil specifier tick)]])]))

(rum/defc axisLeft [{:keys [scale ticks format-specifier styles]
                     :or   {scale  (->Identity [0 1] 10)
                            ticks  (range 0 1 0.1)
                            styles default-axis-style}}]
  (let [[y1 y2] (in scale)
        y (i->o scale)
        specifier (if (nil? format-specifier) (tick-format-specifier scale) format-specifier)]
    [:g {:key        "axis-l"
         :class-name (:axis styles)}
     [:line {:key "Y"
             :x1  0
             :y1  (y y1)
             :x2  0
             :y2  (y y2)}]

     (for [tick ticks]
       [:g {:key   (gensym "K")
            :class "tick"}
        [:text {:key         2
                :x           0
                :y           (y tick)
                :dx          "-0.7ex"
                :dy          "0.5ex"
                :text-anchor "end"
                } (cl-format nil specifier tick)]
        [:line {:key 1
                :x1  0
                :y1  (y tick)
                :x2  "-0.5ex"
                :y2  (y tick)}]])]))

(rum/defc axisRight [{:keys [scale ticks format-specifier styles]
                      :or   {scale  (->Identity [0 1] 10)
                             ticks  (range 0 1 0.1)
                             styles default-axis-style}}]
  (let [[y1 y2] (in scale)
        y (i->o scale)
        specifier (if (nil? format-specifier) (tick-format-specifier scale) format-specifier)]
    [:g {:key        "axis-l"
         :class-name (:axis styles)}
     [:line {:key "Y"
             :x1  0
             :y1  (y y1)
             :x2  0
             :y2  (y y2)}]

     (for [tick ticks]
       [:g {:key   (gensym "K")
            :class "tick"}
        [:text {:key         2
                :x           0
                :y           (y tick)
                :dx          "0.7ex"
                :dy          "0.5ex"
                :text-anchor "start"} (cl-format nil specifier tick)]
        [:line {:key 1
                :x1  0
                :y1  (y tick)
                :x2  "0.5ex"
                :y2  (y tick)}]])]))
