(ns translations.tongue-base
  "Common translation support for both production and edit systems"
  (:require [cljs.core.async :refer [chan <! >! put! take! alts!]]
            [tongue.core :as tongue]
            [ajax.core :refer [GET POST]]
            [cljs.reader :as edn]
            [clojure.string :refer [starts-with?]]
            [translations.tranny-api :refer [download-all-translations server-url]]
            [predict3.state.local-storage :refer [get-settings!]]
    ;[predict3.state.run-time :refer [t-state-cursor]] - we get a loop if we include this
            [clojure.string :refer [split]])
  (:require-macros [cljs.core.async :refer [go]]))



(defn wrap-translator
  "Return a translation function which wraps tongue 'translate', giving it similar
  capabilities as the tempura translator.

  The returned function takes a language, a k to translate, plus any args

  If k is a string it returns k untranslated.

  If k is a keyword, it looks up  k in the translations dictionary for the
  given language, possibly with argument interpolation. i.e. Just like the original.

  If k is a vector, the first element is used as the lookup key. The remaining elements are ignored and may
  be used to document the translation.

  If k is some other type (most usefully a string), it is converted to a string and returned. Any other arguments are ignored.
  "
  ([lang translator]
   (fn [k & args]
     (let [k* (if (keyword? k) [k] k)]
       (if (vector? k*)
         (do
           ;(println lang (first k*) args)
           (let [translation (translator lang (first k*) args)]
             ; use supplied default(s) if the translation is missing
             (if (starts-with? translation "{")             ; indicates a missing entry
               k*                                           ;(apply str (rest k*))                          ; return the original vector
               translation)))
         (str k))))))


(defn error-handler [{:keys [callback status status-text]}]
  (println "error dispatch - status : " status "\ntext: " status-text)
  (callback (js/Error. (str status " " status-text))))

;
; Read the set of supported languages from "languages.edn" into state

#_(defn handle-languages
    "if the languages file contains a set of two character keys, use it as a local list"
    ([ref edn-rsp]
     (let [rsp (edn/read-string edn-rsp)]
       (when (and (set? rsp) (every? #(and (keyword? %) (count (name %))) rsp))
         (swap! ref assoc :languages rsp)))))

(defn file-error [{:keys [status status-text]}]
  (.log js/console (str "file error: " status " " status-text)))


#_(defn get-languages
    ([]
     (get-languages "languages.edn"
                    {:error-handler file-error
                     :handler       handle-languages}))
    ([m]
     (get-languages "languages.edn" m))
    ([url {:keys [error-handler handler]}]
     (GET url {:error-handler file-error
               :handler       handler
               :format        :transit})
     ))


;
; dictionary handling
;
(defn process-dict-op
  "Process ops over a dictionary, returning a new dictionary.
  If the ops merge in words from a new language, that is merged in to the supported languages set"
  ([dict [op & args] wrap?]
   ;(print "op" op args)
   (case op
     :upsert (let [[lang new-words] args
                   current-lang (:lang dict)
                   ;_ (print "dict " dict)
                   ;_ (print "lang " lang)
                   ;_ (print "new words " new-words)
                   ;_ (println "selected lang is" (:lang dict))
                   ;_ (println "adding language" lang "to" (:languages dict))
                   new-lang-dict (-> dict
                                     (update :languages conj lang)
                                     (update-in [:translations lang] merge new-words))]
               (assoc new-lang-dict
                 :translator (wrap-translator current-lang (tongue/build-translate (:translations new-lang-dict)))))

     :switch (let [[lang] args]
               ;(println "switch lang" lang)
               ;(print "translations" (get-in dict [:translations lang]) lang)
               (if (and lang (not (empty? (get-in dict [:translations lang]))))
                 (do                                        ;(println "switching to language" lang)
                   (assoc dict :lang lang
                               :translator (wrap-translator lang (tongue/build-translate (:translations dict)))))
                 dict))))
  ([dict op-args]
   (process-dict-op dict op-args true))
  )

(defn process-dict-ops
  "process a sequence of operations into the translation state"
  [t-state op-args]
  ;(print "p-d-o" op-args)
  (reduce process-dict-op t-state (conj op-args [:switch (:lang (get-settings! {:lang :en}))]))
  )


(comment
  (publish language-change (:lang (get-settings! {:lang :en}))))


