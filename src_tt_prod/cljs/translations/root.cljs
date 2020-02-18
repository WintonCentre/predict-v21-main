(ns translations.root
  (:require [rum.core :as rum]
            (predict3.state.run-time :refer [t-state-cursor ttt-cursor edit-change])
            (pubsub.feeds :refer [publish])))


(defn _ttt
  [ttt arg]
  (let [text-or-v (ttt arg)
        text (if (vector? text-or-v)
               (if (> (count text-or-v) 1)
                 (second text-or-v)
                 (if (> (count text-or-v) 0)
                   (str (first text-or-v))
                   "**argv**")
                 )
               text-or-v)
        lang (:lang (rum/react t-state-cursor))]
    [lang text]
    ))

; prod mode
(rum/defc ttt-edit < rum/reactive
          [arg]
          (let [[lang text] (_ttt (rum/react ttt-cursor) arg)]
            [:span text])
          )
