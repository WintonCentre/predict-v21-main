(ns predict.models.predict-test
  (:require [clojure.test :refer [deftest is testing]]
            [winton-utils.data-frame :refer [cell-apply cell-update cell-binary cell-binary-seq cell-sums cell-diffs map-of-vs->v-of-maps]]
            [predict.models.predict :refer [exp ln pow abs
                                            deltas rec-age-10-sq log-age-10
                                            r-base-br r-br r-base-oth r-oth detection-coeff grade-a her2-rh ki67-rh
                                            prognostic-index m-oth-prognostic-index
                                            base-m-cum-br valid-age years
                                            types-rx base-m-cum-oth*
                                            cljs-predict
                                            ]]
            [predict.models.s-cum-oth-rx :refer [s-cum-oth-rx*]]
            [predict.models.s-cum-br-rx :refer [s-cum-br-rx*]]
            ))



;
; test case inputs for cljs-predict, matching R configuration is:
;
(def inputs (into {}
              [
               [:bis :yes]
               [:age 25]
               [:radio nil]
               [:bis? true]
               [:tra :yes]
               [:ki67 1]
               [:chemoGen 3]
               [:size 2]
               [:radio? false]
               [:nodes 2]
               [:grade 1]
               [:erstat 1]
               [:rtime 15]
               [:her2 1]
               [:detection 1]
               [:horm :h5]
               [:her2-rh (her2-rh 1)]
               [:ki67-rh (ki67-rh 1 1)]
               [:delay 0]
               ]))



(def default-epsilon "default float tolerance" 1e-7)

(defn approx=
  ([a b epsilon]
   (< (abs (- a b)) epsilon))
  ([a b]
   (approx= a b default-epsilon)))

(defn approx=v
  ([a b epsilon]
   (if (not= (count a) (count b))
     false
     (every? (fn [[a* b*]] (approx= a* b* epsilon)) (map vector a b))))
  ([a b]
   (approx=v a b default-epsilon)))

(deftest simple-functions
  (testing "deltas"
    (is (= [1 1 2 4] (deltas 0 [1 2 4 8])))
    (is (= [0 1 2 4] (deltas 1 [1 2 4 8])))
    ))

(deftest rec-age-10-sq-test
  (testing "rec-age-10-sq"
    (is (= 1 (rec-age-10-sq 10)))
    (is (= 0.25 (rec-age-10-sq 20)))
    (is (approx= 0.16 (rec-age-10-sq 25)))
    )
  )

(deftest log-age-10-test
  (testing "log-age-10"
    (is (= 1 (log-age-10 (* js.Math.E 10)))))
  )

