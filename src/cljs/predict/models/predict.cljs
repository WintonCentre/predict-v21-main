(ns predict.models.predict
  "A cljs version of the predict model, enhanced with radiotherapy and bisphosphonates, and extended to 15 years."
  (:require [cljs.pprint :as pp]
            [winton-utils.data-frame :refer [cell-apply cell-update cell-binary cell-binary-seq cell-sums cell-diffs map-of-vs->v-of-maps]]
            ))

(enable-console-print!)


(defn psym [n s]
  ;(printff (str n "%") s)
  )

(defn expand
  "Expand a vector of survival by year by repeating it for each type. Return a map by type"
  [types vs]
  (into {} (map (fn [k] [k vs]) types)))


(def exp Math/exp)
(def ln Math/log)
(def pow Math/pow)
(def abs Math/abs)

(defn deltas [start v]
  "Calculate deltas of a seq, inserting start as the first value to compare"
  (into [] (map (fn [[a b]] (- b a)) (partition 2 1 (cons start v)))))

(defn rec-age-10-sq
  "1/(age/10)^2"
  [age]
  (pow (/ age 10) -2.0))

(defn log-age-10
  "log(age/10)"
  [age]
  (ln (/ age 10)))

(defn r-base-br
  "R: r.base.br
  Base breast cancer mortality coefficient for radiotherapy enabled or disabled."
  [radio?]
  (if radio? 0.133 0))                                      ;

(defn r-base-oth
  "R: r.base.oth
  Base other cause mortality coefficient"
  [radio?]
  (if radio? -0.047 0))                                     ;

#_(def dfs 0.023)                                           ; not for this version

(defn r-br
  "R: r.br breast mortality coefficient for radiotherapy treatment if enabled"
  [radio?]
  (if radio? -0.198 0))

(defn r-oth
  [radio?]
  "R: r.oth other mortality coefficient for radiotherapy treatment if enabled"
  (if radio? 0.068 0))

(defn prognostic-index
  "Calculate the breast cancer mortality prognostic index (pi).
  Comments relate this code to the corresponding R variables."
  [{:keys [age size nodes grade erstat detection her2-rh ki67-rh grade-a radio? bis?]
    :or   {age 65 size 19 nodes 1 grade 1 erstat 1 detection 0 her2-rh -0.0762 ki67-rh -0.11333 grade-a 0 radio? false bis? true}
    :as   pi-inputs}]
  #_(println "pi inputs: " pi-inputs)

  (+
    her2-rh                                                 ; -0.0762 (ok)
    ki67-rh                                                 ; -0.11333 (ok)
    (r-base-br radio?)                                      ; adjust baseline for radiotherapy (r.base.br)
    (if (pos? erstat)
      (+
        (* 34.53642 (+ (rec-age-10-sq age) -0.0287449295))  ; age.beta.1 * age.mfp.1 (er==1) (ok)
        (* -34.20342                                        ; age.beta.2 (er==1) (ok)
          (+ (* (rec-age-10-sq age)                         ; * age.mfp.2 (er==1) (ok)
               (log-age-10 age))
            -0.0510121013))
        (* 0.7530729                                        ; size.beta (er==1) (ok)
          (+ (ln (/ size 100))                              ; * size.mfp (er==1) (ok)
            1.545233938))
        (* 0.7060723                                        ; nodes.beta (er==1) (ok)
          (+ (ln (/ (inc nodes) 10))                        ; * nodes.mfp (er==1) (ok)
            1.387566896))
        (* 0.746655 grade)                                  ; grade.beta (er==1) (ok)
        (* -0.22763366 detection))                          ; screen.beta (er==1) (ok)

      (+
        (* 0.0089827 (- age 56.3254902))                    ; age.beta.1 * age.mfp.1 (er==0)
        (* 2.093446 (+ (pow (/ size 100) 0.5) -0.5090456276)) ; size.beta * size.mfp (er==0)
        (* 0.6260541 (+ (ln (/ (inc nodes) 10)) 1.086916249)) ; nodes.beta * nodes.mfp (er==0)
        (* 1.129091 grade-a)))))                            ; grade.beta * grade.val (er==0)


