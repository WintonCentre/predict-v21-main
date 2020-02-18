(ns predict3.state.load-config
  (:require
    [rum.core :as rum]
    [pubsub.feeds :refer [->Topic publish subscribe unsubscribe]]
    [clojure.string :refer [index-of]]
    [predict3.state.config :refer [input-groups event-bus]]
    [predict3.state.run-time :refer [event-bus
                                     rtdb
                                     input-cursor
                                     input-cursors
                                     input-change
                                     estimates
                                     ttt-cursor
                                     ]]

    [predict3.components.button :refer [radio-button-group radio-button-group-vertical information]]
    [predict3.components.select :refer [picker]]
    [wc-rum-lib.numeric-input :refer [numeric-input]]))

; Make a map of all the widget group options
(def widget-options (into {} (map (fn [g] [(:key g) g]) input-groups)))

(rum/defc default < rum/static [{:keys [key label type params]} & extra]
  [:div (pr-str "Unknown widget " key label type params extra)])

(defmulti make-widget :type)

(defmethod make-widget :default [params]
  (default params))

(defmethod make-widget :string [{:keys [params]}]
  [:div {:style {:padding-top "10px" :font-size "16px"}} params])

(defn rbg-label [label] (str label " radio button group"))

(defmethod make-widget :radio-group [{:keys [ttt key label params unknowable]}]
  (radio-button-group
    {:ttt        ttt
     :key        key
     :aria-label        (rbg-label label)
     :values            params
     :unknowable        unknowable}
    (input-cursor key)))

(defmethod make-widget :radio-group-vertical [{:keys [ttt key label params unknowable]}]
  (radio-button-group-vertical
    {:ttt        ttt
     :key        (name key)                                 ;EXPERIMENTAL
     :aria-label        (rbg-label label)
     :values            params
     :unknowable        unknowable}
    (input-cursor key)))

(defmethod make-widget :numeric-input [{:keys [key params]}]
  (numeric-input (assoc params
                   :key (name key)
                   :input-ref (input-cursor key)
                   :onChange #(publish (input-change key) %))))

(defmethod make-widget :select [{:keys [key params]}]
  (picker {:key key :on-change #(publish (input-change key) %)} (:menu params)))

(defmethod make-widget :information [{:keys [key label params unknowable ttt]}]
  (information
    {:key               key
     :aria-label        label
     :aria-described-by "info button"
     :values            (second params)
     :ttt ttt
     :ttt-key           (first params)
     :unknowable        unknowable}
    (input-cursor key))
  )


(defn render-widget
  "We're now rendering widgets at use rather than at initialisation."
  [ttt key]
  (let [options (widget-options key)
        {:keys [key label widget-type widget-params unknowable]} options
        m (assoc {:key        key
                  :label      label
                  :type       widget-type
                  :params     widget-params
                  :unknowable unknowable} :ttt ttt)]
    #_(make-widget m)
    (make-widget (assoc options
                   :type widget-type
                   :params widget-params
                   :ttt ttt))
    ))

(defn add-input-group
  "Adds the db refs on key :cursor, and mutation refs on key :change to the set of known inputs."
  [ref old-wiring groups]
  (reduce (fn [m g]
            (let [{:keys [key label read-only write-only]} g]
              (-> m
                  (assoc-in [:cursor key] (when-not write-only (rum/cursor-in ref [:widgets key])))
                  (assoc-in [:change key] (when-not read-only (->Topic key event-bus)))
                (assoc-in [:label key] label)
                  )))
          old-wiring groups))

;;
;; Create input widgets and load into state just once (per browser load)
;;
;; todo: This process is a bit naff as the app-state contains react components that cannot be shown or printed
;; or for some reason easily reloaded by figwheel. (The once only guard is there to prevent triggering of multiple mutations
;; after reloading multiple times. Maybe there's a way to clear them out on figwheel load?)
;;
;; Main benefit is that we can configure inputs using data (currently using state.config).
;; Note: Solution is probably to implement a print-method on the unprintable forms
;;
(defonce once-only-guard (swap! rtdb assoc :input-config (add-input-group rtdb {} input-groups)))
#_(swap! rtdb update :input-config #(build-input-widgets-in ttt % input-groups))


(defn live-keys-by-model
  "The (maximal) set of input-group keys present in a model. This is derived directly from the model
  field in the input configuration."
  [model]
  (into #{} (map :key (filter #(and ((:models %) model)) input-groups))))
