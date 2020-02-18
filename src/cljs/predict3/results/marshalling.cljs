(ns predict3.results.marshalling
  "Functions related to marshalling data returned from the model into useful formats for the UI views"
  (:require [clojure.string :refer [split join]]
            [clojure.set :refer [map-invert]]
            [predict3.results.util :refer [treatment-keys]]))

(defn- make-precursors
  "Extract the treatment precursors and the treatment from a model rx combination key which may have a '-high' or '-low' suffix.
  :hrct-high -> [:hrc-high :t-high]
  :hrc -> [:hr :c]"
  [rx-key]
  (let [[tks suffix] (split (name rx-key) #"-")
        pre (butlast tks)]
    [rx-key [(if-let [pres pre] (keyword (join pres)) nil)
             (keyword (str (last tks) (when suffix (str "-" suffix))))]]))

(defn precursors
  "Given a seq of treatment-keys - usually from a result calculation - return a map of key to its precursors"
  [result-keys]
  (into {} (map (memoize make-precursors) result-keys)))

(comment
  (precursors [:h :h-high :h-low :r :r-high :r-low :hr :hr-high :hr-low :hrc-low])
  ;=>
  #_{:r-low   [nil :r-low],
     :h-high  [nil :h-high],
     :r       [nil :r],
     :hr      [:h :r],
     :h-low   [nil :h-low],
     :hrc-low [:hr :c-low],
     :hr-low  [:h :r-low],
     :h       [nil :h],
     :hr-high [:h :r-high],
     :r-high  [nil :r-high]})

(def tk->rx
  "Although this map is bijective,  the key names are meaningful in their respective contexts, so best to preserve the
  different conventions. Keys are UI treatment keys, Values are model treatment keys."
  {
   :surgery    :z

   :horm       :h
   :horm-low   :h-low
   :horm-high  :h-high

   :radio      :hr
   :radio-low  :hr-low
   :radio-high :hr-high

   :chemo      :hrc
   :chemo-low  :hrc-low
   :chemo-high :hrc-high

   :tra        :hrct
   :tra-low    :hrct-low
   :tra-high   :hrct-high

   :bis        :hrctb
   :bis-low    :hrctb-low
   :bis-high   :hrctb-high

   :oth        :oth})

(defn- lookup-delta
  [result key]
  (let [[pre-k final-k] (key (precursors (keys result)))]
    (if pre-k
      (- (key result) (pre-k result))
      (final-k result)
      )))


(defn delta-pc-benefits
  "Return the additional benefit given the year-benefits and the treatment key.

  Note that there is a translation here between model treatment key (the rx keys), and
  the UI treatment-keys.

  Note also that the treatment-key order is significant as we are calculating the
  additional benefit over the benefit of treatments lower in the display stack.

  There's an unavoidable bias here - so we try to keep the stack order close to the
  order in which clinicians consider additional treatments.

  year benefits is a map of one year's benefits keyed by treatment-key (e.g. :chemo)
  treatment-key is a UI treatment key"

  ([{:keys [year-benefits treatment-key]}]
   (* 100 (condp = treatment-key
            :oth (- 1 (:oth year-benefits))
            :br 0
            (lookup-delta year-benefits (tk->rx treatment-key))))))

(defn additional-benefit-kvs
  "Lookup treatments and create a stacked bar dataset from model results, indexed by year.
  Each data-item is a key value pair ordered in the same way as incoming treatments vector."
  [{:keys [annual-benefits year tks]
    :or   {tks treatment-keys}}]

  #_(println "annual-benefits" annual-benefits)
  #_(println "year" year)
  #_(println "tks" tks)

  (map (fn [key]
         [key (delta-pc-benefits {:treatment-key key
                                  :year-benefits (nth annual-benefits year)})])
       tks))

(defn additional-benefit-map
  "Lookup treatments and create a stacked bar dataset from model results, indexed by year.
  Each data-item is a key value pair ordered in the same way as incoming treatments vector."
  [args]
  (into {} (additional-benefit-kvs args)))

(defn all-rounded-benefits
  "Without rounding benefits, insert against key :br the breast cancer related deaths. This is calculated so as to make
  the difference to 100% when all benefits have been added and finally rounded."
  [{:keys [annual-benefits year tks]
    :as   args}]

  (let [data (additional-benefit-map args)]
    (assoc data :br (- 100 (Math/round (+ (:oth data) (:tra data) (:chemo data) (:horm data) (:surgery data)))))))

(comment
  (require '[predict3.state.run-time :refer [results-cursor]])
  (additional-benefit-kvs {:annual-benefits @results-cursor
                           :year            10})
  (additional-benefit-map {:annual-benefits @results-cursor
                           :year            10})
  )
