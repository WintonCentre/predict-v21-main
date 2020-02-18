(ns predict3.state.config
  (:require [rum.core :as rum]
    ;[predict3.eventbus :refer [event-bus]]
            [pubsub.feeds :refer [->Topic publish create-feed]]
            [predict3.state.local-storage :refer [get-settings!]]
            [tongue.core :as tongue]
            [translations.tongue-base :refer [wrap-translator]]
            [translations.config :refer [initial-supported-langs live-dictionary-url]]))

;;;
;; Initial language configuration - this data is used until teh full translations files are read in.
;; It does not have to be very complete as calls to the translator function should include the English default
;;;
(def rtl-languages #{:ar :az :dv :he :ku :fa :ur})          ; switch into right to left for these languages

#_(def initial-supported-langs #{:en :es :nl :it :fr :ja :pt})

(def initial-lang :en)

(def initial-translations {:en {:missing         "**MISSING**"
                                ; SVG texts MUST be present in the initial dictionary
                                :curves/x-axis   "Years after surgery"
                                :curves/y-axis   "Percentage of women surviving"
                                :what-is-predict "What is Predict?"}})

; The inital translations state only has to provide something until the online dictionary is accessed. Also, all translator calls should have an english default
; built in.
(def initial-t-state {:languages    initial-supported-langs ; list of currently supported languages (initialise by reading "languages.edn")
                      :lang         initial-lang ; the currently active language
                      :translations initial-translations
                      :translator   (wrap-translator initial-lang (tongue/build-translate initial-translations)) ; the current translator function (a wrapped tongue translator)
                      })


;;;
;; Input configuration
;; key - identifies the input widget
;; label - labells it when on screen
;; widget-type - is a key to the multi-method 'make-widget'
;; widget-params - are used by widget code
;; read-only - if true, a publication Topic is not created for ths widget
;; write-only - if true, a cursor is not created for this widget
;; unknowable - if true, an unknown value is valid
;; panel - a key indicating the panel where the widget will appear
;; models - the set of models which use this widget
;; default - the default value of the widget
;;;
(defrecord Input-group [key label widget-type widget-params read-only write-only unknowable panel models default])


(def input-groups
  "Define all input groups associated with one treatment option. The list of all treatment inputs are
  added into the list of input-groups at startup.

  If a treatment widget has value 'nil' we do not flag it as an error. Instead we assume the treatment is not
  to be graphed or displayed. "
  [
   ;; treatments first
   (->Input-group :chemo
                  "Chemotherapy"
                  :radio-group
                  [[nil "None"]
                   [:2nd "2nd gen"]
                   [:3rd "3rd gen"]]

                  false false nil
                  :treatment
                  #{"v1.2" "v2.1"}
                  nil)

   (->Input-group :radio
                  "Radiotherapy"
                  :radio-group
                  [[nil "No"]
                   [:yes "Yes"]]
                  false false nil
                  :treatment
                  #{"v2.1"}
                  nil)

   ; note that radiotherapy is enabled by a setting stored in localstorage
   (->Input-group :enable-radio
                  "Enable radiotherapy"
                  :radio-group
                  [[:no "No"]
                   [:no "Not yet implemented"]]
                  false false nil
                  :settings
                  #{"v2.1"}
                  (:enable-radio (get-settings! {:enable-radio :no})))

   ; note that 10 or 15 years outlook from time of diagnosis
   #_(->Input-group :ten-fifteen
                  "Ten or fifteen years outlook?"
                  :radio-group
                  [[10 "10"]
                   [15 "15"]]
                  false false nil
                  :settings
                  #{"v2.1"}
                  (:ten-fifteen (get-settings! {:ten-fifteen 15})))

   ; Different users want different default result views
   (->Input-group :default-tab
                  "Which result tab should appear first?"
                  :radio-group
                  [[:table "Table"]
                   [:curves "Curves"]
                   [:chart "Chart"]
                   [:texts "Texts"]
                   [:icons "Icons"]]
                  false false nil
                  :settings
                  #{"v2.1"}
                  (:default-tab (get-settings! {:default-tab :table})))

   ; note that bisphosphonates is enabled by a setting stored in localstorage
   (->Input-group :enable-bis
                  "Enable bisphosphonates"
                  :radio-group
                  [[:no "No"]
                   [:yes "Yes"]]
                  false false nil
                  :settings
                  #{"v2.1"}
                  (:enable-bis (get-settings! {:enable-bis :yes})))

   ; note that h10 is enabled by a setting stored in localstorage
   (->Input-group :enable-h10
                  "Enable 10 year hormone treatment?"
                  :radio-group
                  [[:no "No"]
                   [:no "Not yet implemented"]
                   ]
                  false false nil
                  :settings
                  #{"v2.1"}
                  (:enable-h10 (get-settings! {:enable-h10 :no})))

   ; note that dfs is enabled by a setting stored in localstorage
   (->Input-group :enable-dfs
                  "Enable disease free survival"
                  :radio-group
                  [[:no "No"]
                   [:no "Not yet implemented"]
                   ]
                  false false nil
                  :settings
                  #{"v2.1"}
                  (:enable-dfs (get-settings! {:enable-dfs :no})))

   #_(->Input-group :heart-dose
                    "Heart Dose (Gy)"
                    :numeric-input
                    {:min 0 :max 50 :step 0.1 :precision 1}
                    false false true
                    :treatment
                    #{"research"}
                    nil)

   #_(->Input-group :lung-dose
                    "Ipsilateral lung dose (Gy)"
                    :numeric-input
                    {:min 0 :max 50 :step 0.1 :precision 1}
                    false false true
                    :treatment
                    #{"research"}
                    nil)

   (->Input-group :horm
                  "Hormone Therapy"
                  :radio-group
                  [
                   [nil "No"]
                   [:h5 "5 Years"]
                   [:h10 "10 Years"]
                   ]

                  false false nil
                  :treatment
                  #{"v1.2" "v2.1"}
                  nil)

   (->Input-group :horm-delay
                  "Further hormone therapy"
                  :information
                  ;"This decision is only about whether to have an extra 5 years hormone therapy, but please also select treatments already received in the previous 5 years."
                  [:horm-delayed-1 ""]
                  false false true
                  :prp
                  #{"v2.1"}
                  "some value")
   ; for year 5
   (->Input-group :delay
                  "Already received 5 years hormone therapy?"
                  :radio-group
                  [
                   [nil "No"]
                   [:ys5 "Yes"]
                   ]
                  false false nil
                  :treatment
                  #{"v1.2" "v2.1"}
                  nil)

   (->Input-group :bis
                  "Bisphosphonates"
                  :radio-group
                  [[nil "No"]
                   [:yes "Yes"]]
                  false false nil
                  :treatment
                  #{"v2.1"}
                  nil)

   (->Input-group :tra
                  "Trastuzumab (eg. Herceptin)"
                  :radio-group
                  [
                   [nil "No"]
                   [:yes "Yes"]]

                  false false nil
                  :treatment
                  #{"v1.2" "v2.1"}
                  nil)

   (->Input-group :surgery-assumed
                  "Prior treatments"
                  :string
                  "Surgery is assumed"
                  false false false
                  :trp
                  #{"v2.1"}
                  nil)

   ;; inputs from here on

   ;; need to split this (v1.2 and v2 go down to 25 years), else min is 35
   (->Input-group :age
                  "Age at diagnosis"
                  :numeric-input
                  {:min 25 :max 85 :step 1 :precision 0}
                  false false true
                  :prp
                  #{"v1.2" "v2.1" "next-gen" "research"}
                  "")
   (->Input-group :post-meno
                  "Post Menopausal?"
                  :radio-group
                  [[:post "Yes"]
                   [:pre "No"]]
                  false false true
                  :prp
                  #{"v2.1"}
                  nil)
   (->Input-group :dcis
                  "DCIS or LCIS only?"
                  :radio-group
                  [[:yes "Yes"]
                   [:no "No"]]
                  false false false
                  :prp
                  #{"v2.1"}
                    nil)
   (->Input-group :mode
                  "Detected by"
                  :radio-group
                  [[:screen "Screening"]
                   [:symptomatic "Symptoms"]]
                  false false true
                  :trp
                  #{"v1.2" "v2.1" "next-gen" "research"}
                  nil)
   (->Input-group :grade
                  "Tumour grade"
                  :radio-group
                  [[:grade1 1] [:grade2 2] [:grade3 3]]
                  false false false
                  :trp
                  #{"v1.2" "v2.1" "next-gen" "research"}
                  nil)
   (->Input-group :size
                  "Invasive tumour size (mm)"
                  :numeric-input
                  {:min 0 :max 500 :step 5 :precision 0}
                  false false true
                  :trp
                  #{"v1.2" "v2.1"}
                  "")
   (->Input-group :nodes
                  "Positive nodes"
                  :numeric-input
                  {:min 0 :max 100 :step 1 :precision 0}
                  false false true
                  :arp
                  #{"v1.2" "v2.1"}
                  "")
   (->Input-group :micromets
                  "Micrometastases only"
                  :radio-group
                  [[:yes "Yes"]
                   [:no "No"]]
                  false false true
                  :arp
                  #{"v2.1" "research"}
                  :disabled)
   (->Input-group :er-status
                  "ER status"
                  :radio-group
                  [[:yes "Positive"]
                   [:no "Negative"]]
                  false false false
                  :arp
                  #{"v1.2" "v2.1" "next-gen" "research"}
                  nil)
   (->Input-group :her2-status
                  "HER2 status"
                  :radio-group
                  [[:yes "Positive"]
                   [:no "Negative"]]
                  false false true
                  :arp
                  #{"v1.2" "v2.1" "next-gen" "research"}
                  nil)
   (->Input-group :ki67-status
                  "Ki-67 status"
                  :radio-group
                  [[:yes "Positive"]
                   [:no "Negative"]]
                  false false true
                  :arp
                  #{"v1.2" "v2.1"}
                  nil)
   (->Input-group :result-year
                  "Years after surgery"
                  :radio-group
                  [[5 "5 years"]
                   [10 "10 years"]
                   [15 "15 years"]]
                  false false false
                  :tables
                  #{"v2.1"}
                  10)

   (->Input-group :show-uncertainty
                  "Show ranges?"
                  :radio-group
                  [[:yes "Yes"]
                   [:no "No"]]
                  false false false
                  :tables
                  #{"v2.1"}
                  :yes)])





(defn get-input-default [input-groups key]
  (:default (first (get-in (group-by :key input-groups) [key]))))



(comment
  (group-by :panel input-groups)

  (get-input-default input-groups :nodes))
  ;=> ""
