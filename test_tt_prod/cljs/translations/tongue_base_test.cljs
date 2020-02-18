(ns translations.tongue-base-test
  (:require [clojure.test :refer-macros [deftest testing is async]]
            [cljs.reader :as edn]
            [tongue.core :as tongue]
            [translations.tongue-base :refer [wrap-translator
                                              file-error    ;handle-languages
                                              get-dictionary handle-dictionary
                                              process-dict-op process-dict-ops]])
  )


; This static dictionary will be merged by reading an edn dict from the server,
; but it should be sufficient to start the app off.
(def translations-mock
  {:en {:missing              "**MISSING**"           ; Fallback for missing resources
        :hello-world          "Hello, world!"
        :404/oops             "Oops!"
        :404/text             "Try clicking on 'Home' in the navigation bar instead."
        :unknown              "Unknown"

        :home/what-is-predict "What is Predict?"
        :nest2                {:lev1 {:lev2 200}}
        :nest3                {:lev1 {:lev2 {:lev3 300}}}
        #_#_:imports {:__load-resource                      ; Inline edn content loaded from disk/resource
                      "resources/en.clj"}}

   ; German language resources
   :de {:missing              "**FEHLT**"
        :hello-world          "Hallo Welt!"

        :404/oops             "Hoppla!"
        :404/text             "Klicken Sie stattdessen auf 'Startseite' in der Navigationsleiste."
        :unknown              "Unbekannte"

        :home/what-is-predict "Was ist Predict?"
        #_#_:imports {:__load-resource                      ; Inline edn content loaded from disk/resource
                      "resources/de.clj"}
        }

   ; Chinese language resources
   :zh {:missing     " **失Ê踪** "
        :hello-world " 世界，你好 "}})

(def translator-mock (tongue/build-translate translations-mock))

(def ^:dynamic *en* (wrap-translator :en translator-mock))           ; dynamic so we can rebind it in tests if necessary
(def ^:dynamic *de* (wrap-translator :de translator-mock))           ; dynamic so we can rebind it in tests if necessary


(deftest simple-english-lookup

  (testing "tt can lookup keys in a the default English locale"
    (is (= "Hello, world!" (*en* :hello-world)))
    ))

(deftest simple-german-lookup
  (testing "redefed tt can lookup keys in German"
    (is (= "Hallo Welt!" (*de* :hello-world)))
    ))

(deftest wrap-translator-test
  (testing "wrap-translator adds stuff to tongue tt"
    (is (= "Unbekannte" (*de* :unknown)))
    (is (= "Unknown" (*en* :unknown)))
    (is (= "welsh" (*en* "welsh")))
    (is (= "dutch" (*de* "dutch")))                     ;=> "dutch"
    (is (= "Unbekannte" (*de* [:unknown "Text for :unknown entry"]))) ;=> "Unknown"
    (is (= [:goo "unknown"] (*en* [:goo "unknown"])))
    (is (= [:goo ""] (*de* [:goo ""])))                ; Should this be in German?
    ))

(def dictionary (atom {:lang :en}))

