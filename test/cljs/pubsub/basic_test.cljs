(ns pubsub.basic-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [pubsub.feeds :refer [create-feed ->Topic subscribe publish unsubscribe]]
            ))

(enable-console-print!)

(deftest test-numbers
  (let [foo "bar"]
    (is (= foo "bar"))))

(deftest subscribe-and-publish
  (async done
    (let [feed (create-feed)
          warn-text "This is a warning"
          warning (->Topic :warning feed)
          handler (fn [topic message]
                    (is (= topic :warning))
                    (is (= message warn-text))
                    (done))]
      (subscribe warning handler)
      (publish warning warn-text)
      )))
