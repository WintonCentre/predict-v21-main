(ns e2e.execs
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [etaoin.api :as w]
            [etaoin.keys :as k]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [e2e.generators :as pgen]
            [e2e.ocpu :as ocpu]
            [clojure.stacktrace :as trace]))




(comment
  ; RUN THESE IN THE REPL

  ; first start a driver
  (def driver (w/chrome {:headless false}))
  #_(def driver (w/firefox {:headless false}))

  ; Make sure there's a cljs dev repl running predict on localhost:5448
  ; Then load up the tool page, and make sure the GDPR bar is hidden
  (load-tool-page driver)

  ; And run tests R or consistency tests - one at a time!
  (check-against-r! driver 5)                               ; check values against r
  (check-rounding-consistency! driver 1)                   ; check consistency of view calculations


  )


; If you get a java error=2 on java.lang.UNIXProcess.forkAndExec() when running the above commands,
; recover by restarting Intellij. The error seems to appear after a MacOSX sleep.

(defn offset
  "set the scroll offset needed to ensure visibility of buttons etc."
  [driver]
  (cond
    (w/chrome? driver) 100
    (w/firefox? driver) 150
    :else (throw (Exception. "unknown web driver"))))


(defn load-tool-page [driver]
  (doto driver
    (w/go "http://localhost:5449/index.html#/tool")
    (w/wait-visible [{:tag :button :id :reset}])))

(def inputs-meta (into {}
                       (map (fn [[k v]] [k (zipmap v (map inc (range)))])
                            {:dcis             [:yes :no]
                             :post-meno        [:yes :no :unknown]
                             :er-status        [:positive :negative]
                             :her2-status      [:positive :negative :unknown]
                             :ki67-status      [:positive :negative :unknown]
                             :grade            [:1 :2 :3]
                             :mode             [:screening :symptoms :unknown]
                             :micromets        [:yes :no :unknown]
                             :horm             [:no :h5 :h10]
                             :delay            [:no :yes]
                             :chemo            [:none :2nd :3rd]
                             :tra              [:no :yes]
                             :bis              [:no :yes]
                             :show-uncertainty [:yes :no]
                             :result-year      [:5 :10 :15]
                             })))

(def r-inputs-meta (into {}
                         (map (fn [[k v]] [k (zipmap v (map inc (range)))])
                              {:dcis             [:yes :no]
                               :post-meno        [:yes :no :unknown]
                               :er-status        [:positive :negative]
                               :her2-status      [:positive :negative :unknown]
                               :ki67-status      [:positive :negative :unknown]
                               :grade            [:1 :2 :3]
                               :mode             [:screening :symptoms :unknown]
                               :micromets        [:yes :no :unknown]
                               :horm             [:no :h5 :h10]
                               :delay            [:no :yes]
                               :chemo            [:none :2nd :3rd]
                               :tra              [:no :yes]
                               :bis              [:no :yes]
                               :show-uncertainty [:yes :no]
                               :result-year      [:5 :10 :15]
                               })))

(defn load-page! [driver url]
  "load up a (predict) page and wait "
  (doto driver
    (w/go url)
    (w/wait-visible [{:tag :button :id :reset}])
    ))

(defn press-reset!
  "Press reset button to clear entries"
  [driver & args]
  (let [target [{:id "reset"}]]
    (doto driver
      (w/wait 1)
      (w/scroll-top)
      (w/scroll-up 65)
      (w/click target)
      )))

(defn wait!
  "Wait some seconds"
  [driver & [secs]]
  (w/wait driver secs))

(defn wait-for!
  "Waits till a predicate is true"
  [driver & [pred opt]]
  (w/wait-predicate pred opt)
  )

(defn wait-visible!
  "Waits till an element identified by q is visible "
  [driver q & [opt]]
  (w/wait-visible driver q opt)
  )
(comment
  (wait-visible! driver [{:id ":size"}])
  )

(defn press-radio!
  "Press button identified by keyword on widget identified by key.
  Note that clicking on a disabled button is fine, but it has no effect."
  [driver & [key button]]
  (doto driver
    (w/scroll-query [{:id (name key)}])
    (w/scroll-up (offset driver))
    (w/click [{:id (name key)} {:tag   :button
                               :index (get-in inputs-meta [key button])}])))

