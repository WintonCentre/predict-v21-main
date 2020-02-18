(ns predict3.input-preset
  "Saves and restores whatever values have been entered as inputs. Useful after a reload to restore input state quickly."
  (:require [cljs.reader :refer [read-string]]
            [predict3.state.run-time :refer [rtdb input-cursor results-cursor get-inputs]]
            [predict3.models.adapters.predict2 :refer [recalculate]]
            [predict3.state.local-storage :refer [get-local put-local]]))

(defn save-inputs []
  (put-local :saved-input-map (pr-str (get-inputs)))
  nil)ˆ
#_(save-inputs)

(defn restore-inputs []
  (doseq [[k v] (read-string (get-local :saved-input-map))]
    (reset! (input-cursor k) v))
  (reset! results-cursor (recalculate (get-inputs))))

#_(restore-inputs)
