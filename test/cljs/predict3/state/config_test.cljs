(ns predict3.state.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [predict3.state.config :refer [get-input-default input-groups]]
            ))

(deftest get-input-default-test
  (testing "get-input-default-test. Returns default value for given key within pre-defined structure."
    (is (= :yes (get-input-default input-groups :show-uncertainty)))
    (is (= :yes (get-input-default input-groups :enable-bis)))
    (is (= :disabled (get-input-default input-groups :micromets)))
    (is (= "" (get-input-default input-groups :nodes)))

    (is (= nil (get-input-default input-groups :not-existing-key-case-here)))
    )
  )
