(ns predict3.components.bs-mixins)

;;
;; Rum mixins to enable bootstrap popovers and tooltips
;;

(defn ready [handler]
  (.ready (js/$ js/document) handler))

(def tt-mixin {:did-mount (fn [state]
                            (ready
                              #(do (.popover (js/$ "[data-toggle=\"popover\"]"))
                                   (.tooltip (js/$ "[data-toggle = \"tooltip\"]"))))
                            state)
               })

(def bs-popover
  {:did-mount (fn [state]
                (ready
                  #(.popover (js/$ "[data-toggle=\"popover\"]")))
                state)
   })


(def dont-manage
  {:should-update (fn [old-state state] false)})