(defn enter-number!
  "Enter  value into numeric input identified by key"
  [driver & [key value]]
  (w/fill driver [{:id (name key)}] (str value)))

(defn switch-view!
  "Switch to a result view"
  [driver & [view]]
  (let [target [{:aria-controls (name view)}]]
    (doto driver
      (w/scroll-query target)
      (w/scroll-up (offset driver))
      (w/click target)))
  )

(defn exec!
  "execute a list of browser operations with their arguments"
  [driver ops]
  (doseq [[op & [key value :as args]] ops]
    ;(println "exec!" op args)
    (condp = op
      :load (load-page! driver key)
      :reset (press-reset! driver)
      :wait (wait! driver key)
      :wait-for (wait-for! driver args)
      :wait-vis (wait-visible! driver args)
      :press (press-radio! driver key value)
      :enter (doseq [v (str value)]
               (wait! driver 0.1)
               (enter-number! driver key (str v))
               (wait! driver 0.1))
      :view (switch-view! driver key)
      :do (exec! driver args)                               ;nested exec if given a [:do ops]
      :else
      )))


;
; todo: In :h10/delay we don't have all plots so this errors
;
(defn get-curve-data!
  [driver plot-id year]
  (let [[_ X Y] (-> (get (str/split
                           (w/get-element-attr driver [{:id plot-id}] "points")
                           #",") year)
                    (str/split #"\s+"))
        [x y] [(read-string X) (read-string (if Y Y "0"))]]
    (* (- 310 y) (/ 100. 310))))
(comment
  (get-curve-data! driver "plot-horm" 5)
  (get-curve-data! driver "plot-horm" 10)
  (get-curve-data! driver "plot-horm" 15)
  (get-curve-data! driver "plot-surgery" 15)
  (get-curve-data! driver "plot-chemo" 15)
  (get-curve-data! driver "plot-tra" 15)
  (get-curve-data! driver "plot-bis" 15)
  )


(defn get-results!
  "Get results from a view. It is assumed that the view is currently loaded into the browser, and a year
  has been selected (5, 10, or 15) years. The year parameter is necessary to pick out values from curves.

  Keys may be nil valued in the result map. This may mean the corresponding treatment was not selected or
  it may mean that the view does not display that quantity, even though it might be inferred from others that are
  displayed.

  Curves have a special place as plotted points are not rounded, and so very useful for understanding expected
  roundings in other views. We transform points from screen coordinates back into data coordinates.

  h10 with delay views only have ovther, bc, horm and surgery
  "
  [driver view year]

  (let [result-map (into {} (map (fn [k] [k nil]) [:overall-surg
                                                   :horm
                                                   :overall-horm
                                                   :chemo
                                                   :overall-chemo
                                                   :tra
                                                   :overall-tra
                                                   :bis
                                                   :overall-bis
                                                   :horm+chemo
                                                   :horm+chemo+tra
                                                   :horm+chemo+tra+bis
                                                   :nobc
                                                   :bc
                                                   :other]))]
    (condp = view
      :table (let [extract (fn [field] (let [s (try (w/get-element-text driver [{:id field}]) (catch Exception e))]
                                         (when s (read-string (first (str/split s #"%"))))))]
               (assoc result-map
                 :overall-surg (extract "overall-surg")
                 :horm (extract "horm")
                 :overall-horm (extract "overall-horm")
                 :chemo (extract "chemo")
                 :overall-chemo (extract "overall-chemo")
                 :tra (extract "tra")
                 :overall-tra (extract "overall-tra")
                 :bis (extract "bis")
                 :overall-bis (extract "overall-bis")
                 :nobc (extract "nobc")
                 :other (extract "other")
                 ))

      :curves (let [extract (fn [plot]
                              (if (w/exists? driver [{:id plot}])
                                (get-curve-data! driver plot year)
                                0))
                    overall-surg (extract "plot-surgery")
                    overall-horm (extract "plot-horm")
                    overall-chemo (extract "plot-chemo")
                    overall-tra (extract "plot-tra")
                    overall-bis (extract "plot-bis")
                    horm (- overall-horm overall-surg)
                    chemo (- overall-chemo overall-horm)
                    tra (- overall-tra overall-chemo)
                    bis (- overall-bis overall-tra)
                    horm+chemo (+ horm chemo)
                    horm+chemo+tra (+ horm chemo tra)
                    results {:overall-surg   overall-surg
                             :horm           (when (pos? horm) horm)
                             :overall-horm   overall-horm
                             :chemo          (when (pos? chemo) chemo)
                             :overall-chemo  overall-chemo
                             :tra            (when (pos? tra) tra)
                             :overall-tra    overall-tra
                             :bis            (when (pos? bis) bis)
                             :overall-bis    overall-bis
                             :horm+chemo     (when (pos? horm+chemo) horm+chemo)
                             :horm+chemo+tra (when (pos? horm+chemo+tra) horm+chemo+tra)
                             :nobc           nil
                             :bc             nil
                             :other          nil}
                    ;_ (println results)
                    ]
                results)

      :chart (let [extract (fn [field] (let [s (try (w/get-element-text driver [{:id field}]) (catch Exception e))]
                                         (when s (read-string s))))]
               (assoc result-map
                 :overall-surg (extract "overall-surg")
                 :horm (extract "horm")
                 :chemo (extract "chemo")
                 :tra (extract "tra")
                 :bis (extract "bis")))

      :texts (let [query (fn [field] [{:id "results"} {:class field}])
                   extra (fn [field] ({:horm  :horm
                                       :chemo :horm+chemo
                                       :tra   :horm+chemo+tra
                                       :bis   :Horm+chemo+tra+bis} field))
                   get-n (fn [s] (when-not (empty? s) (read-string s)))
                   extract (fn [field]
                             (let [q (query field)]
                               (when (w/exists? driver q)
                                 (get-n (w/get-element-text driver q)))))
                   extract2 (fn [field]
                              (if (w/exists? driver (query "no-benefit"))
                                {}
                                (let [q (query field)]
                                  (if (w/exists? driver q)
                                    (let [[_ overall-v v] (re-matches #"(\d+).*extra (\d+).*" (w/get-element-text driver q))]
                                      {(keyword (str "overall-" field)) (get-n overall-v)
                                       (extra (keyword field))          (get-n v)})
                                    {}))))
                   get-other (fn []
                               (let [q (query "other")]
                                 (when (w/exists? driver q)
                                   (get-n (get (re-matches #".* (\d+) .*" (w/get-element-text driver q)) 1)))))]
               (assoc (apply merge result-map (map extract2 ["horm" "chemo" "tra" "bis"]))
                 :overall-surg (extract "surgery")
                 :other (get-other))
               )

      :icons (let [extract (fn [field]
                             (let [q [{:id "legend"} {:class field}]]
                               (if (w/exists? driver q)
                                 (read-string (w/get-element-text driver q)) 0)))
                   surgery (extract "surgery")
                   horm (extract "horm")
                   chemo (extract "chemo")
                   tra (extract "tra")
                   bis (extract "bis")
                   other (extract "other")
                   bc (extract "bc")
                   ]
               (assoc result-map
                 :surgery surgery
                 :horm horm
                 :chemo chemo
                 :tra tra
                 :bis bis
                 :bc bc
                 :other other
                 ))

      ; else
      (throw (Exception. "unknown view")))))

(comment
  ; keep these in case we need to develop :text extractions again

  (def query (fn [field] [{:id "results"} {:class field}]))
  (def get-n (fn [s] (when-not (empty? s) (read-string s))))
  (def extract (fn [field]
                 (let [q (query field)]
                   (when (w/exists? driver q)
                     (get-n (w/get-element-text driver q))))))
  (def extract2 (fn [field]
                  (if (w/exists? driver [{:id "results" :class "no-benefit"}])
                    {}
                    (let [q [{:id "results"} {:class field}]]
                      (if (w/exists? driver q)
                        (let [[_ overall-v v] (re-matches #"(\d+).*extra (\d+).*" (w/get-element-text driver q))]
                          {(keyword (str "overall-" field)) (get-n overall-v)
                           (keyword field)                  (get-n v)})
                        {})))))
  (def get-other (fn []
                   (let [q [{:id "results"} {:class "other"}]]
                     (when (w/exists? driver q)
                       (get-n (get (re-matches #".* (\d+) .*" (w/get-element-text driver q)) 1)))))))

(defn get-all-results!
  "Assume a tool page has been loaded with suitable parameters.
  Scan through :table, :chart, :texts, and :icons views collecting results together"
  [driver year]
  ; ensure table view is loaded, and a year has been selected
  (exec! driver [[:view :table]
                 [:wait 0.1]
                 [:press :show-uncertainty :no]
                 [:press :result-year (if (keyword? year)
                                        year
                                        (keyword (str year)))]])
  (into {} (map
             (fn [view]
               (exec! driver (conj [[:view view]
                                    [:wait 0.1]]))
               [view (get-results! driver view year)])
             [:table :curves :chart :texts :icons])))


(defn check-key
  "Check that the value at (k v) agrees with that at (k c) up to rounding.
  Return nil if either value is nil."
  [c v k]
  ;(println k (k v) (k c))
  (when (and k (k v) (k c))                                 ;todo: can k still be nil?
    (if (not= (Math/round (float (k c))) (k v))
      [k {:exp (k c) :act (k v)}]
      nil)))

(defn check-view
  "check all the keys in the result for view v, generating a map of keys with errors"
  [c v]
  ;(println "check-view c = " c " v = " v)
  (into {} (filter some? (map #(check-key c v %) (keys v))))
  )

(comment
  (check-view (:curves r)
              (-> (:table r) (dissoc :bc) (dissoc :other) (dissoc :nobc)))
  (def v {:horm+chemo+tra     nil,
          :chemo              3,
          :bis                0,
          :horm+chemo         nil,
          :overall-tra        97,
          :tra                1,
          :overall-chemo      96,
          :horm+chemo+tra+bis nil,
          :overall-horm       94,
          :overall-bis        97,
          :overall-surg       91,
          :horm               3})
  (def c {:horm+chemo+tra 5.612907668639323,
          :chemo          1.990994920412632,
          :bis            0.4562542434791226,
          :horm+chemo     4.532884834352245,
          :overall-tra    96.83796081103512,
          :bc             nil,
          :tra            1.0800228342870781,
          :other          nil,
          :overall-chemo  95.75793797674804,
          :overall-horm   93.7669430563354,
          :overall-bis    97.29421505451424,
          :nobc           nil,
          :overall-surg   91.2250531423958,
          :horm           2.541889913939613})
  (check-view c v)
  )

(defn calculate-total-icons
  "Add up effect of treatments for overall-surg horm chemo tra bis bc and other and hope the answer
  is 100 - because that's how many icons we want to display."
  [icons]
  (apply + (filter some? (map (fn [k] (k icons))
                              [:surgery :horm :chemo :tra :bis :bc :other]))))

(defn check-all-views
  "Check Results for consistency. Compare everything against the curves output.
  Returns a possibly empty map of diffs by view and by input key.
  Also add in a check that the total number of icons is 100."
  [result]
  (let [rv (into {} (filter (fn [[v errors]] (not= errors {}))
                            (map
                              (fn [v] [v (check-view (:curves result)
                                                     (-> (v result) (dissoc :bc) (dissoc :other) (dissoc :nobc)))])
                              [:table
                               :chart
                               :texts
                               :icons
                               ])))
        icon-sum (calculate-total-icons (:icons result))]
    (if (not= 100 icon-sum)
      (assoc rv
        :icon-sum icon-sum)
      rv)))
;;
;; for R comparisons
;;
(defn web-benefits22!
  "This version is designed to return results in a batch which is more directly comparable to the R results.
  Call with the tools view set to  curves."
  [driver]
  (map (fn [year]
         (zipmap [:z :h :c :t :b]
                 (let [z (get-curve-data! driver "plot-surgery" year)]
                   (cons z
                         (map (fn [[x y]] (- y x)) (partition 2 1 [z
                                                                   (get-curve-data! driver "plot-horm" year)
                                                                   (get-curve-data! driver "plot-chemo" year)
                                                                   (get-curve-data! driver "plot-tra" year)
                                                                   (get-curve-data! driver "plot-bis" year)]))))))
       (range 0 16)))

(defn web-benefits2210!
  "Returns numbers comparable to R benefits2210 function"
  [driver]
  (map (fn [year]
         (zipmap [:h5 :h10]
                 (map (fn [[x y]] (- y x)) (partition 2 1 [(get-curve-data! driver "plot-surgery" year)
                                                           (get-curve-data! driver "plot-horm" year)
                                                           ]))))
       (range 0 16)))

(comment
  (web-benefits22! driver)
  (web-benefits2210! driver)
  )

;;;;;;;;;;;;;;;;;;;;;
(defn setup-for-test!
  "Before calling this, start a driver and start a copy of predict running at the given URL.
  Single arity version uses a local predict started by the repl"
  ([driver url]
   (exec! driver
          [[:load url]]))
  ([driver]
   (setup-for-test! driver "http://localhost:5449/index.html#/tool"))
  )

(defn alex-example
  "This is/was a false positive case. Passed, but should have failed."
  [driver]
  (setup-for-test! driver)
  (exec! driver pgen/alex-example)
  )

;;;;;;;;
;;;;;;
;;;;
;;

(defn approx=
  "is a approximately equal to b?"
  ([a b epsilon]
   (< (Math/abs (- a b)) epsilon))
  ([a b]
   (approx= a b 1e-6)))

(comment
  (setup-for-test! driver)

  (benefits22-sample driver)



  (ocpu/benefits2210 {:generation 3, :screen 1, :er 1, :ki67 1, :size 20, :nodes 10, :grade 2, :age.start 57, :traz 1,
                      :her2       1, :horm 0, :delay 0})
  (ocpu/benefits2210 {:generation 3, :screen 1, :er 1, :ki67 1, :size 20, :nodes 10, :grade 2, :age.start 57, :traz 1,
                      :her2       1, :horm 1, :delay 0})

  (ocpu/benefits2210 {:generation 3, :screen 1, :er 1, :ki67 1, :size 20, :nodes 10, :grade 2, :age.start 57, :traz 1,
                      :her2       1, :horm 0, :delay 1})
  (ocpu/benefits2210 {:generation 3, :screen 1, :er 1, :ki67 1, :size 20, :nodes 10, :grade 2, :age.start 57, :traz 1,
                      :her2       1, :horm 1, :delay 1})
  #_({:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0"}
     {:pi5 "0", :pi10 "0.399283297505937"}
     {:pi5 "0", :pi10 "0.771472565651221"}
     {:pi5 "0", :pi10 "1.11614119765249"}
     {:pi5 "0", :pi10 "1.4333236342269"}
     {:pi5 "0", :pi10 "1.72337764061056"})


  (defn web-example
    "This is/was a false positive case. Passed, but should have failed."
    [driver]
    (setup-for-test! driver)
    (exec! driver [[:reset]
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
                   [:press :bis :yes]
                   [:view :curves]])
    )

  (do
    (web-example driver)
    (web-benefits2210! driver))



  (ocpu/benefits2210 {:generation 3, :screen 1, :er 1, :ki67 1, :size 20, :nodes 10, :grade 2, :age.start 57, :traz 1, :bis 1,
                      :her2       1, :horm 1, :delay 0})


  (single driver)

  {
   :chemo              [3.9922982388692674 4],
   :horm               [4.9162684080631465 5]
   :tra                [2.2181546602934787 2],
   :bis                [0.9482220042501837 1],

   :horm+chemo         [8.908566646932414 9],
   :horm+chemo+tra     [11.126721307225893 11],
   :horm+chemo+tra+bis [12.074943311476076 12],

   :overall-surg       [69.37480599104177 69],
   :overall-horm       [74.29107439910491 74],
   :overall-chemo      [78.28337263797418 78],
   :overall-bis        [81.44974930251784 81],
   :overall-tra        [80.50152729826766 81],
   }
  )


(defn fill-in-inputs-and-treatments!
  "Run setup-for-test before this."
  [driver]
  (exec! driver (pgen/inputs-and-treatments)))

(comment
  (fill-in-inputs-and-treatments! driver)
  (dotimes [n 100] (println "n" n) (fill-in-inputs-and-treatments! driver))

  (exec! driver
         '([:reset] [:wait 0.1] [:press :dcis :no] [:enter :age 57] [:press :post-meno :yes] [:press :er-status :positive] [:press :her2-status :positive] [:press :ki67-status :unknown] [:enter :size 29] [:press :grade :1] [:press :mode :symptoms] [:enter :nodes 5] [:press :horm :h10] [:press :chemo :none] [:press :bis :no] [:press :delay :yes])
         )

  (exec! driver
         '([:reset] [:wait 0.1] [:press :dcis :no] [:enter :age 57] [:press :post-meno :yes] [:press :er-status :positive] [:press :her2-status :positive] [:press :ki67-status :positive] [:enter :size 20] [:press :grade :2] [:press :mode :symptoms] [:enter :nodes 5] [:press :horm :h10] [:press :chemo :3rd] [:press :bis :yes] [:press :tra :yes] [:press :delay :yes])
         )

  #_(exec! driver
         '([:reset] [:wait 0.1] [:press :dcis :no] [:enter :age 57] [:press :post-meno :yes] [:press :er-status :positive] [:press :her2-status :positive] [:press :ki67-status :unknown] [:enter :size 29] [:press :grade :1] [:press :mode :symptoms] [:enter :nodes 5] [:press :horm :h10] [:press :chemo :none] [:press :bis :yes] [:press :delay :yes])
         )
  )



(defn check-rounding-consistency!
  "OK - now we can start testing proper.

  Run this in the REPL for a soak test of the parameter space.
  Choose n > 1000 samples for reasonable parameter space coverage.

  Alternatively, use the shorter pure version in the integration-test namespace

  Plan is
  1. setup-for-test
  2. for [n number-of-tests]
  3. fill-in-inputs-and-treatments
  4. (check-all-views (get-all-results!))
  5. if errors exist, print them along with the input that caused them.

  We do still have occasional exceptions caused by input mis-timings, so we
  simply catch these, print an 'E' and continue testing.

  Run this in the REPL. Choose n > 100 for reasonable parameter space coverage.
  "
  [driver n]
  (setup-for-test! driver)
  (doseq [i (range n)]
    (let [inputs-and-treatments (pgen/inputs-and-treatments)
          h10-delay ((into #{} inputs-and-treatments) [:press :delay :yes])]
      (exec! driver inputs-and-treatments)
      (let [;checking at year 5 makes no sense if 5 years already gone by
            check-5 (if h10-delay
                      {}
                      (check-all-views (get-all-results! driver 5)))
            check-10 (check-all-views (get-all-results! driver 10))
            check-15 (check-all-views (get-all-results! driver 15))]
        (print (if (or (not= {} check-5 check-10 check-15))
                 (str {:inputs inputs-and-treatments
                       :5      check-5
                       :10     check-10
                       :15     check-15} "\n")
                 (str (inc i) " ")))
        (when (zero? (mod (inc i) 5)) (println)))

      )))

(defn forms-equal
  "A reducing function that compares form1 and form2 of the assertion. If they are the same, error is returned unchanged.
  If different an error message is constructed and conjd into the error map with the supplied key.
  If the assertion is nil error-m is returned unchanged"
  [error [k form1 form2 :as assertion]]
  (if (or (nil? assertion) (approx= form1 form2))
    error
    (conj error [k [form1 form2]])))

(defn check-against-r!
  "This has a similar structure to check-rounding-consistency!.

  You need to run both check-rounding-consistency! and check-against-r! for a complete validation.
  check-against-r! throws lots of random but valid inputs at an opencpu r server running PREDICT, and then
  throws the same parameters at the web site. It looks at ONLY the curves view on the web site as that allows
  results to be extracted from the user interface at reasonably high precision. It then checks those results against
  the R results.

  When this runs successfully, you know that the curves view is correct. You can then use check-rounding-consistency!
  to verify that the curves view agrees with all the other views.
  "
  [driver n]
  (setup-for-test! driver)
  (doseq [i (range n)]
    (try
      (let [sample (pgen/r-inputs-and-treatments)
            r-function (:r-function sample)
            r-params (:r-params sample)
            ]


        (exec! driver (conj (into [] (:cljs sample)) [:view :curves] [:wait 0.3]))

        (let [error (if (= r-function "benefits22")
                      (let [r-results (last (ocpu/benefits22 r-params))
                            [horm chemo tra bis] ((juxt :horm :generation :traz :bis) r-params)
                            delay (contains? (into #{} (:cljs sample)) [:press :horm :h10])
                            web-results (last (web-benefits22! driver))]
                        (reduce
                          forms-equal []
                          [[:surgery (read-string (:surg r-results)) (:z web-results)]
                           (if (and horm (pos? horm))
                             [:h
                              (read-string ((if delay :h10 :h) r-results))
                              (:h web-results)])
                           (when (and chemo (pos? chemo))
                             [:c
                              (- (read-string ((if delay :h10c :hc) r-results)) (read-string ((if delay :h10 :h) r-results)))
                              (:c web-results)])
                           (when (and tra (pos? tra))
                             [:t
                              (- (read-string ((if delay :h10ct :hct) r-results)) (read-string ((if delay :h10c :hc) r-results)))
                              (:t web-results)])
                           (when (and bis (pos? bis))
                             [:b
                              (if tra
                                (- (read-string ((if delay :h10ctb :hctb) r-results)) (read-string ((if delay :h10ct :hct) r-results)))
                                (if chemo
                                  (- (read-string ((if delay :h10cb :hcb) r-results)) (read-string ((if delay :h10c :hc) r-results)))
                                  (if horm
                                    (- (read-string ((if delay :h10b :hb) r-results)) (read-string ((if delay :h10 :h) r-results)))
                                    (read-string (:b r-results)))))

                              (:b web-results)]
                             )])
                        )
                      (reduce forms-equal []
                              [[:h10-h5
                                (read-string (:pi10 (last (ocpu/benefits2210 r-params))))
                                (:h5 (last (web-benefits2210! driver)))]])
                      )]
          (if (seq error)
            (do
              (println r-function "\n\n" r-function "error")
              (println "(def sample" (assoc sample :r-function r-function) ")")
              (println "(def r-params" r-params ")")
              (println "diffs")
              (pp/pprint error))
            (println (inc i)))))
      (catch Exception e (str (.getMessage e))))))


(comment
  ; RUN THESE IN THE REPL
  (check-against-r! driver 10000)                               ; check values against r
  (check-rounding-consistency! driver 10)                   ; check consistency of view calculations
  )

(comment

  (ocpu/benefits22 r-params)
  (gen/sample pgen/age)
  (gen/sample pgen/inputs-and-treatments)

  (reduce check-view [] [:table :chart :texts :icons])

  (exec! driver [[:load "http://localhost:5449/index.html#/tool"]
                 [:reset]
                 [:press :dcis :no]
                 [:enter :age 35]
                 [:press :post-meno :yes]
                 [:press :er-status :positive]
                 [:press :her2-status :positive]
                 [:press :ki67-status :unknown]
                 [:enter :size 42]
                 [:press :grade 2]
                 [:press :mode :screening]
                 [:enter :nodes 1]
                 [:wait 1]
                 [:press :micromets :yes]
                 [:press :horm :h5]
                 [:press :chemo :3rd]
                 [:press :tra :yes]
                 [:press :bis :yes]
                 [:view :table]
                 [:press :show-uncertainty :no]
                 [:press :result-year :5]
                 ])

  (get-all-results! driver 5)
  (def r (get-all-results! driver 5))
  (check-all-views (get-all-results! driver 5))
  (check-all-views r)
  (:curves (get-all-results! driver 5))


  ; single view tests.
  ; But maybe we should as it contains unrounded data!
  (get-results! driver :table)
  (get-results! driver :charts)
  (get-results! driver :texts)
  (get-results! driver :icons)

  ; low level tests
  (load-tool-page driver)
  (press-radio! driver :dcis :no)
  (press-radio! driver :dcis :yes)
  (press-radio! driver :post-meno :unknown)
  (press-radio! driver :post-meno :no)
  (press-radio! driver :post-meno :yes)
  (w/scroll-query driver [{:id ":ki67-status"}])
  (press-radio! driver :ki67-status :negative)
  (press-radio! driver :grade :3)
  (w/scroll-down driver)
  (w/scroll-up driver)
  (w/scroll-top driver)
  (w/fill driver [{:id :size}] 44)
  (enter-number! driver :age 33)
  (press-reset! driver)
  (press-radio! driver :horm :no)
  (press-radio! driver :horm :h5)
  (press-radio! driver :horm :h10)
  (press-radio! driver :delay :no)
  (press-radio! driver :delay :yes)
  (press-radio! driver :tra :yes)
  )