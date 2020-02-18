(ns e2e.generators
  (:require [clojure.test.check.generators :as gen]))

(def five-through-nine (gen/choose 5 9))
#_(gen/sample five-through-nine)

(def no (gen/return :no))
#_(gen/sample no)

(def yes (gen/return :yes))
#_(gen/sample yes)

(def unknown (gen/return :unknown))
#_(gen/sample unknown)

(def yes-no (gen/elements [:yes :no]))
(def yes-no-unk (gen/elements [:yes :no :unknown]))
#_(gen/sample yes-no)

(def pos-neg (gen/elements [:positive :negative]))
(def pos-neg-unk (gen/elements [:positive :negative :unknown]))
(def scr-sym-unk (gen/elements [:screening :symptoms :unknown]))
(def no-5-10 (gen/elements [:no :h5 :h10]))
;(def horm (gen/sample no-5-10))

(def none-2nd-3rd (gen/elements [:none :2nd :3rd]))
(def y5-10-15 (gen/elements [:5 :10 :15]))
(def age (gen/fmap (fn [age] [:enter :age age]) (gen/choose 25 85)))


(def post-meno (gen/fmap (fn [x] [:press :post-meno x]) (gen/frequency [[5 yes] [5 no] [1 unknown]])))


;(def age (gen/elements (range 25 85)))

; todo: replace constant tests with x
#_(def er-status (gen/fmap (fn [x] [:press :er-status x]) pos-neg))
(def er-status (gen/fmap (fn [x] [:press :er-status x]) (gen/frequency [[80 (gen/return :positive)]
                                                                        [20 (gen/return :negative)]])))
#_(def her2-status (gen/fmap (fn [x] [:press :her2-status x]) pos-neg-unk))
(def her2-status (gen/fmap (fn [x] [:press :her2-status x]) (gen/frequency [[70 (gen/return :positive)]
                                                                            [20 (gen/return :negative)]
                                                                            [10 (gen/return :unknown)]])))
(def ki67-status (gen/fmap (fn [x] [:press :ki67-status x]) (gen/frequency [[70 (gen/return :positive)]
                                                                            [20 (gen/return :negative)]
                                                                            [10 (gen/return :unknown)]])))
(def size (gen/fmap (fn [x] [:enter :size x]) (gen/frequency [[100 (gen/choose 1 50)]
                                                              [30 (gen/choose 51 99)]
                                                              [5 (gen/choose 100 550)]])))
(def grade (gen/fmap (fn [x] [:press :grade (keyword (str x))]) (gen/choose 1 3)))
(def mode (gen/fmap (fn [x] [:press :mode x]) scr-sym-unk))
(def nodes (gen/fmap (fn [x] [:enter :nodes x]) (gen/frequency [[100 (gen/choose 1 5)]
                                                                [30 (gen/choose 6 20)]
                                                                [5 (gen/choose 21 100)]])))
(def micromets (gen/fmap (fn [x] [:press :micromets x]) yes-no-unk))

(def horm (gen/fmap (fn [x] [:press :horm x]) no-5-10))
(def g-delay (gen/fmap (fn [x] [:press :delay x]) yes-no))
(def chemo (gen/fmap (fn [x] [:press :chemo x]) none-2nd-3rd))
(def tra (gen/fmap (fn [x] [:press :tra x]) yes-no))
(def bis (gen/fmap (fn [x] [:press :bis x]) yes-no))
;(def result-year (gen/fmap (fn [x] [:press :result-year x]) y5-10-15))



(defn inputs-and-treatments
  []
  (let [v-age (gen/generate age)
        v-nodes (gen/generate nodes)
        v-post-meno (gen/generate post-meno)
        v-er-status (gen/generate er-status)
        v-her2-status (gen/generate her2-status)
        v-ki67-status (gen/generate ki67-status)
        v-size (gen/generate size)
        v-grade (gen/generate grade)
        v-mode (gen/generate mode)
        v-nodes (gen/generate nodes)
        v-horm (if (= (v-er-status 2) :positive) (gen/generate horm) nil)
        v-delay (if (and v-horm (= (v-horm 2) :h10)) (gen/generate g-delay) nil)
        v-chemo (gen/generate chemo)
        v-tra (if (and (not= (v-chemo 2) :none) (= (v-her2-status 2) :positive))
                (gen/generate tra) nil)
        v-bis (if (not= (v-post-meno 2) :no) (gen/generate bis) nil)
        v-micromets (if (= (v-nodes 2) 1) (gen/generate micromets) nil)
        inputs (remove nil? [[:reset]
                             [:wait 0.1]
                             [:press :dcis :no]
                             v-age
                             v-post-meno
                             v-er-status
                             v-her2-status
                             v-ki67-status
                             v-size
                             v-grade
                             v-mode
                             v-nodes
                             v-micromets
                             v-horm
                             v-chemo
                             v-tra
                             v-bis
                             v-delay])]
    ;(println "inputs-and-treatments1:" inputs)
    inputs
    ))

