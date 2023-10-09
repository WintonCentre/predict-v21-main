# PREDICT
If you are wanting to use the code to validate the PREDICT algorithm in a new population, please contact us at predict@statslab.cam.ac.uk and we'd be delighted to help. The new version, v3, is about to be released and it would be better to use that version.

### Installing js dependencies
We are currently importing jQuery to support Bootstrap modals. These are
pulled in by script elements, and their externs are resolved by
cljsjs/jquery and cljsjs/bootstrap. So no extra installation is necessary.

### Installing cljs dependencies
wc-rum-lib is not on Maven, so needs to be installed separately.
```
git clone https://github.com/WintonCentre/wc-rum-lib.git
cd wc-rum-lib
lein install
```

Similarly pubsub:
```
git clone https://github.com/WintonCentre/pubsub.git
cd pubsub
lein install
```

Then make sure intellij has refreshed the leiningen project.

### Creating a production build.
Run the ./build.sh script

### Clojure Test:
Use a leiningen run configuration with both +clj and +cljtest profile

### Clojurescript with figwheel
Use a REPL run configuration, selecting clojure.main in normal JVM process, and adding
`script/repl.clj` as a parameter. A server should start at http://0.0.0.0:5449.

### Tests

We're using a script `bin/kaocha` to start `kaocha` tests. These are configured in `tests.edn`. So, to run cljs test suite, say:
```
./bin/kaocha :unit-cljs
```
To run end to end tests - sometimes known as integration tests:
```
./bin/kaocha :integration
```
Integration tests are fragile because they have to extract values from the
views displayed by predict for use in the tests. This means that changes to view
layout or view markup can easily break the test. For example, adding text can cause a value to scroll off screen. When this happens, you'll need to 
revisit the function that reads those values from the screen - e2e.execs/get-results!.

Also, in order to compare against R runs, we need to run the R code inside a webserver.
Set up the R server by cloning https://github.com/WintonCentre/predict-opencpu-server.gitand running it inside RStudio. This application in turn pulls in the R package installed from the github repo https://WintonCentre/predict-v22-r

Configure [kaocha-cljs](https://github.com/lambdaisland/kaocha-cljs) in tests.edn

The R server runs on a local server at localhost:5656. You can test it manually by
running a POST to http://localhost:5656/ocpu/library/Predictv2.2/R/benefits22/json or
http://localhost:5656/ocpu/library/Predictv2.2/R/benefits2210/json with a tool such as `curl` or `Postman`.
### Clojurescript production build
```
lein cljsbuild prod once
```
If you get a warning like this on build:
```
WARNING: resources/public/js/compiled/min/inferred_externs.js:33: WARNING - name goog is not defined in the externs.
goog.isArrayLike;
```
then it's benign. (goog does not need to be declared extern)
