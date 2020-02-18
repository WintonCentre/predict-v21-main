

(ns svg.mixins
  (:require [rum.core :as rum]))

(defn patch-svg-attrs [m]
  "a :did-mount mixin to patch in any svg
   attributes that ReactJS fails to implement"
  (fn [state]
    (doseq [[attr value] m]
      (.setAttribute (rum/dom-node state) attr value))
    state))

