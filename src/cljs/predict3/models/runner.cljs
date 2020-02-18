(ns predict3.models.runner
  (:require [predict3.state.run-time :refer [results-change results-cursor get-inputs
                                             on-screen-inputs-cursor rtdb
                                             input-cursor input-cursors]]
            [clojure.set :refer [union]]
            [clojure.string :refer [index-of]]
            [pubsub.feeds :refer [publish]]
            [predict3.models.adapters.predict2 :refer [recalculate]]
            ))

(defn error? [v]
  (or (nil? v) (= v "") (and (string? v) (some? (index-of v ":")))))

(defn error-by-key? [k]
  (error? @(input-cursor k)))


(defn recalculate-model?
  "return true if the model can be calculated, else nil.
  im is the result of calling input-map"
  [input-map]
  (and (seq @on-screen-inputs-cursor)
       (every? (fn [k] (and (get input-map k) (not (error-by-key? k)))) @on-screen-inputs-cursor)

       ;removing following line to fix PB-24
       ;(> (count @on-screen-inputs-cursor) 1)
       )
  )

;;
;; Define a multimethod that recalculate-models predictions based on a selected model
;;
(defmulti recalculate-model "recalculates the selected model" (fn [model inputs] model))


(defmethod recalculate-model "v2.1"
  [model input-map]
  ;(println "model " model "\n")
  ;(println "input-map" input-map "\n")

  (if (recalculate-model? input-map)
    (let [results (recalculate input-map)]
      (publish results-change results)
      results)
    (publish results-change [])))

(comment                                                    ;; --- tests

  (recalculate-model? (get-inputs))
  @results-cursor

  (recalculate-model "v1.2" {:a 1 :b 2})
  ; v1.2 {:a 1, :b 2}
  ; => nil

  (recalculate-model "v2.1" {:a 1 :b 2})
  ; v2.1 {:a 1, :b 2}
  ; => nil
  )