(deftest base-constants-test
  (is (= [0.133 0] [(r-base-br true) (r-base-br false)]))
  (is (= [-0.047 0] [(r-base-oth true) (r-base-oth false)]))
  (is (= [-0.198 0] [(r-br true) (r-br false)]))
  (is (= [0.068 0] [(r-oth true) (r-oth false)]))
  (is (= [0, 1, 0.204] (mapv detection-coeff (range 3))))
  (is (= [0 1 1 0] (mapv grade-a (range 1 5))))
  (is (= [-0.0762 0.2413] (mapv her2-rh [0 1])))
  (is (= [-0.11333 0.14904 0 0] (mapv #(apply ki67-rh %1) [[1 0] [1 1] [0 0] [0 1]])))
  )

(deftest prognostic-index-test
  (is (approx= 0.5200516 (prognostic-index inputs)))
  (is (approx= 0.5006471230275893 (prognostic-index {})))
  (is (= -0.5074627543042763 (prognostic-index {:age 25 :size 1 :nodes 1 :grade 1 :grade-a (grade-a 1) :detection 0 :her2_rh 0 :ki67_rh 0 :erstat 1 :radio? true})))
  (is (approx= -0.36397949827304354 (prognostic-index {:age 90 :size 10 :nodes 2 :grade 2 :detection 0 :her2_rh 0 :ki67_rh 0 :erstat 0 :radio? false})))
  (is (approx= -0.8680964143042763 (prognostic-index {:age 25 :size 1 :nodes 1 :grade 1 :detection 1 :her2_rh 1 :ki67_rh 1 :erstat 1 :radio? false})))
  )

(deftest m-oth-prognostic-index-test
  (is (approx= 0.5597244192408362 (m-oth-prognostic-index 65 false)))
  (is (approx= 0.5127244192408361 (m-oth-prognostic-index 65 true)))
  )

(deftest base-m-cum-br-test
  (testing "base-m-cum-br"
    (is (approx= 0.015012789617578808 (base-m-cum-br 0 1)))
    (is (approx= 0.0011302439325964351 (base-m-cum-br 1 1)))
    (is (approx= 0.05193237928632519 (base-m-cum-br 1 10)))
    (is (approx= 0.22253215565946607 (base-m-cum-br 0 10)))
    ))

(deftest age-test
  (testing "age-clamp"
    (is (= 25 (valid-age 16)))
    (is (= 30 (valid-age 30))))
  )

(deftest detection-test
  (testing "detection"
    (is (zero? (detection-coeff 0)))
    (is (= 1 (detection-coeff 1)))
    (is (= 0.204 (detection-coeff 2)))))

(deftest grade-a-test
  (testing "grade-a"
    (is (= 0 (grade-a 1)))
    (is (= 1 (grade-a 2)))
    (is (= 1 (grade-a 3))))
  )

(deftest her2-rh-test
  (testing "her2-rh"
    (is (= 0.2413 (her2-rh 1)))
    (is (= -0.0762 (her2-rh 0))))
  )

(deftest ki67-rh-test
  (testing "ki67-rh"
    (is (= 0 (ki67-rh 0 10)))
    (is (= 0.14904 (ki67-rh 1 1)))
    (is (= -0.11333 (ki67-rh 1 0)))
    )
  )

(deftest years-test
  (testing "years"
    (is (= [0 1 2 3 4 5 6 7 8 9 10] (years 10 0)))
    (is (= [0 1 2 3 4 5] (years 10 5)))
    (is (= [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15] (years 15 0)))
    (is (= [0 1 2 3 4 5 6 7 8 9 10] (years 15 5)))))


(deftest types-rx-test
  (testing "types-rx"

    (is (= {:r-low 0, :h-high 0, :r 0, :hr 0, :b-high 0, :hrc-high -0.579, :hrct -0.446, :hrctb-high -0.446, :h-low 0, :hrc-low -0.313, :c-low -0.313, :hrct-low -0.446, :hr-low 0, :hrc -0.446, :hrctb-low -0.446, :z 0, :c -0.446, :hrct-high -0.446, :hrctb -0.446, :h 0, :t-high 0, :b 0, :c-high -0.579, :t 0, :hr-high 0, :b-low 0, :t-low 0, :r-high 0}
          (types-rx {:erstat 0 :her2 0 :horm :h5 :chemoGen 3 :radio? false :bis? false :bis 1 :tra 1} 0)) "0 :h5 0")

    (is (= {:r-low 0, :h-high -0.502, :r 0, :hr -0.3857, :b-high -0.32, :hrc-high -0.9646999999999999, :hrct -1.1884000000000001, :hrctb-high -1.5084000000000002, :h-low -0.212, :hrc-low -0.6987, :c-low -0.313, :hrct-low -1.0707, :hr-low -0.3857, :hrc -0.8317, :hrctb-low -1.2584000000000002, :z 0, :c -0.446, :hrct-high -1.3647, :hrctb -1.3864, :h -0.3857, :t-high -0.533, :b -0.198, :c-high -0.579, :t -0.3567, :hr-high -0.3857, :b-low -0.07, :t-low -0.239, :r-high 0}
          (types-rx {:erstat 1 :her2 1 :horm :h5 :chemoGen 3 :radio? false :bis? true :bis 1 :tra 0}
            0)) "1 :h5 0")

    (is (= {:r-low 0, :h-high 0, :r 0, :hr 0, :b-high -0.32, :hrc-high -0.579, :hrct -0.8027, :hrctb-high -1.1227, :h-low 0, :hrc-low -0.313, :c-low -0.313, :hrct-low -0.685, :hr-low 0, :hrc -0.446, :hrctb-low -0.8727, :z 0, :c -0.446, :hrct-high -0.9790000000000001, :hrctb -1.0007, :h 0, :t-high -0.533, :b -0.198, :c-high -0.579, :t -0.3567, :hr-high 0, :b-low -0.07, :t-low -0.239, :r-high 0}
          (types-rx {:erstat 1 :her2 1 :horm nil :chemoGen 3 :radio? false :bis? true :bis 1 :tra 0}
            11)) "1 nil 11")

    (is (= {:r-low 0, :h-high -0.502, :r 0, :hr -0.3857, :b-high -0.32, :hrc-high -0.9646999999999999, :hrct -1.1884000000000001, :hrctb-high -1.5084000000000002, :h-low -0.212, :hrc-low -0.6987, :c-low -0.313, :hrct-low -1.0707, :hr-low -0.3857, :hrc -0.8317, :hrctb-low -1.2584000000000002, :z 0, :c -0.446, :hrct-high -1.3647, :hrctb -1.3864, :h -0.3857, :t-high -0.533, :b -0.198, :c-high -0.579, :t -0.3567, :hr-high -0.3857, :b-low -0.07, :t-low -0.239, :r-high 0}
          (types-rx {:erstat 1 :her2 1 :horm :h5 :chemoGen 3 :radio? false :bis? true :bis 1 :tra 0}
            11)) "1 :h5 11")

    (is (= {:r-low 0, :h-high -0.8440000000000001, :r 0, :hr -0.7277, :b-high -0.32, :hrc-high -1.3067, :hrct -1.5304, :hrctb-high -1.8504, :h-low -0.554, :hrc-low -1.0407, :c-low -0.313, :hrct-low -1.4127, :hr-low -0.7277, :hrc -1.1737, :hrctb-low -1.6004, :z 0, :c -0.446, :hrct-high -1.7067, :hrctb -1.7284, :h -0.7277, :t-high -0.533, :b -0.198, :c-high -0.579, :t -0.3567, :hr-high -0.7277, :b-low -0.07, :t-low -0.239, :r-high 0}
          (types-rx {:erstat 1 :her2 1 :horm :h10 :chemoGen 3 :radio? false :bis? true :bis 1 :tra 1}
            15)) "1 :h10 15")
    ))
;
;
; Comparisons with intermediate calculations found in the R environment
;
(def pi (prognostic-index {:age       25
                           :size      2
                           :nodes     2
                           :grade     1
                           :detection 1
                           :her2_rh   1
                           :ki67_rh   1
                           :erstat    1
                           :radio?    false
                           :her2-rh   (her2-rh 1)
                           :ki67-rh   (ki67-rh 1 1)}))
(def mi (m-oth-prognostic-index 25 false))
(def times (years 15 0))

(def base-m-cum-oth (base-m-cum-oth* times))

(deftest base-m-cum-oth*-test
  (testing "base-m-cum-oth* R:121"
    (is (approx=v '(0 0.003255573 0.007875150 0.013531566 0.020144040 0.027680291 0.036128375 0.045487139 0.055761969 0.066962590 0.079101799 0.092194686 0.106258128 0.121310438 0.137371129 0.154460731) base-m-cum-oth)))
  )

(def s-cum-oth (mapv #(exp (* (- (exp mi)) %)) base-m-cum-oth))
(deftest s-cum-oth-test
  (is (approx=v
        [1 0.9995388 0.9988846 0.9980843 0.9971495 0.9960851 0.9948934 0.9935748 0.9921292 0.9905557 0.9888532 0.9870202 0.9850551 0.9829562 0.9807216 0.9783494]
        s-cum-oth) "s-cum-oth"))

(def base-m-oth (deltas 0 base-m-cum-oth))
(deftest base-m-oth-test
  (is (approx=v
        [0 0.003255573 0.004619576 0.005656416 0.006612474 0.007536251 0.008448084 0.009358764 0.010274830 0.011200621 0.012139209 0.013092888 0.014063442 0.015052310 0.016060690 0.017089602]
        base-m-oth) "base-m-oth"))

(def m-cum-oth [0
                0.00046123617167015407
                0.001115353579987044
                0.0019157013210695517
                0.0028505123043990332
                0.003914850840574191
                0.005106616419662813
                0.006425186276472705
                0.007870809602329554
                0.009444291529239424
                0.011146810094146975
                0.012979801759237075
                0.014944885371467098
                0.017043809091435103
                0.019278411749743873
                0.02165059363589017])
(deftest m-cum-oth-test
  (is (= m-cum-oth (mapv (fn [tm] (- 1 (nth s-cum-oth tm))) times))))

(def m-oth [0
            0.00046123617167015407
            0.0006541174083168899
            0.0008003477410825077
            0.0009348109833294815
            0.0010643385361751578
            0.0011917655790886217
            0.0013185698568098925
            0.0014456233258568485
            0.0015734819269098699
            0.0017025185649075514
            0.0018329916650901001
            0.001965083612230023
            0.0020989237199680044
            0.002234602658308771
            0.002372181886146296])
(deftest m-oth-test
  (is (approx=v m-oth (deltas 0 m-cum-oth))))

(deftest cljs-predict-test
  (testing "v2.1 model"
    (is (approx=v (map #(/ % 100) [0 0.1423600 0.5289672 1.0759822
                                   1.7200768 2.4213923 3.1543309 3.9019877
                                   4.6529417 5.3993493
                                   6.135767218134225
                                   6.85840054034158
                                   7.564607730104955
                                   8.252566579852982
                                   8.921043783391924
                                   9.569233116719345])

          (map :hrctb (cljs-predict
                        {:age       25
                         :size      2
                         :nodes     2
                         :grade     1
                         :erstat    1
                         :detection 1
                         :her2      1
                         :ki67      1
                         :rtime     15
                         :chemoGen  3
                         :bis?      true
                         :bis       1
                         :radio?    false
                         :radio     0
                         :horm      :h5
                         :tra       1
                         :delay     0
                         })))))
  )

(def radio? false)
;
; todo: Sort out types-rx - we are calculating far too much here on each call...
;
(def types-rx-curry (partial types-rx {:erstat 1 :her2 1 :horm :h10 :chemoGen 3 :radio? false :bis? true :bis 1 :tra 1}))


(def types (map first (types-rx-curry 0)))

(deftest types-test
  (is (= '(:r-low :h-high :r :hr :b-high :hrc-high :hrct :hrctb-high :h-low :hrc-low :c-low :hrct-low :hr-low :hrc :hrctb-low :z :c :hrct-high :hrctb :h :t-high :b :c-high :t :hr-high :b-low :t-low :r-high)
        types)))

(def rx-oth (->> types
              (map (fn [type] [type (if (and radio? (some #{"r"} (name type))) r-oth 0)]))
              (into {})))
(deftest rx-oth-test
  (is (= {:r-low      0,
          :h-high     0,
          :r          0,
          :hr         0,
          :b-high     0,
          :hrc-high   0,
          :hrct       0,
          :hrctb-high 0,
          :h-low      0,
          :hrc-low    0,
          :c-low      0,
          :hrct-low   0,
          :hr-low     0,
          :hrc        0,
          :hrctb-low  0,
          :z          0,
          :c          0,
          :hrct-high  0,
          :hrctb      0,
          :h          0,
          :t-high     0,
          :b          0,
          :c-high     0,
          :t          0,
          :hr-high    0,
          :b-low      0,
          :t-low      0,
          :r-high     0}
        rx-oth)))

(def xf-m-oth-rx (fn [type]
                   [type (map (fn [tm]
                                (* (base-m-oth tm) (exp (+ mi (type rx-oth)))))
                           times)]))
(def s-cum-oth-rx (into {}
                    (comp
                      (map xf-m-oth-rx)                     ; -> m-oth-rx               R 126
                      (map cell-sums)                       ; -> m-cum-oth-rx (state 1) R 140
                      (map (cell-apply #(->> % (-) (exp))))) ; -> s-cum-oth-rx        R 143

                    types))

(deftest s-cum-oth-rx-test
  (testing "s-cum-oth-rx-test with h10"
    (is (= s-cum-oth-rx* s-cum-oth-rx))
    )
  )

(def erstat 1)

(def base-m-br
  (->> times                                                ;base.m.br (ok)   R 200, S
    (map (partial base-m-cum-br erstat))
    (deltas 0)))

(deftest base-m-br-test
  (testing "base-m-br"
    (is (approx=v [0 0.001130244 0.003085806 0.004406595 0.005250795 0.005796647 0.006150640 0.006376906 0.006515610 0.006592877 0.006626260 0.006627880 0.006606300 0.006567684 0.006516544 0.006456227]
          base-m-br))))


(def m-br-rx-xf-1
  (fn [type]
    [type (map-indexed #(* (exp (+ (type (types-rx-curry %1)) pi)) %2) base-m-br)]))

(def m-br-rx-1
  (into {}
    (comp
      (map m-br-rx-xf-1)                                    ; -> m-br-rx       R 251
      )
    types))

(deftest m-br-rx-1-test
  (testing "m-br-rx with h10"
    (is (= 0.0027862375961101257
          (nth (:hrctb m-br-rx-1) 10)))
    (is (= 0.001928411632440143
          (nth (:hrctb m-br-rx-1) 15))))
  )

(def s-cum-br-rx
  (into {}
    (comp
      (map m-br-rx-xf-1)                                    ; -> m-br-x       R 251
      (map cell-sums)                                       ; -> m-cum-br-rx  R 178
      (map (cell-apply #(->> % (-) (exp))))
      )                                                     ; -> s-cum-br-rx R 181
    types))

(deftest s-cum-br-rx-test
  (testing "s-cum-br-rx"
    (is (= 0.9783999507570005 (nth (:hrctb s-cum-br-rx) 10)))
    (is (= 0.9688686708023612 (nth (:hrctb s-cum-br-rx) `15)))))

(def m-br-rx
  (into {}
    (comp
      (map (cell-apply #(- 1 %)))                           ; -> m-cum-br-rx  R 184
      (map (cell-diffs 0)))                                 ; -> m-br-rx      R 187
    s-cum-br-rx))

(deftest m-br-rx-test
  (testing "m-br-rx"
    (is (approx= 0.0004751368 (nth (:hrctb m-br-rx) 1)))
    (is (approx= 0.0027298560 (nth (:hrctb m-br-rx) 10)))
    (is (approx= 0.0018701803 (nth (:hrctb m-br-rx) 15)))
    ))

(def m-all-rx
  (into {}
    (comp
      (map (cell-binary #(- 1 (* %1 %2)) s-cum-br-rx))
      (map (cell-diffs 0)))
    s-cum-oth-rx))

(deftest m-all-rx-test
  (testing "m-all-rx"
    (is (approx= 0.0009361538 (nth (:hrctb m-all-rx) 1)))
    (is (approx= 0.0043698185 (nth (:hrctb m-all-rx) 10)))
    (is (approx= 0.0041324589 (nth (:hrctb m-all-rx) 15)))))

(def pred-m-br-rx
  (into {}
    (comp
      (map (cell-update (fn [type tm old] (if (> tm 0) (/ old (+ old (nth m-oth tm))) 0))))
      (map (cell-binary * m-all-rx))
      )
    m-br-rx))

(deftest pred-m-br-rx-test
  (testing "pred-m-br-rx"
    (is (approx= 0.0004750256 (nth (:hrctb pred-m-br-rx) 1)))
    (is (approx= 0.0026913283 (nth (:hrctb pred-m-br-rx) 10)))
    (is (approx= 0.0018217311 (nth (:hrctb pred-m-br-rx) 15)))))

(def pred-cum-br-rx
  (into {}
    (map cell-sums)
    pred-m-br-rx))

(deftest pred-cum-br-rx-test
  (testing "pred-cum-br-rx"
    (is (approx= 0.0004750256 (nth (:hrctb pred-cum-br-rx) 1)))
    (is (approx= 0.0214437363 (nth (:hrctb pred-cum-br-rx) 10)))
    (is (approx= 0.0307704989 (nth (:hrctb pred-cum-br-rx) 15)))
    ))

(def pred-cum-all-rx
  (into {}
    (comp
      (map (cell-binary #(- %2 %1) pred-m-br-rx))           ;pred-m-oth-rx R 203
      (map cell-sums)                                       ; pred-cum-oth-rx R204
      (map (cell-binary + pred-cum-br-rx))
      )                                                     ; pred-cum-all-rx R 205
    m-all-rx))

(deftest pred-cum-all-rx-test
  (testing "pred-cum-all-rx"
    (is (approx= 0.0009361538 (nth (:hrctb pred-cum-all-rx) 1)))
    (is (approx= 0.0325060877 (nth (:hrctb pred-cum-all-rx) 10)))
    (is (approx= 0.0521079111 (nth (:hrctb pred-cum-all-rx) 15)))
    ))

(def surg-only (map #(- 1 %) (:z pred-cum-all-rx)))
(deftest surg-only-test
  (testing "surg-only"
    (is (approx= (- 1 0.002359754) (nth surg-only 1)))
    (is (approx= (- 1 0.093863760) (nth surg-only 10)))
    (is (approx= (- 1 0.151576452) (nth surg-only 15)))
    ))

(def benefits2-1
  (assoc (into {}
           (map (cell-binary-seq - (:z pred-cum-all-rx)))
           pred-cum-all-rx)
    :z surg-only
    :oth (:z s-cum-oth-rx)))

(deftest benefits2-1-test
  (testing "benefits2-1"
    (is (approx= (/ 0.14236 100) (nth (:hrctb benefits2-1) 1)))
    (is (approx= (/ 6.1357672 100) (nth (:hrctb benefits2-1) 10)))
    (is (approx= (/ 9.9468541 100) (nth (:hrctb benefits2-1) 15)))
    ))

;
;
;

#_(deftest cljs-predict-h5-test
    (testing "h10 model"
      (is (= '(0
                0.0014235998353466783
                0.005289672012324664
                0.010759822493926374
                0.017200767587482346
                0.024213922849740006
                0.0315433093601687
                0.0390198769320368
                0.046529416795973236
                0.05399349285357215
                0.06135767218134225
                0.0685840054034158
                0.07564607730104955
                0.08252566579852982
                0.08921043783391924
                0.09569233116719345)

            (:hrctb (:benefits2-1 (cljs-predict
                                    {:age       25
                                     :size      2
                                     :nodes     2
                                     :grade     1
                                     :erstat    1
                                     :detection 1
                                     :her2      1
                                     :ki67      1
                                     :rtime     15
                                     :chemoGen  3
                                     :bis?      true
                                     :bis       1
                                     :radio?    false
                                     :radio     0
                                     :horm      :h5
                                     :tra       1
                                     }))))))
    )


#_(deftest cljs-predict-h10-test
    (testing "h10 model"
      (is (= '(0
                0.0014235998353466783
                0.005289672012324664
                0.010759822493926374
                0.017200767587482346
                0.024213922849740006
                0.0315433093601687
                0.0390198769320368
                0.046529416795973236
                0.05399349285357215
                0.06135767218134225
                0.0685840054034158
                0.07564607730104955
                0.08252566579852982
                0.08921043783391924
                0.09569233116719345)

            (:hrctb (:benefits2-1 (cljs-predict
                                    {:age       25
                                     :size      2
                                     :nodes     2
                                     :grade     1
                                     :erstat    1
                                     :detection 1
                                     :her2      1
                                     :ki67      1
                                     :rtime     15
                                     :chemoGen  3
                                     :bis?      true
                                     :bis       1
                                     :radio?    false
                                     :radio     0
                                     :horm      true
                                     :tra       1
                                     }))))))
    )

