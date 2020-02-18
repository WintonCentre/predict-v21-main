(ns graphics.simple-icons
  (:require [rum.core :as rum]))

(def families {:ionicon "icon "
               :fa      "fa fa-"})

(rum/defc icon
  [{:keys [family
           style]} name]
  [:i {:class (str (get families family) name)
       :style style
       :aria-hidden "true"}]
  )

(comment

  ;; selected from http://ionicons.com/
  (icon {:family :ionicon
         :font-size "18px"} "ion-man")


  ;; selected from http://fontawesome.io/icons/
  (icon {:family :fa} "bar-chart")

  )