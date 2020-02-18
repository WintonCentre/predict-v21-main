(ns predict3.pages.root
  (:require                                                 ;[org.martinklepsch.derivatives :refer [rum-derivatives]]
    [rum.core :as rum]
    [predict3.state.run-time :refer [rtdb route media-change media-cursor t-state-cursor ttt-cursor edit-cursor edit-change text-change]]
    [predict3.pages.home :refer [home]]
    [predict3.pages.about :refer [about]]
    [predict3.pages.legal :refer [legal]]
    [predict3.pages.contact :refer [contact]]
    [predict3.pages.predict-v2 :refer [v2]]
    [predict3.pages.not_found :refer [not-found]]
    [pubsub.feeds :refer [->Topic publish subscribe unsubscribe]]
    [predict3.components.bs3-modal :refer [editor-modal new-language-modal]]
    [translations.root :refer [ttt-edit]]
    ))

(defn before-print []
  (publish media-change :print)
  )

(defn after-print []
  (publish media-change :screen))

(defn before-after-print []
  (goog.object.set js/window "onbeforeprint" before-print)
  (goog.object.set js/window "onafterprint" after-print))

(def media-watch {:did-mount (fn [state]
                               ; chrome matchMedia is dodgy, and we detect it through its addEventListener prototype
                               (if-not (.-oldBrowser js/window)
                                 (if (.-addEventListener (.matchMedia js/window "print"))
                                   (before-after-print)     ; chrome
                                   (let [mql (.matchMedia js/window "print")]
                                     (.addListener mql #(if (goog.object.get % "matches") (before-print) (after-print))))) ; firefox
                                 (before-after-print)       ; ie9 (but this is failing...)
                                 )

                               state)})


(rum/defc root < rum/reactive media-watch
                 "Root of site. All components are within this tree"
  []
  (let [[page params query :as rt] (rum/react route)
        ttt (rum/react ttt-cursor)
        ]
    [:div
     (case page
       :home (home ttt-edit)
       :about (about ttt-edit rt)
       :tool (v2 ttt-edit)
       :contact (contact ttt-edit rt)
       :legal (legal ttt-edit rt)
       :not-found (not-found ttt)
       )
     ]))