(defn handle-dictionary
  "process a sequence of operations on the dictionary, inserting them into the translation state @ref.
  USED IN PRODUCTION DICTIONARY LOAD."
  ([ref edn-rsp]
   ; ref must contain a :languages set
   {:pre (set? (:languages @ref))}
   (let [op-args (edn/read-string edn-rsp)]
     (when (and (seq op-args)
                (every? (fn [[op lang & args]]
                          (and (#{:upsert :switch} op)      ; op is valid
                               (keyword? lang)
                               (= 2 (count (name lang)))    ; accept lang codes of 2 characters
                               ))
                        op-args))
       (reset! ref (process-dict-ops @ref op-args)))))
  )


(defn get-dictionary
  "read dictionary ops from a url.
  USED IN PRODUCTION DICTIONARY LOAD"
  ([url {:keys [on-error handler]}]
   (GET url {:error-handler file-error                      ;on-error
             :handler       handler
             :format        :transit                        ;:transit
             })))

;;
;; load translation state; Call this to set up the translation system.
;;
(defn load-translations*
  "load the initial dictionary into the rtdb t-state.
  local dictionary-url is the relative url to a txt file containing :upsert and :switch commands.
  At runtime, the callback handler is usually a call to handle-dictionary with ref bound to the t-state-change cursor.
  At test time this may be different.

  USED IN PRODUCTION DICTIONARY LOAD"
  ([local-dictionary-url handler]
   (get-dictionary
     local-dictionary-url {:on-error file-error
                           :handler  handler})))




(defn validate-op-args [op-args]
  (and (seq op-args)
       (every? (fn [[op lang & args]]
                 (condp = op
                   :upsert (and
                             (seq? args)
                             (map? (first args))
                             (keyword? lang))
                   :switch (keyword? lang)
                   false))
               op-args)))

(defn read-json-dict
  "Note that his code should be shared with the babashka bin/getdictionary.clj script, once we work out how to do that.
  Take json read in from the dictionary api end point and convert it into a valid clojure data structure with valid
  keywords in the form :ns/key rather than teh string ':ns#key'."
  [json]
  (->> json
       (map
         (fn [[cmd lang m]]
           [(keyword cmd)
            (keyword lang)
            (into {} (map
                       (fn [[k v]]
                         [(->> (split k #"#")
                               (interpose "/")
                               (apply str)
                               (keyword)) v])
                       m))]))))


;(def edit-dictionary (read-string (slurp "resources/public/dictionary.txt")))
;(def prod-dictionary (read-string (slurp "resources/public/prod_dictionary.txt")))

; create runtime product dictionary
#_(->> (concat prod-dictionary
               (->> *input*
                    (mapv
                      (fn [[cmd lang m]]
                        [(keyword cmd)
                         (keyword lang)
                         (into {} (map
                                    (fn [[k v]]
                                      [(->> (split k #"#")
                                            (interpose "/")
                                            (apply str)
                                            (keyword)) v])
                                    m))]))))
       (into [])
       (spit "resources/public/dict_es.txt"))


;;
;; load local translations into state; Do this once at startup
;;
(defn load-all-translations
  "Load the initial dictionary into the rtdb t-state from the live-dictionary-url - a static txt file on the server.
   Once complete, and this is just used in the editing system, also download all translations on the server, and merge them.

   Both remote and local dictionaries contain a vector of [:upsert lang translations] commands, and these are added
   into the ref atom or cursor on reception. Once there, they are used to translate all text wrapped in ttt function calls.

   If a [:switch lang] command is received then the default language will change too.
   We can use transit here since we are not dependent on coast middleware which only supports json."
  [static-chan ok-chan err-chan dictionary-endpoint ref]
  ;{:pre (set? (:languages @ref))}                           ; ref must contain a :languages set

  (get-dictionary
    "dictionary.txt"
    {:on-error #(put! err-chan %)
     :handler  #(put! static-chan %)})

  (go
    (let [[seed-dict port] (alts! [static-chan err-chan])
          seed-dict (edn/read-string seed-dict)]

      (if (= static-chan port)

        (do
          (reset! ref (process-dict-ops @ref seed-dict))
          ;(println "seed-dict = " seed-dict)
          ;(println "ref:" @ref)
          ;(println "POSTING to " dictionary-endpoint)
          (POST dictionary-endpoint {:handler       #(put! ok-chan %)
                                     :error-handler #(put! err-chan %)
                                     :format        :transit})
          (go
            (let [[response port] (alts! [ok-chan err-chan])]
              (if (= ok-chan port)
                (let [op-args (read-json-dict response)]
                  (if (validate-op-args op-args)
                    (reset! ref (process-dict-ops @ref op-args))
                    (js/alert (str "Local dictionary parse failure" #_(pr-str response)))))
                (js/alert (str "error from" dictionary-endpoint ":" (pr-str response)))))))
        (js/alert (str "error from dictionary.txt " (pr-str seed-dict)))))))


(comment
  (process-dict-ops
    {:lang :en}
    [[:upsert [:en {:lang :en, :new-words "new words"}]]])

  ; we can process a composition of op-args into the dictionary
  (process-dict-ops
    {:lang         :en
     :translations {:en {}}
     :languages    #{}}
    [[:upsert :en {:lang :en, :new-words "new words"}]
     [:upsert :es {:lang :en :some-key "some-value"}]])

  )
