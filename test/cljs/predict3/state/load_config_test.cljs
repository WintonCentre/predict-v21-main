(ns predict3.state.load-config-test
  (:require [clojure.test :refer [deftest is testing]]
            [predict3.state.load-config :refer [rbg-label live-keys-by-model]]
            ))

(deftest rbg-label-test
  (testing "rbg-label-test. "
    (is (= "some-label radio button group" (rbg-label "some-label")))
    )
  )



(deftest live-keys-by-model-test
  (testing "live-keys-by-model-test. Currently model number is fixed variable and only v2.1 is used. Possible different models is needed for future."
    (is (= #{:post-meno :horm-delay :enable-radio :chemo :bis :age :result-year
             :enable-bis
             :radio
             :tra
             :her2-status
             :mode
             :ki67-status
             :size
             :nodes
             :grade
             :surgery-assumed
             :enable-dfs
             :default-tab
             :er-status
             :show-uncertainty :micromets :dcis :delay :enable-h10 :horm}
           (live-keys-by-model "v2.1")))
    )
  )