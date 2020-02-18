(ns predict3.state.run-time
  (:require
    [rum.core :as rum]
    [clojure.string :refer [index-of starts-with?]]
    [clojure.pprint :refer [cl-format]]
    [clojure.set :refer [union]]
    [predict3.state.config :refer [initial-t-state]]
    [pubsub.feeds :refer [->Topic create-feed]]
    ))



(def event-bus (create-feed))

(defn make-topic
  "Make a new topic on which to report events"
  [topic-key]
  (->Topic topic-key event-bus))

"When plotting adjuvant treatments, we start from a baseline of surgery only, adding treatments and hopefully improving
survival, up to the projected survival of breast-cancer-free women "
(def treatment-order "surgery horm chemo tra bis radio")

(defn treatment-key-order [key]
  "Given a treatment key, returns the numeric order of the treatment as displayed"
  (index-of treatment-order (name key)))

; =>
(defn t-comp [key1 key2]
  (< (treatment-key-order key1) (treatment-key-order key2)))

(comment
  (treatment-key-order :horm)
  ;=> 0

  (treatment-key-order :horm)
  ;=> 0
  (treatment-key-order :tra))
;=> 11

(defn unknown [] "Unknown")
(def model "v2.1")

(defonce rtdb
         (atom {:recalculate-error-state 0

                :active-results-pane     "table"
                :active-mockup           "Mockup 1"         ;used for adverse effects mockups
                :incomplete              true

                ;; The set of widgets
                :on-screen-inputs        #{}
                :on-screen-treatments    (sorted-set-by t-comp)

                :show-uncertainty        :no

                ;; model results.
                ;; These should be non-nil when all inputs are complete.
                ;; Result data structure can be dependent on model
                :results                 []
                :h-cache-results         []

                ;; active route
                :route                   [:home nil nil]

                ;; help
                :help-key                nil
                :help-header             "Help header"
                :help-content            "Help content"

                ;; settings modal initially not shown
                :settings-visible        false

                ;; print modal initially not shown
                :print-visible           false

                ;; Generated results
                :completed-results       false

                ;; The state of the tool left-column accordion
                :hide-warning            false
                :test                    "test"
                :media                   :screen

                ;; 10 year Hormone latch
                :h10-latch               nil

                ;; translations state
                :t-state                 initial-t-state

                ;; modals
                :active-modal            nil

                ;; editing only
                :edit                    {}

                ;; Display results rounded?
                :rounded                 true
                }))

(defonce active-modal-cursor (rum/cursor-in rtdb [:active-modal]))
(defonce active-modal-change (make-topic :active-modal))

; The complete translations state (t-state) gets loaded from a URL of commands (usually dictionary.txt) at startup
(defonce edit-cursor (rum/cursor-in rtdb [:edit]))
(defonce edit-change (make-topic :edit-change))             ; change to the edit status of a field
(defonce new-text-cursor (rum/cursor-in rtdb [:edit :new-text]))
(defonce new-text-change (make-topic :new-text-change))

(defonce text-change (make-topic :text-change))             ; change to the new-text in an edited field (use args [key new-text])
(defonce add-language-modal (make-topic :add-language-modal))
(defonce add-language (make-topic :add-language))
(defonce t-state-cursor (rum/cursor-in rtdb [:t-state]))
(defonce t-state-change (make-topic :t-state-change))

(defonce ttt-cursor (rum/cursor-in rtdb [:t-state :translator]))
(defonce language-change (make-topic :language-change))

(defonce estimates (atom nil))



(defonce rounded-cursor (rum/cursor rtdb :rounded))
(defonce rounded-change (make-topic :rounded-change))

(defonce media-cursor (rum/cursor rtdb :media))
(defonce media-change (make-topic :media-change))

(defonce hide-warning-cursor (rum/cursor rtdb :hide-warning))
(defonce hide-warning-change (make-topic :hide-warning-change))

(defonce show-uncertainty-cursor (rum/cursor rtdb :show-uncertainty))
(defonce show-uncertainty-change (make-topic :show-uncertainty-change))

(defonce route (rum/cursor rtdb :route))
(defonce route-change (make-topic :route-change))

(defonce help-header-cursor (rum/cursor rtdb :help-header))
(defonce help-header-change (make-topic :help-header-change))

(defonce help-content-cursor (rum/cursor rtdb :help-content))
(defonce help-content-change (make-topic :help-content-change))

(defonce help-key-cursor (rum/cursor rtdb :help-key))
(defonce help-key-change (make-topic :help-key-change))

(defonce settings-cursor (rum/cursor rtdb :settings-visible))
(defonce settings-change (make-topic :settings-change))

(defonce print-cursor (rum/cursor rtdb :print-visible))
(defonce print-change (make-topic :print-change))

(defonce completed-results-cursor (rum/cursor rtdb :completed-results))
(defonce completed-results-change (make-topic :completed-change))

;; input keys that are currently on-screen
(defonce on-screen-inputs-cursor (rum/cursor rtdb :on-screen-inputs))

;; ref to a set containing active treatment keys (like :hormone)
(defonce on-screen-treatments-cursor (rum/cursor rtdb :on-screen-treatments))

; for REPL use
(def ost on-screen-treatments-cursor)

(defonce results-change (make-topic :results))
(defonce results-cursor (rum/cursor rtdb :results))
(defonce h-cache-cursor (rum/cursor rtdb :h-cache-results))


#_(defonce treatment-selection-cursor (rum/derived-atom [treatments-visible-cursor results-cursor] ::treatment-selection
                                                        (fn [a b] (and a b))))

