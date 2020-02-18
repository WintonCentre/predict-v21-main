(ns translations.tranny-api
  (:require [ajax.core :refer [GET POST]]
            [cljs.core.async :refer [chan <! >! put! take! alts!]]
            [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.edn :as edn]
            [goog.object :as gobj])
  (:require-macros [cljs.core.async :refer [go]]))

; For remote editing
;(def server-url "http://localhost:1337/")
(def server-url "https://api.spa.breast.wintoncentre.uk/")                     ;but this!

; For local testing
(def base-url (str server-url "api/"))

(defn get-handler [response]
  ;(println response)
  (let [clj-rsp (js->clj response :keywordize-keys true)
        hit-count (count clj-rsp)
        result (condp = hit-count
                 0 (println "No results")
                 1 (get (first clj-rsp) "text")
                 (map #(get % "text") clj-rsp))]
    ;(println result)
    result))

(def ta (atom nil))

(defn post-handler [response]
  (println "successful response" response)
  ;(println (get (js->clj response :keywordize-keys true) "body"))
  )


(defn error-handler [{:keys [status status-text response]}]
  (.log js/console (str "something bad happened: " status " " status-text))
  (when response (.log js/console (get response "message"))))


(defn lookup-key
  "lookup key in tranny server. 3-arity variant for testing."
  ([key]
   (lookup-key (str base-url "key/" key) {:handler get-handler :error-handler error-handler}))
  ([url {:keys [handler error-handler]}]
   (GET url {:handler handler :error-handler error-handler :mode "no-cors"})))


(defn lookup-key-lang
  "lookup language and key in tranny server"
  [key lang]
  (GET (str base-url "key/" (name key) "/lang/" (name lang))
       {:handler       get-handler
        :error-handler error-handler}))

(defn wordkey
  "generates a string representation of a namespaced keyword.
  :foo/bar -> \":foo/bar\"
  :bar -> \"bar\"
  The inverse function to 'keyword'."
  [key]
  (let [ns-key (namespace key)]
    (str (if ns-key (str ns-key "/") nil) (name key))))

(defn upload-translation
  "Update a key for a language with new text"
  ([key lang text]
   (let [url (str base-url "translation")]
     (upload-translation url key lang text)))
  ([url key lang text]
   (POST url {:handler post-handler :error-handler error-handler :params {:key (wordkey key) :lang (str lang) :text (str text)} :format :json})))

(defn end-point
  "Convert a relative endpoint url into an absolute address"
  [rel-url]
  (str base-url rel-url))

(defn delete-translation
  "Update a key for a language with new text"
  ([key lang]
   (let [url (end-point "delete")]
     (delete-translation url key lang)))
  ([url key lang]
   (POST url {:handler post-handler :error-handler error-handler :params {:key (wordkey key) :lang (str lang)} :format :json})))

(defn json-kvs->m
  "Convert a seq of json key-value pairs to a clojure map, replacing # with / to regain namespace.
  This assumes the json generator replaced / namespace separators with \\#.
  It's necessary to substitute '/' in namespaced keywords as data.json lib throws away namespaces - which we are using
  to help identify the page where the translation appears."
  [kvs]
  (->> kvs
       (map (fn [[s v]] [(keyword (str/replace s #"#" "/")) v]))
       (into {})))

(defn parse-downloaded-op
  "Validate a download containing an upsert or switch operation.
  If the op is a valid upsert, return the :upsert with decoded params
  If the op is a valid switch, return the :switch with decoded params"
  [download]
  (when (vector? download)
    (let [[op lang translations :as d] download]
      (when (= 2 (count lang))
        (condp = op
          "upsert" [:upsert (keyword lang) (json-kvs->m translations)]
          "switch" [:switch (keyword lang)]
          "error" (let [[e & msgs] d] [:error msgs])
          nil)
        ))))

(defn parse-error
  "Parse a server error into something tidier. Expects a map with :status, :status-text and [:response :message]"
  [er-m]
  (let [msg (get-in er-m [:response :message])]
    (-> er-m
        (dissoc :response)
        (dissoc :failure)
        (assoc :message msg)))
  )

(defn download-lang-translations
  "Given bufferless ok-chan and err-chan, will POST a download request to endpoint api/upserts in 3 arity version or to
  given url in 4 arity version. Downloads just the dictionary for the given language.
  The go block waits for a response on either channel, and returns a channel containing the parsed response.

  Intended for a single language. NOT IN USE YET."
  ([ok-chan err-chan lang url]
   (POST url {:handler #(put! ok-chan %) :error-handler #(put! err-chan %) :params {:lang lang} :format :json})
   (go
     (let [[response port] (alts! [ok-chan err-chan])]
       (if (= port ok-chan)
         (parse-downloaded-op (get response "body"))
         (parse-error (keywordize-keys response))))))
  ([ok-chan err-chan lang]
   (download-lang-translations ok-chan err-chan lang (end-point "upserts"))))


(defn download-all-translations
  "Given bufferless ok-chan and err-chan, will POST a download-all-translations request to endpoint upserts-all in
  2-arity or given url in 3 arity version.
  The go block waits for a response on either chan, and returns a chan containing the parsed response."
  ([ok-chan err-chan url]
   (POST url {:handler #(put! ok-chan %) :error-handler #(put! err-chan %) :format :json})
   (go
     (let [[response port] (alts! [ok-chan err-chan])]
       (if (= port ok-chan)
         (when (vector? response)
           (mapv parse-downloaded-op response))
         (parse-error (keywordize-keys response))))))
  ([ok-chan err-chan]
   (download-all-translations ok-chan err-chan (end-point "upserts/all"))))


(comment

  (def success-chan (chan 0))
  (def error-chan (chan 0))

  ; Start the tranny server before testing these.
  (go
    (println "get single languages" (<! (download-lang-translations success-chan error-chan "en"))))

  (go
    (println "get all languages!" (<! (download-all-translations success-chan error-chan))))

  (upload-translation :home/what-is-predict "de" "Was istttt Predict?")
  (upload-translation :foo "de" "Foo")
  (delete-translation :foo "de")

  (lookup-key "foo")
  (lookup-key "hello")
  (lookup-key-lang "hello" "fr")
  (update-key-lang "hello" "de" "Gooey")
  (update-key-lang "hello" "de" "Hallo")
  (update-key-lang "hello" "ar" "مرحبا")
  (update-key-lang :home/the-answer "en" "42")
  (update-key-lang :home/what-is-predict "de" "Was istttt Predict?")

  (str (namespace :home/what-is-predict) "/" (name :home/what-is-predict))
  ;=> "home/what-is-predict"

  (keyword (str (namespace :home/what-is-predict) "/" (name :home/what-is-predict)))
  ;=> :home/what-is-predict

  (wordkey :foo)
  ;=> "foo"

  (wordkey :foo/bar)
  ;=> "foo/bar

  (keyword "foo")
  ;=> :foo

  (keyword "foo/bar")
  :foo/bar

  )


; coast should execute:
;(coast/q '[:select * :from translation :where [:key ?key] [:lang ?lang]] {:key "hello" :lang "fr"})