#_(deftest get-languages-test
    (testing "get languages list"
      (reset! dictionary nil)                               ; to isolate this test
      (async done
        (js/setTimeout (fn []
                         (is false "get-languages-test: Start figwheel server at localhost:5449 to test ajax calls")
                         (done))
                       500)
        (get-languages
          "resources/public/languages..txt"
          {:error-handler (fn [m]
                            (is (nil? m))
                            (file-error m)
                            (done))
           :handler       (fn [edn-rsp]
                            (handle-languages dictionary edn-rsp)
                            (is (= "#{:en :de}" edn-rsp))
                            (is (= #{:en :de} (:languages @dictionary)))
                            (done))}))))

;
; Stateful tests usingg the dictionary atom
;
(deftest process-upsert-test
  (let [new-dict (process-dict-op
                   @dictionary
                   [:upsert :en {:new-word "another word"}]
                   )]
    (is (= {:new-word "another word"}
           (get-in new-dict [:translations :en])))
    (is (= "another word" ((:translator new-dict) :new-word)))))

(comment (process-upsert-test))

(deftest multiple-upserts-test
  (let [new-dict (process-dict-ops
                   @dictionary
                   [[:upsert :en {:a-word "a word"}]
                    [:upsert :en {:another-word "another word"}]])]
    (is (= {:a-word       "a word"
            :another-word "another word"}
           (get-in new-dict [:translations :en])))
    (is (= "another word" ((:translator new-dict) :another-word)))))

(deftest multilingual-upserts-test
  (let [new-dict (process-dict-ops
                   @dictionary
                   [[:upsert :en {:a-word "a word"}]
                    [:upsert :de {:please "bitte"}]
                    [:switch :en]])]
    (is (= {:a-word "a word"}
           (get-in new-dict [:translations :en])))
    (is (= {:please "bitte"}
           (get-in new-dict [:translations :de])))
    (is (= "a word" ((:translator new-dict) :a-word)))))

(deftest multilingual-upserts-and-switch-test
  (let [new-dict (process-dict-ops
                   @dictionary
                   [[:upsert :en {:a-word "a word"}]
                    [:upsert :de {:please "bitte"
                                  :a-word "ein Wort"}]
                    [:switch :de]
                    ])]
    (is (= {:a-word "a word"}
           (get-in new-dict [:translations :en])))
    (is (= {:please "bitte"
            :a-word "ein Wort"}
           (get-in new-dict [:translations :de])))
    (is (= :de (:lang new-dict)))
    (is (= "ein Wort" ((:translator new-dict) :a-word))))
  )

(def dict (atom {:languages #{:en :de}}))

(deftest handle-dictionary-test
  (testing "handle-dictionary"
    (let [translations (handle-dictionary dict "[[:upsert :en {;:lang                 :en\n               :missing              \"**MISSING** %1 %2\"    ; Fallback for missing resources\n               :test/fixture         \"English test\"\n\n               :hello-world          \"Hello, world!\"\n               :hello-tempura        \"Hello tempura!\"\n\n               :404/oops             \"Oops!\"\n               :404/text             \"Try clicking on 'Home' in the navigation bar instead.\"\n               :unknown              \"Unknown\"\n\n               :home/what-is-predict \"What is Predict?\"\n               }]\n [:upsert :de {                                             ;:lang                 :de\n               :missing              \"**FEHLT**\"\n               :test/fixture         \"Deutschtest\"\n\n               :hello-world          \"Hallo Welt!\"\n\n               :404/oops             \"Hoppla!\"\n               :404/text             \"Klicken Sie stattdessen auf 'Startseite' in der Navigationsleiste.\"\n               :unknown              \"Unbekannte\"\n\n               :home/what-is-predict \"Was ist Predict?\"\n\n               }]\n [:switch :de]]")]

      (is "English test" (get-in translations [:translations :en :test/fixture])))))

(comment
  (handle-dictionary-test))

(deftest get-dictionary-test
    (testing "get dictionary"
      (with-redefs [dict (atom {:languages #{:en :de}})]
                   (async done
                     (js/setTimeout (fn []
                                      (is true "get-dictionary: Start figwheel server at localhost:5449 to test ajax calls")
                                      (done))
                                    1000)

                     #_(get-languages
                         "resources/public/dictionary.txt"
                         {:error-handler (fn [m]
                                           (is (nil? m))
                                           (file-error m)
                                           (done))
                          :handler       (fn [edn-rsp]
                                           (handle-languages dictionary edn-rsp)
                                           (is (= "#{:en :de}" edn-rsp))
                                           (is (= #{:en :de} (:languages @dictionary)))
                                           (done))})
                     (get-dictionary
                       "resources/public/dictionary_mock.txt"
                       {:error-handler (fn [m]
                                         (is (nil? m))
                                         (file-error m)
                                         (done))
                        :handler       (fn [edn-rsp]
                                         (handle-dictionary dict edn-rsp)
                                         ;(println "dict=" dict)
                                         (is (= #{:en :de} (into #{} (keys (:translations @dict)))))
                                         (is (= :de (:lang @dict)))
                                         (is (= "Deutschtest" ((:translator @dict) :test/fixture)))
                                         (done))})))))

#_(deftest test-async
  (async done
    (http/get "localhost:5449/languages..txt"               ; "resources/public/dictionary.txt"
              (fn [res]
                (println "res=" res)
                (is (string? res))
                (done)))))

#_(deftest get-languages-test
    (testing "get languages list"
      (reset! dictionary nil)                               ; to isolate this test
      (async done
        (js/setTimeout (fn []
                         (is false "get-languages-test: Start figwheel server at localhost:5449 to test ajax calls")
                         (done))
                       500)
        (get-languages
          "resources/public/languages.txt"
          {:error-handler (fn [m]
                            (is (nil? m))
                            (file-error m)
                            (done))
           :handler       (fn [edn-rsp]
                            (handle-languages dictionary edn-rsp)
                            (is (= "#{:en :de}" edn-rsp))
                            (is (= #{:en :de} (:languages @dictionary)))
                            (done))}))))


;
; later
;
#_(deftest get-translations-for-test
    (testing "get languages list"
      (reset! dictionary nil)                               ; to isolate this test
      (async done
        (js/setTimeout (fn []
                         (is false "get-translations-for-test: Start figwheel server at localhost:5449 to test ajax calls")
                         (done))
                       500)
        (get-translations-for
          "resources/public/dictionary.txt"
          {:error-handler (fn [m]
                            (is (nil? m))
                            (file-error m)
                            (done))
           :handler       (fn [edn-rsp]
                            (handle-languages dictionary edn-rsp)
                            (is (= "#{:en :de}" edn-rsp))
                            (is (= #{:en :de} (:languages @dictionary)))
                            (done))}))))



(comment
  (simple-english-lookup)
  )

