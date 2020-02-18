(defproject predict "2.2.1"
  :description "Predict cljs repo"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"


  :plugins [[lein-figwheel "0.5.18"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            ;[lein-doo "0.1.8"]
            ;[lein-resource "16.11.1"]
            ;[lein-codox "0.10.3"]
            ;[lein-ancient "0.6.15"]
            ]

  ;:source-paths ["src/cljs"]
  ;:test-paths ["test/cljs" "test/clj"]
  :dependencies [
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.516"]
                 [org.clojure/core.async "0.4.490"]

                 ;; cljs dependencies
                 [cljs-ajax "0.7.3"]
                 [cljs-css-modules "0.2.1"]
                 ;[sablono "0.7.4"]
                 ;; need to specify this for sablono
                 ;; when not using devcards
                 ;[cljsjs/react "15.4.2-2"]
                 ;[cljsjs/react-dom "15.4.2-2"]
                 [devcards "0.2.4"]
                 [figwheel-sidecar "0.5.18"]


                 [rum "0.11.3"]
                 [tongue "0.2.7"]
                 [pubsub "0.2.1"]
                 [wc-rum-lib "0.1.16"]
                 [cljs-css-modules "0.2.1"]
                 [cljsjs/jquery "1.9.1-0"]
                 [cljsjs/bootstrap "3.3.6-0"]

                 [com.taoensso/tempura "1.2.1"]
                 [winton-utils "0.2.1"]

                 ; routing
                 [funcool/bide "1.6.0"]
                 [funcool/promesa "3.0.0"]

                 [binaryage/devtools "0.9.10"]
                 [binaryage/oops "0.7.0"]


                 ;for etaoin
                 [etaoin "0.3.3"]

                 ; logging
                 [com.taoensso/timbre "4.10.0"]

                 ; to enable rebel-readline
                 ;[com.bhauman/rebel-readline "0.1.4"]
                 ]


  :aliases {"kaocha-prod" ["with-profile" "+kaocha-prod" "run" "-m" "kaocha.runner"]
            "kaocha-edit" ["with-profile" "+kaocha-edit" "run" "-m" "kaocha.runner"]
            "integration" ["with-profile" "+integration" "run" "-m" "kaocha.runner"]}


  :cljsbuild {:builds [
                       {:id           "min"
                        :source-paths ["src_tt_prod/cljs"]
                        ;:test-paths   ["test_tt_prod/cljs"]
                        :compiler     {:output-to       "resources/public/js/compiled/predict3.js"
                                       :output-dir      "resources/public/js/compiled/min"
                                       :main            "predict3.core"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       ;:pseudo-names true
                                       ;:parallel-build  true
                                       :externs         ["externs/bootstrap.js"
                                                         "externs/jquery.js"
                                                         "externs/mediatypechecker.js"
                                                         "externs/autotrack.js"]
                                       :pseudo-names    false
                                       :infer-externs   true
                                       :language-in     :ecmascript5
                                       :pretty-print    false}}

                       {:id           "min-edit"
                        :source-paths ["src_tt_edit/cljs"]
                        ;:test-paths   ["test_tt_edit/cljs"]
                        :compiler     {:output-to       "resources/public/js/compiled/predict3.js"
                                       :output-dir      "resources/public/js/compiled/min-edit"
                                       :main            "predict3.core"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       ;:pseudo-names true
                                       ;:parallel-build  true
                                       :externs         ["externs/bootstrap.js"
                                                         "externs/jquery.js"
                                                         "externs/mediatypechecker.js"
                                                         "externs/autotrack.js"]
                                       :pseudo-names    false
                                       :infer-externs   true
                                       :language-in     :ecmascript5
                                       :pretty-print    false}}



                       {:id           "dev"
                        :source-paths ["src/cljs"]
                        ;:test-paths   ["test/cljs"]
                        :figwheel     {:open-urls ["http://localhost:5449/index.html"]}
                        :compiler     {:main                 "predict3.core"
                                       :optimizations        :none
                                       :asset-path           "js/compiled/dev-out"
                                       :output-to            "resources/public/js/compiled/predict3.js"
                                       :output-dir           "resources/public/js/compiled/dev-out"
                                       :closure-defines      {goog.DEBUG true}

                                       :source-map-timestamp true
                                       :preloads             [devtools.preload]
                                       :aot-cache            false
                                       :external-config      {:devtools/config {:features-to-install    [:formatters :hints]
                                                                                :fn-symbol              "F"
                                                                                :print-config-overrides true}}}}

                       {:id           "dev-prod"
                        :source-paths ["src/cljs" "src_tt_prod/cljs"]
                        ;:test-paths   [_tt_prod/cljs"]
                        :figwheel     {:open-urls ["http://localhost:5449/index.html"]}
                        :compiler     {:main                 "predict3.core"
                                       :optimizations        :none
                                       :asset-path           "js/compiled/dev-prod"
                                       :output-to            "resources/public/js/compiled/predict3.js"
                                       :output-dir           "resources/public/js/compiled/dev-prod"
                                       :closure-defines      {goog.DEBUG true}
                                       :source-map-timestamp true
                                       :preloads             [devtools.preload]
                                       :aot-cache            false
                                       :external-config      {:devtools/config {:features-to-install    [:formatters :hints]
                                                                                :fn-symbol              "F"
                                                                                :print-config-overrides true}}}}

                       {:id           "dev-edit"
                        :source-paths ["src/cljs" "src_tt_edit/cljs"]
                        ;:test-paths   ["test/cljs" "test_tt_edit/cljs"]
                        :figwheel     {:open-urls ["http://localhost:5450/index.html"]}
                        :compiler     {:main                 "predict3.core"
                                       :optimizations        :none
                                       :asset-path           "js/compiled/dev-edit"
                                       :output-to            "resources/public/js/compiled/predict3.js"
                                       :output-dir           "resources/public/js/compiled/dev-edit"
                                       :closure-defines      {goog.DEBUG true}
                                       :source-map-timestamp true
                                       :preloads             [devtools.preload]
                                       :aot-cache            false
                                       :external-config      {:devtools/config {:features-to-install    [:formatters :hints]
                                                                                :fn-symbol              "F"
                                                                                :print-config-overrides true}}}}

                       {:id           "dev-default"
                        :source-paths ["src/cljs" "src_tt_prod/cljs"]
                        ;:test-paths   ["test/cljs" "src_tt_prod/cljs"]
                        :figwheel     {:open-urls ["http://localhost:5449/index.html"]}
                        :compiler     {:main                 "predict3.core"
                                       :asset-path           "js/compiled/dev-default"
                                       :output-to            "resources/public/js/compiled/predict3.js"
                                       :output-dir           "resources/public/js/compiled/dev-default"
                                       :closure-defines      {goog.DEBUG true}

                                       :source-map-timestamp true
                                       :preloads             [devtools.preload]
                                       :aot-cache            false
                                       :external-config      {:devtools/config {:features-to-install    [:formatters :hints]
                                                                                :fn-symbol              "F"
                                                                                :print-config-overrides true}}}}

                       ]
              }

  ;; from Babel transforms example in https://clojurescript.org/guides/javascript-modules
  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"]

  :figwheel {:css-dirs        ["resources/public/css"]
             ;:server-port     5449
             :readline        false
             :validate-config true}

  ;:repositories [["jitpack" "https://jitpack.io"]]


  :profiles {:kaocha-edit {:dependencies [[lambdaisland/kaocha "0.0-565"]
                                              [lambdaisland/kaocha-cljs "0.0-68"]]
                               :server-port  5450
                               :source-paths ["src/cljs" "src_tt_edit/cljs"]
                               :test-paths   ["test/cljs" "test_tt_edit/cljs"]}

             :kaocha-prod {:dependencies [[lambdaisland/kaocha "0.0-565"]
                                              [lambdaisland/kaocha-cljs "0.0-68"]]
                               :server-port  5449
                               :source-paths ["src/cljs" "src_tt_prod/cljs"]
                               :test-paths   ["test/cljs" "test_tt_prod/cljs"]}

             :integration {:dependencies [[lambdaisland/kaocha "0.0-565"]
                                          [lambdaisland/kaocha-cljs "0.0-68"]
                                          [org.clojure/test.check "0.10.0"]
                                          [etaoin "0.3.6"]
                                          [clj-http "3.10.0"]
                                          [clojure-csv/clojure-csv "2.0.1"]
                                          [semantic-csv "0.2.1-alpha1"]]
                           :server-port  5449
                           :source-paths ["test/clj"]
                           ;:source-paths ["src/cljs" "src_tt_prod/cljs"]
                           ;:test-paths   ["test/cljs" "test/clj" "test_tt_prod/cljs"]
                           }

             ;; Setting up nREPL for Figwheel and ClojureScript dev
             ;; Please see:
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             :dev         {
                           :source-paths ["src/cljs"]
                           :test-paths   ["test/cljs"]
                           }
             :dev-prod    {:dependencies [[binaryage/devtools "0.9.10"]
                                          [figwheel-sidecar "0.5.18"]]
                           :source-paths ["src_tt_prod/cljs"]
                           :test-paths    ["test_tt_prod/cljs"]
                           :figwheel      {:server-port 5449}}
             :dev-edit    {:dependencies [[binaryage/devtools "0.9.10"]
                                          [figwheel-sidecar "0.5.18"]]
                           :source-paths ["src_tt_edit/cljs"]
                           :test-paths   ["test_tt_edit/cljs"]
                           :figwheel     {:server-port 5450}
                           }
             :dev-default {:dependencies [[binaryage/devtools "0.9.10"]
                                          [figwheel-sidecar "0.5.18"]]
                           :source-paths ["src/cljs" "src_tt_prod/cljs"]
                           :test-paths   ["test/cljs" "test_tt_prod/cljs"]
                           :figwheel     {:server-port 5449}
                           }
             }

  ; Use to select tests to be included in the test run.
  ; but I don' think this is used by ./bin/kaocha. Only lein test
  :test-selectors {:default     (complement :integration)
                   :integration :integration}

  )