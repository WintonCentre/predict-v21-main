(ns predict3.results.util
  (:require [clojure.string :refer [index-of split join]]
            [clojure.edn :as edn]
            [goog.color :as col :refer [parse lighten rgbToHex hexToRgb rgbArrayToHex blend]]
            ))

(defn round-s
  [v rounded?]
  (.toFixed v (if rounded? 0 1)))

(defn dp1 [v] (.toFixed v 1))

(defn round
  [v rounded?]
  (edn/read-string (round-s v rounded?)))

(comment

  (round 3.124 true)
  (round 3.124 false)
  (round 3.9 false)
  (round-s 3.124 true)
  (round-s 3.124 false)
  )

(def treatment-keys
  "An ordered list of treatment keys. The order corresponds to the display order."
  [:surgery
   :horm :horm-low :horm-high
   :radio :radio-low :radio-high
   :chemo :chemo-low :chemo-high
   :tra :tra-low :tra-high
   :bis :bis-low :bis-high
   :br :oth])

(defn without-h10
  "Calculate the overall benefit with only 5 years hormone.
  Overall h5 low and h5 high are returned on the keys :hrctb-low and :hrctb-high"
  ([h5-results t key]
   (+ (key (nth h5-results t)) (:z (nth h5-results t))))
  ([h5-results t]
   (without-h10 h5-results t :hrctb))
  ([h5-results]
   (without-h10 h5-results 15 :hrctb))
  )

