(ns predict3.state.local-storage
  (:require [cljs.reader :refer [read-string]]))

(defn put-local
  "put a value in a localstorage key"
  [k v]
  (.setItem js/localStorage (name k) v))

(defn get-local
  "get a localstorage value at key k"
  [k]
  (.getItem js/localStorage (name k)))

(defn get-settings!
  [default-value]
  (when (exists? js/window)                                 ; for node tests
    (if-let [settings (.getItem js/localStorage "predict-2.1-settings")]
      (merge default-value (read-string settings))
      default-value)))

(comment
  (put-local "foo" "bar")
  (get-local "foo")
  (get-settings! {:enable-bis :no})
  ; =>
  {:enable-bis   :yes,
   :enable-radio :no,
   ;:ten-fifteen  15,
   :default-tab  :table,
   :enable-h10   :no,
   :enable-dfs   :no})


(defn put-settings!
  [settings]
  (when (exists? js/window)                                 ; for node tests
    (let [old-settings (get-settings! {})]
      (.setItem js/localStorage "predict-2.1-settings" (merge old-settings settings)))))

(defn clr-settings!
  []
  (when (exists? js/window)                                 ; for node tests
    (.setItem js/localStorage "predict-2.1-settings" {})))
