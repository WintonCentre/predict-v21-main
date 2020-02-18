(ns predict3.core
  (:require [rum.core :as rum]
            [cljs.core.async :refer [chan]]
            [goog.dom :as gdom]
            [predict3.state.mutations :refer [mutator]]
            [predict3.state.run-time :refer [t-state-change t-state-cursor]]
            [translations.config :refer [live-dictionary-url]]
            [predict3.pages.root :refer [root]]
            [pubsub.feeds :refer [publish]]
            [translations.tongue-base :refer [load-translations* load-all-translations]]
            [translations.config :refer [predict-edit]]
            [translations.tranny-api :refer [base-url]]
            ))


; We are getting an infer warning on accessing .modal property in bootstrap
(set! *warn-on-infer* false)

(enable-console-print!)

;;;; Start the mutator!
(defonce once-only-guard (mutator))

; Global channels for response handling on startup
(def ok-chan (chan 0))
(def err-chan (chan 0))
(def static-chan (chan 0))                                  ; It may be safe to re-use ok-chan here

(defn main []
  ;; conditionally start the app based on whether the #main-app-area
  ;; node is on the page
  (rum/mount (root) (gdom/getElement "app"))

  ;(println "predict-edit" predict-edit (str base-url "upserts/all"))

  (if predict-edit
    (load-all-translations static-chan ok-chan err-chan (str base-url "upserts/all") t-state-cursor)
    (publish t-state-change live-dictionary-url)))          ; will call load-translation*

(defonce loaded-id (js/setInterval
                     #(when (#{"loaded" "complete"} (.-readyState js/document))
                        (js/clearInterval loaded-id)
                        (main))
                     10))


#_(main)