(defn m-oth-prognostic-index [age radio?]                   ; mi (Shiny R 130)/R 67
  "Calculate the other mortality prognostic index"
  (+ (* 0.0698252 (+ (pow (/ age 10) 2) -34.23391957)) (r-base-oth radio?)))

(defn base-m-cum-br
  "Generate cumulative baseline breast mortality R 194"
  [erstat tm]
  (if (pos? tm)
    (exp
      (if (pos? erstat)
        (+ 0.7424402
          (* -7.527762
            (pow (/ 1.0 tm) 0.5))
          (* -1.812513
            (pow (/ 1.0 tm) 0.5)
            (ln tm)))
        (+ -1.156036
          (/ 0.4707332 (pow tm 2))
          (/ -3.51355 tm))))

    0))


(defn valid-age [y] (if (< y 25) 25 y))

(defn detection-coeff [d] ([0, 1, 0.204] d))

(defn grade-a
  [grade]
  (if (#{2 3} grade) 1 0))

(defn her2-rh
  [her2]
  (condp = her2
    1 0.2413                                                ;her2.beta (er==1)
    0 -0.0762                                               ;her2.beta (er==0)
    0))

(defn ki67-rh
  [erstat ki67]
  (if (pos? erstat)
    (condp = ki67
      1 0.14904                                             ;ki67.beta (er==1 && ki67)
      0 -0.11333                                            ;ki67.beta (er==1 && not ki67)
      0)                                                    ;ki67.beta (all other cases)
    0))

(defn types-rx
  "Calculate treatment coefficients
  radio indicates radiotherapy is available in the interface and selected
  bis indicates bisphosphonates is available in the interface and selected
  c = chemo, h = hormone therapy, t = trastuzumab, r = radiotherapy, b = bisphosphonates

  Note that we _always_ calculate the same columns, but if a treatment is _not_ selected, then
  its associated treatment coefficients will be zero.

  e.g. The treatment combination hcb will be calculated as hrctb, but with r and c coefficients zeroed.

  We have 4 combinations of hormone and delay to worry about
  horm :h5, :delay 0    we calculate years (range 0 16) using (repeat 15 :h5). We tabulate ALL treatment benefits over surgery
  horm :h10 :delay 0    we calculate years (range 0 16) using (concat (repeat 10 :h5) (repeat 5 :h10)). We tabulate ALL treatment benefits over surgery

  horm :h5  :delay 5    we calculate years (range 0 11) using (repeat 10 :h5). We tabulate h5 and sum of all others (:z + :rctb)  ??
  horm :h10 :delay 5    we calculate years (range 0 11) using (repeat 10 :h10). We tabulate h10 and sum of all others (:z + :rctb) ??

  Times:
  if delay = 5, then time == 0 corresponds to year 5.
  We can drop 5 and zero the new first in time arrays to preserve the same algorithm as for 15 years
  Age at surgery has not changed and neither does the mortality index."


  [{:keys [erstat her2 horm chemoGen radio? radio bis? bis tra delay]} time]

  (let [h-plus -0.26                                       ;-0.342                                       ;-0.2
        h-plus-vec [-0.178 h-plus 0.2]
        z-vec [0 0 0]

        c-vec (condp = chemoGen
                2 [-0.360 -0.248 -0.136]
                3 [-0.579 -0.446 -0.313]
                z-vec)
        [c-high c c-low] c-vec


        h-vec* (if (and (pos? erstat) (#{:h5 :h10 :yes} horm))
                 [-0.502 -0.3857 -0.212]
                 z-vec)

        h-vec (if (and (pos? erstat)
                       (> time (if (zero? delay) (- 10 delay) (- 9 delay)))
                    (= :h10 horm))
                (mapv + h-plus-vec h-vec*)
                h-vec*)
        [h-high h h-low] h-vec

        hh-vec (mapv + h-plus-vec h-vec*)

        t-vec (if (and (= her2 1) tra)
                [-0.533 -0.3567 -0.239]
                z-vec)
        [t-high t t-low] t-vec

        r-vec (if (and radio? radio)
                [-0.288 -0.198 -0.105]
                z-vec)
        [r-high r r-low] r-vec

        b-vec (if (and bis? bis)
                [-0.32 -0.198 -0.07]
                z-vec)
        [b-high b b-low] b-vec

        hr-vec (mapv #(+ h %) r-vec)
        [hr-high hr hr-low] hr-vec

        hrc-vec (mapv #(+ h r %) c-vec)
        [hrc-high hrc hrc-low] hrc-vec

        hrct-vec (mapv #(+ h r c %) t-vec)
        [hrct-high hrct hrct-low] hrct-vec

        hrctb-vec (mapv #(+ h r c t %) b-vec)
        [hrctb-high hrctb hrctb-low] hrctb-vec

        ]

    ; change this if the presentation of treatment order changes
    {:z     0                                               ; surgery only
     :h     h :h-low h-low :h-high h-high
     :r     r :r-low r-low :r-high r-high
     :c     c :c-low c-low :c-high c-high
     :t     t :t-low t-low :t-high t-high
     :b     b :b-low b-low :b-high b-high
     :hr    hr :hr-low hr-low :hr-high hr-high
     :hrc   hrc :hrc-low hrc-low :hrc-high hrc-high
     :hrct  hrct :hrct-low hrct-low :hrct-high hrct-high
     :hrctb hrctb :hrctb-low hrctb-low :hrctb-high hrctb-high}))


(defn years [rtime delay]
  (range (inc (- (Math/round rtime) delay))))

(defn base-m-cum-oth*
  [times]
  (map #(exp (+ -6.052919 (* 1.079863 (ln %)) (* 0.3255321 (pow % 0.5)))) times))

(def d-drop 6)

(defn cljs-predict
  "clojure/script implementation of predict-v2 model.

  Predicts survival based on patient input parameters.

  Arguments age, size and nodes are entered as values; the others as lookups
  # This is how the model assigns some input parameters (or ranges) into variables
  # i.e. parameter (or ranges) -> web form setting -> Predict model variable setting
  # Tumour Grade (1,2,3,unknown) -> (1,2,3,9) -> (1.0,2.0,3.0,2.13)
  # ER Status (-ve,+ve) -> (0,1) -> (0,1) n.b. unknown not allowed
  # Detection (Clinical,Screening,Other) -> (0,1,2) -> (0.0,1.0,0.204)
  # HER2 Status (-ve,+ve,unknown) -> (0,1,9)
  # KI67 Status (-ve,+ve,unknown) -> (0,1,9)

  We are now passing in the selected treatments so we don't have to calculate all possible
  treatment combinations on each call. Instead, we calculate the treatment combinations that
  could make up the current set in hrctb order

  This means that if we see non-null horm, chemoGen, bis values, we will calculate
  h, hc, hb, hcb only.

  Note:
  For uncertainties in the coefficients h,c,t etc, see docs/Predictv2-uncertainties.docx
  "

  [{:keys [age size nodes grade erstat detection her2 ki67 rtime radio? bis? chemoGen horm radio bis tra delay]
    :as   inputs}]

  ;; Note R reference is

  #_(println "inputs")

  (let [d? (= delay 5)
        age (valid-age age)                                 ;(+ (valid-age age) delay)
        detection (detection-coeff detection)
        grade ([1, 2, 3, 2.13] (if (= grade 9) 3 (dec grade)))
        grade-a (grade-a grade)
        her2-rh (her2-rh her2)
        ki67-rh (ki67-rh erstat ki67)
        ;chemo (pos? chemoGen)

        types-rx-curry (partial types-rx inputs)            ; Note this is where bis horm radio and ta are used

        pi (prognostic-index {:age       age
                              :size      size
                              :nodes     nodes
                              :grade     grade
                              :grade-a   grade-a
                              :erstat    erstat
                              :detection detection
                              :her2-rh   her2-rh
                              :ki67-rh   ki67-rh
                              :radio?    radio?})

        ;_ (psym "pi" pi)

        mi (m-oth-prognostic-index age radio?)              ;ok
        ;_ (psym "mi" mi)

        times (range 0 16)                                  ;(years rtime delay)
        yrs (years rtime delay)
        ;_ (psym "ytime" (years rtime delay))
        ;times-15 (range 0 16)
        ;_ (psym "times" times)

        types (map first (types-rx-curry 0))                ; treatment type keys


        ;------
        ; Generate cumulative baseline other mortality       base.m.cum.oth R 121
        base-m-cum-oth (base-m-cum-oth* times)
        ;_ (psym "base-m-cum-oth" base-m-cum-oth)

        base-m-oth (deltas 0 base-m-cum-oth)                ;R 125
        ;_ (psym "base-m-oth" base-m-oth)

        ; Generate cumulative survival non-breast mortality  s.cum.oth R 124
        s-cum-oth (map #(exp (* (- (exp mi)) %)) base-m-cum-oth) ;; NOT FOR DELAY

        s-cum-oth-rx s-cum-oth                              ; to be deleted
        ;_ (psym "s-cum-oth-rx" s-cum-oth)

        m-cum-oth (mapv (fn [tm] (- 1 (nth s-cum-oth tm))) times) ; m.cum.oth (ok)
        m-cum-oth-rx m-cum-oth                              ; for delay
        ;_ (psym "m-cum-oth-rx" m-cum-oth)

        m-oth (deltas 0 m-cum-oth)                          ;m.oth.rx -- yearly mortality, other
        ;_ (psym "m-oth" m-oth)
        ;m-oth-rx m-oth


        m-oth-10 (drop d-drop m-oth)                        ; delayed version
        ;_ (psym "m-oth-10" m-oth-10)

        m-oth- ((if d? #(drop d-drop %) identity) (deltas 0 m-cum-oth))
        ;_ (psym "m-oth-" m-oth-)                            ; $$$$$

        m-cum-oth-10 (reductions + m-oth-10)
        ;_ (psym "m-cum-oth-10" m-cum-oth-10)
        m-cum-oth- (reductions + m-oth-)

        s-cum-oth-10 (map #(- 1 %) m-cum-oth-10)
        ;_ (psym "s-cum-oth-10" s-cum-oth-10)

        s-cum-oth- (map #(- 1 %) m-cum-oth-)
        ;_ (psym "s-cum-oth-" s-cum-oth-)                    ; $$$$$



        r-oth (r-oth radio?)

        rx-oth (->> types
                 (map (fn [type] [type (if (and radio? (some #{"r"} (name type))) r-oth 0)]))
                 (into {}))

        xf-m-oth-rx (fn [type]
                      [type (map (fn [tm]
                                   (* (base-m-oth tm) (exp (+ mi (type rx-oth)))))
                              times)])

        xf-m-oth-rx- (fn [type]
                       [type (map (fn [tm]
                                    (* (base-m-oth tm) (exp (+ mi (type rx-oth)))))
                                  yrs)])

        s-cum-oth-rx (into {}
                       (comp
                         (map xf-m-oth-rx)                  ; -> m-oth-rx
                         (map cell-sums)                    ; -> m-cum-oth-rx (state 1)
                         (map (cell-apply #(->> % (-) (exp))))) ; -> s-cum-oth-rx        R 171
                       types)
        ;_ (psym "s-cum-oth-rx2" (:hrctb s-cum-oth-rx))      ; CHECKS

        s-cum-oth-rx- (if d?
                        (expand types s-cum-oth-)
                        (into {}
                       (comp
                                (map xf-m-oth-rx-)          ; -> m-oth-rx
                                (map cell-sums)             ; -> m-cum-oth-rx (state 1)
                                (map (cell-apply #(->> % (-) (exp))))) ; -> s-cum-oth-rx        R 171
                              types))

        ;_ (psym "s-cum-oth-rx-" (:hrctb s-cum-oth-rx-))


        ;------
        ; Generate annual baseline breast mortality
        ; R 161
        base-m-br (->> times                                ;base.m.br (ok)   R 200, S
                    (map (partial base-m-cum-br erstat))
                    (deltas 0))
        ;_ (psym "base-m-br" base-m-br)

        base-m-br-delayed (drop d-drop base-m-br)
        ;_ (psym "base-m-br-delayed " base-m-br-delayed)

        base-m-br- ((if d? #(drop d-drop %) identity) base-m-br)
        ;_ (psym "base-m-br- " base-m-br-)                   ; $$$$$


        m-br-rx-xf-1 (fn [type]
                       [type (map-indexed #(* (exp (+ (type (types-rx-curry %1)) pi)) %2) base-m-br)])

        m-br-rx-xf-1- (fn [type]
                        [type (map-indexed #(* (exp (+ (type (types-rx-curry %1)) pi)) %2) base-m-br-)])

        m-br-rx-10-state1 (map-indexed #(* (exp (+ (:hrctb (types-rx-curry %1)) pi)) %2) base-m-br-delayed)
        ;_ (psym "m-br-rx-10-state1" m-br-rx-10-state1)

        ; I don't think we need to map over all types.
        ; Rather, we should be calculating only with the type selected
        s-cum-br-rx (into {}
                      (comp
                        (map m-br-rx-xf-1)                  ; -> m-br-x       R 251
                            ;(map echo)
                            (map cell-sums)                 ; -> m-cum-br-rx  R 178
                            (map (cell-apply #(->> % (-) (exp))))) ; -> s-cum-br-rx R 181
                          types)
        ;_ (psym "s-cum-br-rx " (:hrctb s-cum-br-rx))        ; CHECKS  finally!!!

        ; I don't think we need to map over all types.
        ; Rather, we should be calculating only with the type selected
        s-cum-br-rx- (into {}
                           (comp
                             (map m-br-rx-xf-1-)            ; -> m-br-x       R 251
                             ;(map echo)
                        (map cell-sums)                     ; -> m-cum-br-rx  R 178
                        (map (cell-apply #(->> % (-) (exp))))) ; -> s-cum-br-rx R 181
                      types)

        s-cum-br-rx-10 (map #(exp (- %)) (drop 1 (reductions + 0 m-br-rx-10-state1)))
        ;_ (psym "s-cum-br-rx-10 " s-cum-br-rx-10)           ; CHECKS  finally!!!
        ;_ (psym "s-cum-br-rx- " (:hrctb s-cum-br-rx-))      ; $$$$$


        m-br-rx (into {}
                  (comp
                    (map (cell-apply #(- 1 %)))             ; -> m-cum-br-rx  R 184
                    (map (cell-diffs 0)))                   ; -> m-br-rx      R 187
                  s-cum-br-rx)
        ;_ (psym "m-br-rx" (:hrctb m-br-rx))

        m-br-rx-10 (->> s-cum-br-rx-10
                        (map #(- 1 %))
                        (cons 0)
                        (partition 2 1)
                        (map (fn [[a b]] (- b a)))
                        )
        ;_ (psym "m-br-rx-10" m-br-rx-10)

        m-br-rx- (into {}
                       (comp
                         (map (cell-apply #(- 1 %)))        ; -> m-cum-br-rx  R 184
                         (map (cell-diffs 0)))              ; -> m-br-rx      R 187
                       s-cum-br-rx-)
        ;_ (psym "m-br-rx-" (:hrctb m-br-rx-))               ; R 424 $$$$$


        ; Cumulative all cause mortality conditional on surviving breast and all cause mortality
        ; R 194
        m-all-rx (into {}
                   (comp
                     (map (cell-binary #(- 1 (* %1 %2)) s-cum-br-rx))
                     (map (cell-diffs 0)))
                   s-cum-oth-rx)
        ;_ (psym "m-all-rx" (:hrctb m-all-rx))

        m-all-rx- (into {}
                        (comp
                          (map (cell-binary #(- 1 (* %1 %2)) s-cum-br-rx-))
                          (map (cell-diffs 0)))
                        s-cum-oth-rx-)
        ;_ (psym "m-all-rx-" (:hrctb m-all-rx-))


        m-cum-all-rx-10 (map #(- 1 (* %1 %2)) s-cum-oth-10 s-cum-br-rx-10)
        ;_ (psym "m-cum-all-rx-10" m-cum-all-rx-10)

        m-all-rx-10 (->> m-cum-all-rx-10
                         (cons 0)
                         (partition 2 1)
                         (map (fn [[a b]] (- b a)))
                         )
        ;_ (psym "m-all-rx-10" m-all-rx-10)                  ;CHECKS R 421 $$$$$

        prop-br-rx- (map #(/ %1 (+ %1 %2)) m-br-rx- m-oth-)
        ;_ (psym "m-br-rx-" m-br-rx-)              ; CHECKS R 424
        ;_ (psym "m-oth-" m-oth-)              ; CHECKS R 424
        ;_ (psym "prop-br-rx-" prop-br-rx-)              ; CHECKS R 424

        prop-br-rx-10 (map #(/ %1 (+ %1 %2)) m-br-rx-10 m-oth-10)
        ;_ (psym "prop-br-rx-10" prop-br-rx-10)              ; CHECKS R 424

        pred-m-br-rx-10 (map * prop-br-rx-10 m-all-rx-10)
        ;_ (psym "pred-m-br-rx-10" pred-m-br-rx-10)          ; CHECKS R 425

        pred-cum-br-rx-10 (reductions + pred-m-br-rx-10)
        ;_ (psym "pred-cum-br-rx-10" pred-cum-br-rx-10)      ; CHECKS R 426

        pred-m-oth-rx-10 (map - m-all-rx-10 pred-m-br-rx-10)
        ;_ (psym "pred-m-oth-rx-10" pred-m-oth-rx-10)        ; CHECKS R 427

        pred-cum-oth-rx-10 (reductions + pred-m-oth-rx-10)
        ;_ (psym "pred-cum-oth-rx-10" pred-cum-oth-rx-10)    ; CHECKS R 428

        pred-cum-all-rx-10 (map + pred-cum-br-rx-10 pred-cum-oth-rx-10)
        ;_ (psym "pred-cum-all-rx-10" pred-cum-all-rx-10)


        ;---------
        ; Proportion of all cause mortality that is breast cancer

        pred-m-br- (into {}
                         (comp
                           (map (cell-update (fn [type tm old]
                                               (if ((if d? >= >) tm 0)
                                                 (/ old (+ old (nth m-oth- tm)))
                                                 0))))
                           (map (cell-binary * m-all-rx-))
                           )
                         m-br-rx-)
        ;_ (psym "pred-m-br-rx-" (:hrctb pred-m-br-))

        pred-m-br-rx (into {}
                       (comp
                         (map (cell-update (fn [type tm old] (if (> tm 0) (/ old (+ old (nth m-oth tm))) 0))))
                         (map (cell-binary * m-all-rx))
                         )
                       m-br-rx)
        ;_ (psym "pred-m-br-rx" (:hrctb pred-m-br-rx))

        ; $$$$$

        pred-cum-br-rx (into {}
                         (map cell-sums)
                         pred-m-br-rx)
        ;_ (psym "pred-cum-br-rx" (:hrctb pred-cum-br-rx))


        pred-cum-br-rx- (into {}
                              (map cell-sums)
                              pred-m-br-)
        ;_ (psym "pred-cum-br-rx-" (:hrctb pred-cum-br-rx-))

        pred-cum-all-rx- (into {}
                               (comp
                                 (map (cell-binary #(- %2 %1) pred-m-br-)) ;pred-m-oth-rx R 203
                                 (map cell-sums)            ; pred-cum-oth-rx R204
                                 (map (cell-binary + pred-cum-br-rx-))
                                 )                          ; pred-cum-all-rx R 205
                               m-all-rx-)
        ;_ (psym "pred-cum-all-rx-" (:hrctb pred-cum-all-rx-))

        pred-cum-all-rx (into {}
                          (comp
                            (map (cell-binary #(- %2 %1) pred-m-br-rx)) ;pred-m-oth-rx R 203
                            (map cell-sums)                 ; pred-cum-oth-rx R204
                            (map (cell-binary + pred-cum-br-rx))
                            )                               ; pred-cum-all-rx R 205
                          m-all-rx)
        ;_ (psym "pred-cum-all-rx" (:hrctb pred-cum-all-rx))


        ; Return value is implemented for generalised calculation (the one ending in '-')
        surg-only- (map #(- 1 %) (:z pred-cum-all-rx-))
        ;_ (psym "surg-only" surg-only)

        benefits2-2 (assoc (into {}
                                 (map (cell-binary-seq - (:z pred-cum-all-rx-)))
                                 pred-cum-all-rx-)
                      :z surg-only-
                      :oth (:z s-cum-oth-rx-))
        ;_ (psym "benefits2-2" benefits2-2)

        result (map-of-vs->v-of-maps benefits2-2)
        ;_ (psym "result" (count result))

        survived (if (zero? delay)
                   nil
                   (repeat (inc delay) (assoc
                                         (zipmap (keys benefits2-2) (repeat 0))
                                   :z 1
                                         :oth 1)))]

    (concat survived result)
    ))

(comment
  (defn h
    ([k t]
     (k (nth
          (cljs-predict {:post-meno :post, :chemo 3, :bis 1, :age 57, :radio nil, :bis? true, :tra 1, :ki67 1, :chemoGen 3, :size 20, :radio? false, :nodes 10, :grade 2, :erstat 1, :rtime 15, :her2 1, :delay 0, :detection 1, :horm :h5})
          t)))
    ([]
     (h :h 15))
    )

  (defn hh
    ([k t]
     (println "hh")
     (k (nth
          (cljs-predict {:post-meno :post, :chemo 3, :bis 1, :age 57, :radio nil, :bis? true, :tra 1, :ki67 1, :chemoGen 3, :size 20, :radio? false, :nodes 10, :grade 2, :erstat 1, :rtime 15, :her2 1, :delay 0, :detection 1, :horm :h10})
          t)))
    ([]
     (hh :h 15))
    )

  (defn hd
    ([k t]
     (k (nth
          (cljs-predict {:post-meno :post, :chemo 3, :bis 1, :age 57, :radio nil, :bis? true, :tra 1, :ki67 1, :chemoGen 3, :size 20, :radio? false, :nodes 10, :grade 2, :erstat 1, :rtime 15, :her2 1, :delay 5, :detection 1, :horm :h5})
          t)))
    ([]
     (hd :hctb 15))
    )

  (defn hhd
    ([k t]
     (k (nth
          (cljs-predict {:post-meno :post, :chemo 3, :bis 1, :age 57, :radio nil, :bis? true, :tra 1, :ki67 1, :chemoGen 3, :size 20, :radio? false, :nodes 10, :grade 2, :erstat 1, :rtime 15, :her2 1, :delay 5, :detection 1, :horm :h10})
          t)))
    ([]
     (hhd :hctb 15))
    ))


(comment

  ; Current example sent to Paul
  (last (cljs-predict {:post-meno :post, :chemo 3, :bis 1, :age 57, :radio nil, :bis? true, :tra 1, :her2-status :yes, :ki67 1, :mode :symptomatic, :ki67-status :positive, :chemoGen 3, :size 29, :radio? false, :nodes 5, :grade 1, :erstat 1, :er-status :yes, :show-uncertainty :yes, :micromets :disabled, :rtime 15, :her2 1, :dcis :no, :delay 0, :detection 0, :horm :h10})))