(def op-to-r
  {:age         [:age.start identity]
   :post-meno   [:post-meno identity]
   :mode        [:screening {:screening 1 :symptoms 0 :unknown 2}] ; R wants 2 for :unknown
   :size        [:size identity]
   :nodes       [:nodes identity]
   :grade       [:grade {:1 1 :2 2 :3 3}]
   :er-status   [:er {:positive 1 :negative 0}]
   :her2-status [:her2 {:positive 1 :negative 0 :unknown 9}]
   :ki67-status [:ki67 {:positive 1 :negative 0 :unknown 9}]
   :chemo       [:generation {:none 0 :2nd 2 :3rd 3}]
   :horm        [:horm {:no nil :h5 :h5 :h10 :h10}]
   :tra         [:traz {:yes 1 :no 0}]
   :bis         [:bis {:yes 1 :no 0}]
   :micromets   [:micromets identity]
   :delay       [:fn {:yes "benefits2210" :no "benefits22"}]})

(defn translate-to-r*
  [ops]
  (let [m (->> ops                                          ; convert generated web ops to an initial parameter map
               (filter (comp not #{:dcis} second))
               (filter (comp #{:press :enter} first))
               (map (fn [[op k v]]
                      (let [[rk rvf] (op-to-r k)
                            rv (rvf v)]
                        [rk rv])))
               (into {}))]
    (let [not-pm (= :no (:post-meno m))
          micromets? (= (:micromets m) :yes)]
      (->> m
           (map (fn [[k v]]
                  (cond
                    ; set nodes to 0.5 if micromets is yes
                    (and micromets? (= :nodes k))
                    [k 0.5]

                    ; encode [horm = :h5] or [:horm :h10] as [:horm 1]
                    (= k :horm)
                    [k (if (#{:h5 :h10} v) 1 0)]

                    ; encode :screening on key :screen
                    (= k :screening) [:screen v]

                    :else [k v]
                    )))
           (remove (fn [[k v]] (and not-pm (= [k v] [:bis 1]))))
           (filter (fn [[k v]]
                     (and
                       (not= k :micromets)
                       (not= k :post-meno)
                       )))
           (into {})))))

;(translate-to-r* (inputs-and-treatments))

(defn r-inputs-and-treatments
  "Generate inputs and treatments for both the browser and the R code.
  The returned :cljs value can be exec'ed on the browser, and then compared
  with the result of running the r-function with r-params via an opencpu server.

  On the cljs side, it's necessary to switch to the curve view and then pick a few
  values to compare after the view has been established."
  []
  (let [cljs-inputs (inputs-and-treatments)
        r-inputs (translate-to-r* cljs-inputs)]
    {:cljs       cljs-inputs
     :r-function (if (:fn r-inputs) (:fn r-inputs) "benefits22")
     :r-params   (into {} (filter (fn [[k v]] (not= k :fn)) r-inputs))})
  )

#_(r-inputs-and-treatments)
#_(inputs-and-treatments)

(def test-input
  [[:reset]
   [:wait 0.1]
   [:press :dcis :no]
   [:enter :age 57]
   [:press :post-meno :yes]
   [:press :er-status :positive]
   [:press :her2-status :positive]
   [:press :ki67-status :positive]
   [:enter :size 20]
   [:press :grade :2]
   [:press :mode :screening]
   [:enter :nodes 10]
   [:do
    [:press :horm :h10]
    [:wait 0.2]
    [:press :delay :yes]]
   [:press :chemo :3rd]
   [:press :tra :yes]
   [:press :bis :yes]]
  )

(def alex-example
  [[:reset]
   [:wait 0.1]
   [:press :dcis :no]
   [:enter :age 66]
   [:press :post-meno :yes]
   [:press :er-status :positive]
   [:press :her2-status :positive]
   [:press :ki67-status :positive]
   [:enter :size 12]
   [:press :grade :2]
   [:press :mode :screening]
   [:enter :nodes 2]
   [:do
    [:press :horm :h10]
    [:wait 0.2]
    [:press :delay :no]]
   [:press :chemo :3rd]
   [:press :tra :yes]
   [:press :bis :yes]]
  )

(comment

  (inputs-and-treatments)



  (def ops-example
    [
     [:press :dcis :no]                                     ; yes is invalid really
     [:enter :age 33]
     [:press :post-meno :no]
     [:press :er-status :positive]
     [:press :her2-status :negative]
     [:press :ki67-status :unknown]
     [:enter :size 44]
     [:press :grade 2]
     [:press :mode :screening]
     [:enter :nodes 2]
     ; treatments from here on
     [:press :horm :yes]
     [:press :chemo :3rd]]
    ))
