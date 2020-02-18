(ns svg.space
  (:require [rum.core :as rum]
            [svg.scales :refer [nice-linear]]
            ))

(defn space [outer margin padding x-domain x-ticks y-domain y-ticks N]
  (let [inner {:width  (- (:width outer) (:left margin) (:right margin))
               :height (- (:height outer) (:top margin) (:bottom margin))}
        width (- (:width inner) (:left padding) (:right padding))
        height (- (:height inner) (:top padding) (:bottom padding))]
    {:outer   outer
     :inner   inner
     :margin  margin
     :padding padding
     :width   width
     :height  height
     :x       (nice-linear x-domain [0 width] x-ticks)
     :y       (nice-linear y-domain [height 0] y-ticks)
     :N       N
     }))