(defonce active-results-pane (rum/cursor rtdb :active-results-pane))
(defonce active-results-change (make-topic :active-results-pane))

(defonce mockup-cursor (rum/cursor rtdb :active-mockup))
(defonce mockup-change (make-topic :active-mockup))

(defonce h10-latch-cursor (rum/cursor rtdb :h10-latch))

(defn is-completed []
  (@rtdb :completed-results))
;;;
;; Input keys
;;;
(defn input-cursors []
  (get-in @rtdb [:input-config :cursor]))

;; and define an access function ...for cursors
(defn input-cursor [key]
  (get-in @rtdb [:input-config :cursor key]))

;; ...and for mutators
(defn input-changes []
  (get-in @rtdb [:input-config :change]))

(defn input-change [key]
  (get-in @rtdb [:input-config :change key]))

(defn input-label [key]
  (get-in @rtdb [:input-config :label key]))

(defn input-default [key]
  (get-in @rtdb [:input-config :default key]))

; This has been replaced by render-widget [ttt key]
#_(defn input-widget [ttt key]
  (get-in @rtdb [:input-config :widget key]))

(defn input-access [key]
  (get-in @rtdb [:input-config :access key]))


;;;
;; An attempt to use derivatives to get a reactive value for enabled-treatments...
;;
;; It stumbles on my storage of rum cursors inside state. Maybe this was a bad move...
;;
;; To resurrect the idea I think we'd have to replace cursors with derivatives - those in :input-config at least
;;;
#_(def drv-spec
    {:base               [[] rtdb]
     :ostc               [[:base] (fn [base] (:on-screen-treatments base))]
     ;:horm               [[:base] (fn [base] @(get-in base [:input-config :cursor :horm]))]
     ;:tra                [[:base] (fn [base] @(get-in base [:input-config :cursor :tra]))]
     :bis                [[] (get-in rtdb [:input-config :cursor :bis])]
     :enabled-treatments [[:bis] (fn [bis] (str bis))]})

(defn enabled-treatments [otsc]
  "Given a list of on screen treatments, return only those that are enabled"
  (into (sorted-set.) (filter #(not= @(input-cursor %) :disabled) otsc)))


(defn get-inputs
  "This is the map of values that we feed into the model."
  []
  (into {}
        (map (fn [[k v]] [k @v])
             (filter (fn [[k _]]
                       ((union @on-screen-inputs-cursor @on-screen-treatments-cursor) k)) (input-cursors)))))

(defn year-selected []
  "return a cursor containing the selected year"
  (input-cursor :result-year))

;;;
;; Treatment keys - which depend on input keys
;;;
(comment                                                    ;; -- tests

  (print (rum/cursor-in rtdb [:page-simple]))

  (input-cursors)
  ; A map of all possible input cursors installed in input-config

  (input-changes)
  ; A map of input mutation channels. Each one references a publication topic.

  @on-screen-inputs-cursor
  ; inputs which appear on screen
  ; #{:age :size :her2-status :mode :ki67-status :nodes :grade :er-status :micromets}

  @on-screen-treatments-cursor
  ; A set! (It could be ordered to save n needing graphable-treatment)
  ; => #{:chemo :horm :tra}

  @enabled-treatments
  ; This is ordered! on-screen-inputs-cursor with radiation doses removed (since they don't appear in the show/explain options)
  ; Order
  ; => (:chemo :horm :tra)

  (get-inputs)
  ; This is the map of values that we feed into the model.
  ; Keys are unqualified (i.e. they don't refer to the selected treatment option)
  ; Values are however sensitive to the treatment option setting.
  ;=> {:tra nil, :age 40, :size 2, :her2-status :yes, :mode :symptomatic, :ki67-status :yes, :nodes 2, :grade 2, :horm nil, :er-status :yes, :chemo nil, :micromets :no}

  ;; show all inputs
  (into {} (map (fn [[k v]] [k @v]) (input-cursors)))
  ; => {:post-meno nil, :chemo nil, :tra nil, :age 40, :opt-picker-1 nil, :tra nil, :size 2, :performance nil, :radio nil, :horm nil, :her2-status :yes, :tra nil, :pr-status nil, :mode :symptomatic, :opt-picker-0 nil, :tra nil, :heart-dose nil, :type nil, :ethnicity nil, :radio nil, :ki67-status :yes, :size nil, :oncotype nil, :surgery nil, :chemo nil, :lung-dose nil, :nodes 2, :bis nil, :grade 2, :weight nil, :bmi nil, :bis nil, :heart-dose nil, :horm nil, :horm nil, :side nil, :chemo nil, :er-status :yes, :chemo nil, :micromets :no, :lung-dose nil, :smoking nil, :horm nil,:height nil, :neo :no}


  @results-cursor
  ([5 {:bcSpecSur 0.9769091120924175, :cumOverallSurOL 0.9692325692973538, :cumOverallSurHormo 0.007321510374584478, :cumOverallSurChemo 0, :cumOverallSurCandH 0.007321510374584478, :cumOverallSurCHT 0.012019282569459833}] [10 {:bcSpecSur 0.9407891636797882, :cumOverallSurOL 0.9192541233440501, :cumOverallSurHormo 0.018478310249836996, :cumOverallSurChemo 0, :cumOverallSurCandH 0.018478310249836996, :cumOverallSurCHT 0.030465967535812846}] [20 {:bcSpecSur 0.8760500103304053, :cumOverallSurOL 0.8104345849686604, :cumOverallSurHormo 0.037282221638997734, :cumOverallSurChemo 0, :cumOverallSurCandH 0.037282221638997734, :cumOverallSurCHT 0.0620689089391925}]))

