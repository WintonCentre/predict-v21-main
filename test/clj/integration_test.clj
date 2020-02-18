(ns integration-test
  "integration test. Use :integration test-suite in bin/kaocha"
  (:require [clojure.test :refer :all]
            [etaoin.api :refer :all]
            [e2e.execs :refer [exec! setup-for-test! check-all-views get-all-results!]]
            [e2e.generators :as pgen])
  (:import (clojure.lang ExceptionInfo)))

(def server-url "http://localhost:5449")

(def ^:dynamic *driver*)

(def driver-types [:chrome])

(defn fixture-driver
  "Executes a test running a driver. Binds a driver
   to the global *driver* variable. Goes to the server url and waits 2 seconds"
  [f]
  (with-chrome {} driver
    (binding [*driver* driver]
      (doto *driver*
        (go server-url)

        ; Wait for website to load
        (wait 2)
        )
      (f))))

(defn fixture-drivers [f]
  (doseq [type driver-types]
    (with-driver type {} driver
      (binding [*driver* driver]
        (doto *driver*
          (go server-url)

          ; John waits 2 seconds for website to load
          (wait 2)
          )
        (testing (format "Testing in %s browser" (name type))
          (f))))))

(use-fixtures
  :each                                                     ;; start and stop driver for each test
  fixture-drivers)


(defn check-roundings
  []
  (let [inputs-and-treatments (pgen/inputs-and-treatments)]
    (exec! *driver* inputs-and-treatments)
    (let [check-5 (check-all-views (get-all-results! *driver* 5))
          check-10 (check-all-views (get-all-results! *driver* 10))
          check-15 (check-all-views (get-all-results! *driver* 15))
          ]
      (when (not= {} check-5 check-10 check-15)
        (str {:inputs inputs-and-treatments
              :5      check-5
              :10     check-10
              :15     check-15} "\n")
        ))))

(deftest ^:integration check-against-r
  "Generates random inputs to the PREDICT website, translate these into web UI operations, and also into R
  parameters. Call the R predict code via an OpenCPU server, and retrieve results. Execute the web operations
  and switch to curves view. Extract the web results from the curves view. Check that these match the R view."

  (testing ""))

(deftest ^:integration check-rounding-consistency
  "A pure version for use in deftest.

  Plan is
  0. open a log file
  1. setup-for-test
  2. for [n number-of-tests]
  3. fill-in-inputs-and-treatments
  4. (check-all-views (get-all-results!))
  5. if errors exist, print them along with the input that caused them.

  We do still have occasional exceptions caused by input mis-timings, so we
  simply catch these, print an 'E' and continue testing.

  Run this in the REPL. Choose n > 100 for reasonable parameter space coverage.
  "
  (testing "rounding consistency"

    (setup-for-test! *driver*)


    ; Let's run 3 tests here. Better to run e2e.execs/check-rounding-consistency! for a
    ; longer run with better parameter space coverage.
    (is (nil? (check-roundings)) "rounding check 1")
    (is (nil? (check-roundings)) "rounding check 2")
    (is (nil? (check-roundings)) "rounding check 3")
    ))


(comment
  ;; now declare your tests
  (deftest ^:integration user-sees-correct-homepage

    (is (= "Predict Breast" (get-title *driver*)))

    ; John sees Alison blue H1 title
    (is (string? (query *driver* {:tag :h1})))
    (is (= "What is Predict?" (get-element-text *driver* {:tag :h1})))
    (is (= "alison-blue-2" (get-element-attr *driver* {:tag :h1} :class)))

    ; John sees Start Predict button
    (is (= "Start Predict"
          (get-element-text *driver* ".//button[@aria-label='go to predict tool']")))

    ; John sees version number on bottom right of the screen
    ; maybe less fragile just to verify element exists?

    (is (= "Build: v0.0-dev-#000-hash"
          (get-element-text *driver* {:fn/has-class "build-version"})))
    )

  ;def test_user_can_see_gdpr_sticky_div(self):

  ;# John sees sticky GDPR bar on bottom of the page. See it's dark color.
  ;gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
  ;self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
  ;self.assertEqual('rgb(158, 158, 158)', gdpr_bar.value_of_css_property('color'))
  ;
  ;# John sees sticky GDPR bar has ok button
  ;gdpr_ok_input = self.browser.find_element_by_css_selector('input.btn-sm')
  ;self.assertEqual('Ok', gdpr_ok_input.get_attribute('value'))
  ;
  ;# # check sticky by scroll down and seeing if it's still there?
  ;# self.browser.execute_script("window.scrollTo(0, document.body.scrollHeight);")
  ;# time.sleep(2)

  #_(deftest ^:integration user-can-see-gdpr-sticky-div

      (is (query *driver* {:fn/has-class "gdpr-container"}))
      )


  ;# Temp work around for locally hosted dev test
  ;# John sees Start Predict button and clicks it.
  ;start_predict_button = self.browser.find_element_by_css_selector('button.btn-lg')
  ;start_predict_button.click()
  ;
  ;# John waits 2 seconds for tools page to load
  ;time.sleep(2)
  ;
  ;def tearDown(self):
  ;super().tearDown()
  ;
  ;def test_user_does_not_see_results_initially(self):
  ;#results
  ;    info_disabled_text_icon = self.browser.find_element_by_class_name('fa-info-circle')
  ;info_disabled_text = info_disabled_text_icon.find_element_by_xpath('..')
  ;self.assertIn('Treatment options and results will appear here when you have filled in all the information needed above.'
  ;, info_disabled_text.text)
  ;
  ;def test_user_add_sensible_input_and_see_results(self):
  ;# John add details into the page
  ;input_age = self.browser.find_element_by_id('age')
  ;input_age.clear()
  ;input_age.send_keys("52")
  ;
  ;input_post_menopausal = self.browser.find_element_by_xpath('//div[@aria-label="Post Menopausal? radio button group"]/button[2]')
  ;                                                            input_post_menopausal.click()
  ;
  ;                                                            input_er_status = self.browser.find_element_by_xpath('//div[@aria-label="ER status radio button group"]/button[1]')
  ;                                                                                                                  input_er_status.click()
  (deftest new-visitor-can-use-tools

    ; John clicks on the Start Button
    (click *driver* ".//button[@aria-label='go to predict tool']")
    (wait *driver* 1)

    (is (has-text? *driver* "Treatment options and results will appear here when you have filled in all the information needed above.")
      "Results do not show at first")

    ; Make separate calls here to give numeric input a chance to do its async stuff
    (fill-human *driver* {:tag :input :id "age"} "3")
    (fill-human *driver* {:tag :input :id "age"} "5")

    (is (= "35" (get-element-attr *driver* {:tag :input :id "age"} :value)))

    ))