(defn h10-benefit
  "Calculate the h10 benefit over h5 at time t"
  ([h10-results h5-results t key]
   (->> [h10-results h5-results]
        (map #(nth % t))
        (map key)
        (apply -)
        ))
  ([h10-results h5-results t]
   (h10-benefit h10-results h5-results t :hrctb))
  ([h10-results h5-results]
   (h10-benefit h10-results h5-results 15 :hrctb))
  )


(defn larger-overall-survival
  "When enabled, uses a 120% larger bold font for the component.
  It is only used to highlight the Overall Survival figure in Text and Table views.
  See JIRA PB-10 for current status."
  [comp]
  ; currently disabled
  comp
  #_[:b {:style {:font-size "1.2em"}} comp]
  )


(def treatment-map*
  "An ordered map of treatment keys without uncertainty.
  The order corresponds to the display order."
  (array-map
    :surgery 0
    :horm 1
    :radio 2
    :chemo 3
    :tra 4
    :bis 5
    :br 6
    :oth 7))

(def treatment-keys*
  "An ordered list of treatment keys without uncertainty. The order corresponds to the display order."
  (into [] (keys treatment-map*)))

(defn make-precursors
  "Extract the treatment precursors and the treatment from a combination key which may have a '-high' or '-low' suffix.
  :hrct-high -> [:hrc-high :t-high]
  :hrc -> [:hr :c]"
  [key]
  (let [[tks suffix] (split (name key) #"-")]
    [key [(keyword (join (butlast tks))) (keyword (str (last tks) (when suffix (str "-" suffix))))]]))

(defn precursors*
  "Given a seq of treatment-keys - usually from a result calculation - return a map of key to its precursors"
  [result-keys]
  (into {} (map make-precursors result-keys)))

(def precursors (memoize precursors*))

(comment
  (precursors [:h :h-high :h-low :r :r-high :r-low :hr :hr-high :hr-low])
  ;=>
  #_([:h [nil :h]]
     [:h-high [nil :h-high]]
     [:h-low [nil :h-low]]
     [:r [nil :r]]
     [:r-high [nil :r-high]]
     [:r-low [nil :r-low]]
     [:hr [:h :r]]
     [:hr-high [:h-high :r-high]]
     [:hr-low [:h-low :r-low]]))

(defn lookup-delta
  [result key]
  (let [[pre-k final-k] (key (precursors (keys result)))]
    (if (and pre-k (> (Math/abs (pre-k result)) 0.005))
      (- (key result) (pre-k result))
      (final-k result))))

(defn lookup**
  [{:keys [result key]}]
  (* 100 (condp = key
           :surgery (lookup-delta result :z)

           :horm (lookup-delta result :h)
           :horm-low (lookup-delta result :h-low)
           :horm-high (lookup-delta result :h-high)

           :radio (lookup-delta result :hr)
           :radio-low (lookup-delta result :hr-low)
           :radio-high (lookup-delta result :hr-high)

           :chemo (lookup-delta result :hrc)
           :chemo-low (lookup-delta result :hrc-low)
           :chemo-high (lookup-delta result :hrc-high)

           :tra (lookup-delta result :hrct)
           :tra-low (lookup-delta result :hrct-low)
           :tra-high (lookup-delta result :hrct-high)

           :bis (lookup-delta result :hrctb)
           :bis-low (lookup-delta result :hrctb-low)
           :bis-high (lookup-delta result :hrctb-high)

           :oth (- 1 (:oth result))
           ; default
           0)))

;;;
;; Most browsers do not print background colours without the user setting a preference to do so.
;; We can paint divs with a background image, but we need to generate them algorithimcally.
;; Let's have a go at making dataURLs to paint backgrounds...
;;
;; See http://jsfiddle.net/LPxrT/
;;
(defn encode-triplet [e1 e2 e3]
  (let [keys (into [] "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=")
        enc1 (bit-shift-right e1 2)
        enc2 (bit-or (bit-shift-left (bit-and e1 3) 4) (bit-shift-right e2 4))
        enc3 (bit-or (bit-shift-left (bit-and e2 15) 2) (bit-shift-right e3 6))
        enc4 (bit-and e3 63)]
    (join [(keys enc1) (keys enc2) (keys enc3) (keys enc4)])))

;;;
;; Some colours
(def NHS-blue "#005EB4")
(def alison-blue-1 "#d3e7fd")                               ; home page block
(def alison-blue-1-rgb [211 231 253])                       ; home page block
(def alison-blue-2 "#002e5d")                               ; navbar & footer
(def alison-blue-3 "#1f6bc4")                               ; h1,h2.., primary button
(def alison-blue-5 "#edf5ff")                               ; treatment options results background
(def alison-blue-4 "#ffffff")                               ; treatment options results background (reverted)
(def alison-pink "#b4078d")                                 ; treatment options results background (reverted)


(defn encode-rgb [r g b]
  (join [(encode-triplet 0 r g) (encode-triplet b 255 255)]))

(defn generate-pixel [encoded-color]
  (join ["data:image/gif;base64,R0lGODlhAQABAPAA" encoded-color "/yH5BAAAAAAALAAAAAABAAEAAAICRAEAOw=="]))

(defn fill-data-url [r g b]
  (generate-pixel (encode-rgb r g b)))

(def hex-palette
  {:surgery          "#272a75"
   :horm             "#2fa8f2"
   :radio            "#238600"
   :chemo            "#ef975b"
   :tra              "#d838b9"
   :bis              "#2254e0"
   :callout          "#e29528"
   :dashed           "#ffaa00"
   :invalid          "#ff0000"
   :br               "#fcc"
   :oth              "#888"
   :other-treatments "#bbbbbb"})

(defn rgb-rx [key] (js->clj (hexToRgb (key hex-palette))))

(def rgb-palette (into {} (map (fn [k] [k (rgb-rx k)]) (keys hex-palette))))

(comment
  (def rgb-palette
    {:surg  [39 42 17]
     :horm  [47 168 242]
     :radio [35 134 0]
     :chemo [239 151 91]
     :tra   [216 56 185]
     :bis   [34 84 224]
     }))

(defn data-palette [key]
  (apply fill-data-url (rgb-palette key)))

(def fills (into [] (vals hex-palette)))

(defn fill
  ([index] (fills index)))

(def data-fill data-palette)

(def callout-fill (:callout hex-palette))
(def callout-data-fill (:callout data-palette))



(defn stepsToRGBArray
  [index]
  (hexToRgb (fill index)))


(comment
  "these urls should display in browsers as the basic stepped colours"
  (data-fill 0)
  ; => "data:image/gif;base64,R0lGODlhAQABAPAAAHG03P///yH5BAAAAAAALAAAAAABAAEAAAICRAEAOw=="

  (data-fill 1)
  ; => "data:image/gif;base64,R0lGODlhAQABAPAAAGaMwv///yH5BAAAAAAALAAAAAABAAEAAAICRAEAOw=="

  (data-fill 3))
; => "data:image/gif;base64,R0lGODlhAQABAPAAAFA8j////yH5BAAAAAAALAAAAAABAAEAAAICRAEAOw=="


(def dashed-stroke (:dashed hex-palette))

(def without-stroke {:stroke dashed-stroke :strokeDasharray "8,8" :strokeWidth 5 :strokeLinecap "round"})

(def min-label-percent 3)

(defn treatment-fills [index]
  "survival colour bands by data-index"
  (cond
    (<= index 5)
    (fill (- 5 index))

    (= index 6)
    "#ffffff"

    (= index 7)
    "#000000"))



; use a line to indicate women's survival without breast cancer
(def use-line false)


;;
;; models may recalculate data based on what is required for the currently selected presentation
;;

(defn clip [{:keys [value min max]
             :or   {value 0 min 0 max 10}}]
  "clip a value to be between min and max inclusively"
  (if (> value max)
    max
    (if (< value min)
      min
      value)))

(defn toPrecision
  [f & [high]]
  (js/parseFloat (.toPrecision (js/Number. f) (if (>= f 10) 2 (if high 2 1)))))


(defn avoid-decimals [d & [high]]
  "return a string representation of a number such as a percentage."
  (let [p (if (< d 10) (if high 1 0) 0)
        ret (.toFixed (js/Number. d) p)]
    ; for negative low precision numbers close to 0, we don't want "-0".
    (if (= ret "-0") "0" ret)))

(comment
  (avoid-decimals 0.0032 true)
  (avoid-decimals 0.0032 false))


(defn to-percent
  "convert float fraction to a decimal percent value at full precision"
  [f & [high]]
  (* 100 f))


(defn benefit% [data key & [high]]
  "Returns a sensibly rounded percentage benefit for given treatment
  If high, allow just one decimal place"
  (str (avoid-decimals (key data) high) "%"))

(defn benefits
  "Sum of benefits as a %, avoiding decimals if possible"
  [data & keys]
  (avoid-decimals (apply + (map #(% data) keys))))

(defn benefits%
  "Sum of benefits as a %, avoiding decimals if possible"
  [data & keys]
  (str (avoid-decimals (apply + (map #(% data) keys))) "%"))

(defn benefits-1dp%
  "Sum of benefits as % to 1dp"
  [data & keys]
  (str (avoid-decimals (apply + (map #(% data) keys))) "%" true))

(defn rounded?benefit%
  [data key rounded?]
  (if rounded?
    (benefit% data key)
    (str (round (key data) false) "%")))

(defn rounded?benefits% [data rounded? & keys]
  (if rounded?
    (str (avoid-decimals (apply + (map #(% data) keys))) "%")
    (str (round (apply + (map #(% data) keys)) false) "%")))



(comment
  (to-percent 0.0032 false)                                 ;=> 0.3
  (to-percent 0.0032 true)                                  ;=> 0.32
  (to-percent 0.0032)                                       ;=> 0.3
  (to-percent 0.2345 true)
  (to-percent 0.002345))


;;;
;; Data items to plot, tabulate, whatever
;;;

; defines a data-item.
(defrecord Item [treatment-key value])

;;
;; Chart annotation texts for use in callouts, titles, sub-titles etc.
;;

(defn lookup
  "Derive a plausible value for each individual treatment from the combined benefit values that Predict
  v2.1 gives us."
  [{:keys [model treatments result key horm-yes tra-yes]}]

  (let [treatments (into #{} treatments)]

    (cond
      (= key :surgery) (to-percent (:cumOverallSurOL result))

      (= key :horm) (to-percent (if horm-yes (:cumOverallSurHormo result) 0))
      (= key :horm-low) (to-percent (if horm-yes ((:marginSurHormo result) 0) 0))
      (= key :horm-high) (to-percent (if horm-yes ((:marginSurHormo result) 1) 0))

      (= key :chemo) (to-percent (if (and (treatments :horm) horm-yes)
                                   (- (:cumOverallSurCandH result) (:cumOverallSurHormo result))
                                   (:cumOverallSurChemo result)))

      (= key :chemo-low) (to-percent (if (and (treatments :horm) horm-yes)
                                       (- ((:marginSurCandH result) 0) (:cumOverallSurHormo result))
                                       ((:marginSurChemo result) 0)))

      (= key :chemo-high) (to-percent (if (and (treatments :horm) horm-yes)
                                        (- ((:marginSurCandH result) 1) (:cumOverallSurHormo result))
                                        ((:marginSurChemo result) 1)))

      (= key :tra) (to-percent (if (and (treatments :tra)
                                        tra-yes
                                        (treatments :chemo))
                                 (- (:cumOverallSurCHT result) (:cumOverallSurCandH result))
                                 0))


      (= key :tra-low) (to-percent (if (and (treatments :tra)
                                            tra-yes
                                            (treatments :chemo))
                                     (- ((:marginSurCHT result) 0) (:cumOverallSurCandH result))
                                     0))


      (= key :tra-high) (to-percent (if (and (treatments :tra)
                                             tra-yes
                                             (treatments :chemo))
                                      (- ((:marginSurCHT result) 1) (:cumOverallSurCandH result))
                                      0))


      (= key :br) (to-percent (:br result))
      (= key :oth) (to-percent (:oth result))
      :else 0)))


