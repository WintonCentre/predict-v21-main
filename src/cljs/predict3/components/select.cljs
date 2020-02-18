(ns predict3.components.select
  (:require [rum.core :as rum]
            [predict3.state.run-time :refer [input-cursor input-change]]
            [pubsub.feeds :refer [publish]]
            [clojure.pprint :refer [cl-format]]
            ))

;;
;; This requires jQuery to work
;;
(def picknmix {:did-mount    (fn [state]
                               (let [props (first (:rum/args state))
                                     sp (js/$ (str "#" (name (:key props)) ".selectpicker"))
                                     handler (:on-change props)]
                                 (.on sp "change" #(handler (if (= "" (.val sp)) nil (.val sp))))
                                 (.selectpicker (.addClass sp "col-md-11") "setStyle")
                                 (.selectpicker sp "show"))
                               state)
               :will-unmount (fn [state]
                               (let [props (first (:rum/args state))
                                     sp (js/$ (str "#" (name (:key props)) ".selectpicker"))]
                                 (.off sp "change")
                                 (.selectpicker sp "destroy"))
                               state)
               })

(rum/defc picker < picknmix rum/static [{:keys [key value class-name on-change :as props]} menu]

  ;;
  ;; Cannot add {:value value into the following without causing a react error.
  ;; So it may be tricky to restore selected value from the database if we ever need to.
  ;;
  ;; doubly nested divs needed for error-state CSS. Do not remove !!!
  ;;
  [:div [:div
         (when key [:select {:id (name key) :class-name (str "selectpicker " class-name)}
                    (map-indexed (fn [idx opt]
                                   (if (vector? opt)
                                     (let [[value text] opt]
                                       [:option {:key idx :value (name value)} text])
                                     [:option {:key idx} opt])) menu)
                    ])]])
