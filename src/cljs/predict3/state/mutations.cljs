(ns predict3.state.mutations
  (:require [predict3.state.run-time :refer [model
                                             input-cursor input-change
                                             input-changes
                                             input-default
                                             active-results-pane
                                             active-results-change
                                             completed-results-cursor completed-results-change
                                             mockup-cursor mockup-change
                                             results-cursor
                                             h-cache-cursor
                                             h10-latch-cursor
                                             results-change
                                             get-inputs
                                             help-key-cursor help-key-change
                                             settings-cursor settings-change
                                             print-cursor print-change
                                             route
                                             route-change
                                             hide-warning-change hide-warning-cursor
                                             show-uncertainty-change show-uncertainty-cursor
                                             year-selected
                                             media-change media-cursor
                                             is-completed
                                             h10-latch-cursor
                                             t-state-cursor t-state-change
                                             language-change
                                             ttt-cursor

                                             active-modal-change active-modal-cursor

                                             ; edit version from here
                                             edit-cursor edit-change
                                             text-change
                                             new-text-cursor
                                             new-text-change
                                             add-language-modal
                                             add-language
                                             rounded-change
                                             rounded-cursor
                                             ]]
            [predict3.state.config :refer [input-groups get-input-default rtl-languages]]
            [predict3.state.local-storage :refer [get-settings! put-settings!]]
            [predict3.models.runner :refer [recalculate-model]]
            [pubsub.feeds :refer [publish subscribe]]
            [clojure.core.async :refer [timeout <!]]
            [bide.core :as r]
            [predict3.router :refer [router use-hash-fragment]]
            [predict3.results.util :refer [clip]]
            [translations.tongue-base :refer [load-translations* handle-dictionary process-dict-op]]
            [translations.tranny-api :refer [upload-translation delete-translation]]
            )
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn clear-inputs []
  (reset! h10-latch-cursor nil)
  (doseq [[key topic] (input-changes)
          :when key
          :when topic]
    "don't zap the settings local storage on startup"
    (cond
      (= key :enable-radio)
      (let [enable-radio (:enable-radio (get-settings! {:enable-radio :no}))]
        (reset! (input-cursor :enable-radio) enable-radio))

      #_(= key :ten-fifteen)
      #_(let [ten-fifteen (:ten-fifteen (get-settings! {:ten-fifteen 15}))]
        (reset! (input-cursor :ten-fifteen) ten-fifteen))

      (= key :default-tab)
      (let [default-tab (:default-tab (get-settings! {:default-tab :table}))]
        (reset! (input-cursor :default-tab) default-tab)
        (publish active-results-change (name default-tab)))

      (= key :enable-bis)
      (let [enable-bis (:enable-bis (get-settings! {:enable-bis :yes}))]
        (reset! (input-cursor :enable-bis) enable-bis))

      (= key :enable-h10)
      (let [enable-h10 (:enable-h10 (get-settings! {:enable-h10 :no}))]
        (reset! (input-cursor :enable-h10) enable-h10))

      (= key :enable-dfs)
      (let [enable-dfs (:enable-dfs (get-settings! {:enable-dfs :no}))]
        (reset! (input-cursor :enable-dfs) enable-dfs))

      :else (publish topic (if (#{:age :size :nodes} key) "" nil)))
    )
  (publish results-change nil))


(defn log [topic old new]
  ;(println "Mutate: " topic " " old " -> " new)
  )

(defn subscribe-to [change cursor & [silent]]
  (subscribe change
             #(do (when-not silent (log %1 @cursor %2))
                  (reset! cursor %2)
                  )))

(defn update-tra-enabled []
  (let [her2+ (= @(input-cursor :her2-status) :yes)
        chemo @(input-cursor :chemo)
        tra @(input-cursor :tra)]
    (if (and her2+ chemo)
      (when (= tra :disabled)
        (reset! (input-cursor :tra) nil))
      (reset! (input-cursor :tra) :disabled))
    ))

(defn mutator []

  (doseq [[key change] (input-changes)]
    (when change
      (subscribe change
                 (fn [topic value]
                   (let [old @(input-cursor key)]
                     ;(log topic old value)

                     (cond
                       (= key :age)
                       (reset! (input-cursor :age) (if (or (= "" value) (nil? value))
                                                     ""
                                                     (str (clip {:value value :min 0 :max 85}))))

                       ;; when nodes changes to zero, micromets can be entered, otherwise, and initially, they are disabled
                       (= key :nodes)
                       (do (if (= 1 (js/parseInt value))
                             (reset! (input-cursor :micromets) nil)
                             (reset! (input-cursor :micromets) :disabled))
                           (reset! (input-cursor :nodes) value))

                       ;; when :er-status is negative, hormone therapy is disabled
                       (= key :er-status)
                       (do (cond
                             (= :no @(input-cursor :er-status))
                             (do (reset! (input-cursor :horm) nil))

                             (= :no value)
                             (do (reset! (input-cursor :horm) :disabled)))
                           (reset! (input-cursor :delay) nil)
                           (reset! (input-cursor :er-status) value))

                       ;; when :post-meno is pre, bisphosphonates are disabled
                       (= key :post-meno)
                       (do (cond
                             (= :pre @(input-cursor :post-meno)) (reset! (input-cursor :bis) nil)
                             (= :pre value) (reset! (input-cursor :bis) :disabled))
                           (reset! (input-cursor :post-meno) value))

                       ;; when :her2-status is negative or unknown, trastuzumab is disabled
                       (= key :her2-status)
                       (do
                         (reset! (input-cursor :her2-status) value)
                         (update-tra-enabled)
                         )

                       (= key :chemo)
                       (do
                         (reset! (input-cursor :chemo) value)
                         (update-tra-enabled))

                       ;;
                       (= key :enable-radio)
                       (do
                         ;(.log js/console ":enable-radio" value)
                         (reset! (input-cursor :enable-radio) value)
                         (put-settings! {:enable-radio value}))

                       #_(= key :ten-fifteen)
                       #_(do
                         ;(.log js/console ":ten-fifteen" value)
                         (if (< value @(year-selected))
                           (reset! (year-selected) value))
                         (reset! (input-cursor :ten-fifteen) value)

                         (put-settings! {:ten-fifteen value}))

                       (= key :enable-bis)
                       (do
                         ;(.log js/console ":enable-bis" value)
                         (reset! (input-cursor :enable-bis) value)
                         (put-settings! {:enable-bis value}))

                       (= key :default-tab)
                       (do
                         ;(.log js/console ":default-tab" (name value))
                         (reset! (input-cursor :default-tab) value)
                         (put-settings! {:default-tab value})
                         (publish active-results-change (name value))
                         )

                       (= key :enable-h10)
                       (do
                         ;(.log js/console ":enable-h10" value)
                         (reset! (input-cursor :enable-h10) value)
                         (put-settings! {:enable-h10 value}))

                       (= key :enable-dfs)
                       (do
                         ;(.log js/console ":enable-dfs" value)
                         (reset! (input-cursor :enable-dfs) value)
                         (put-settings! {:enable-dfs value}))

                       (= key :delay)
                       (do
                         (reset! (input-cursor :horm) :h10)
                         ;(reset! (input-cursor :result-year) 15)
                         (reset! (input-cursor :delay) value))

                       :else
                       (reset! (input-cursor key)
                               (if (nil? value)
                                 (get-input-default input-groups key)
                                 (do (cond
                                       (= :h10 value)
                                       (do
                                         (reset! h10-latch-cursor true)
                                         ;(publish (input-change :ten-fifteen) 15)
                                         )

                                       ;
                                       (= :ys5 value)
                                       (reset! (year-selected) 15))
                                     value)))
                       )
                     #_(reset! (input-cursor key) (if (nil? value) (get-input-default input-groups key) value))

                     ;;
                     ;; if delayed, then we will need both h5 and h10 results. Need to be careful about caching the
                     ;; correct (other) one. We may be updating :horm on this mutation.
                     ;;
                     (when (= :ys5 @(input-cursor :delay))
                       (reset! h-cache-cursor
                               (recalculate-model model
                                                  (assoc (get-inputs)
                                                    :horm (if (= :horm topic)
                                                            (if (= :h5 value) :h10 :h5)
                                                            (if (= :h5 @(input-cursor :horm)) :h10 :h5))))))


                     (recalculate-model model (get-inputs)))))

      ))


  ;; various
  (subscribe-to rounded-change rounded-cursor false)
  (subscribe-to media-change media-cursor false)
  (subscribe-to mockup-change mockup-cursor true)
  ; (subscribe-to help-key-change help-key-cursor true)
  (subscribe-to hide-warning-change hide-warning-cursor true)
  (subscribe-to show-uncertainty-change show-uncertainty-cursor true)

  (subscribe completed-results-change
             (fn [_]
               (reset! completed-results-cursor true)))

  (subscribe results-change
             (fn [_ results]
               (if (and results (not (is-completed)))
                 (publish completed-results-change true)
                 )))

  (subscribe-to results-change results-cursor false)


  ;(subscribe-to active-results-change active-results-pane true)
  (subscribe active-results-change
             (fn [_ tab-label]
               (reset! active-results-pane tab-label)))


  (subscribe help-key-change
             (fn [_ help-key]
               (reset! help-key-cursor help-key)
               (.modal (js/$ "#topModal") "show")))

  (subscribe settings-change
             (fn [_ help-key]
               (reset! settings-cursor help-key)
               (.modal (js/$ "#settingsModal") "show")))

  (subscribe print-change
             (fn [_ val]
               (reset! print-cursor val)
               (.modal (js/$ "#printModal") "show")))

  ;(subscribe-to route-change route true)
  (subscribe route-change
    (fn [_ [page param1 param2 :as rvec]]
      (reset! route rvec)
      (r/navigate! router page param1 param2)

      ;
      ; deleting the following line appears to fix something. But what! HOME BUG???
      ;
      ; The line is necessary to get the browser to make the home page reload
      (when (= page :home) (set! (.-href js/location) (if (use-hash-fragment) "/#" "/")))

      ))

  (subscribe language-change
             (fn [_ lang]
               ;(println "language-change to:" lang)
               (swap! t-state-cursor process-dict-op [:switch lang])
               (put-settings! {:lang lang})
               (.attr (js/$ "html") "dir" (if (rtl-languages lang) "rtl" "ltr"))))

  (subscribe t-state-change
             (fn [_ url]
               (load-translations* url (partial handle-dictionary t-state-cursor))))

  (subscribe edit-change
             (fn [_ arg]
               (let [text (@ttt-cursor arg)]
                 (swap! edit-cursor assoc
                        :edit-arg arg
                        :edit-key (if (and (vector? arg) (> (count arg) 0)) (first arg) arg)
                        :text (if (and (vector? arg) (> (count arg) 1))
                                (second arg)
                                text))
                 (.modal (js/$ "#editorModal") "show")
                 ;(println "edit-change: state=" @edit-cursor)
                 )))

  (subscribe new-text-change
             (fn [_ [edit-key new-text]]
               (let [lang (:lang @t-state-cursor)]
                 ;(println "new-text-change: lang " lang "key" edit-key "text" new-text)
                 (reset! new-text-cursor new-text)
                 ;(swap! t-state-cursor process-dict-op [:upsert (:lang @t-state-cursor) {edit-key new-text}])
                 ;(update-key-lang (subs (str edit-key) 1) (name lang) new-text)
                 )
               ))

  (subscribe text-change
             (fn [_ [edit-key new-text]]
               (let [lang (:lang @t-state-cursor)]
                 ;(println "text-change: lang " lang "key" edit-key "text" new-text)
                 (swap! t-state-cursor process-dict-op [:upsert (:lang @t-state-cursor) {edit-key new-text}])
                 (reset! new-text-cursor nil)
                 (upload-translation edit-key (name lang) new-text)
                 )
               ))

  (subscribe add-language-modal
             (fn [_]
               ;(println "add-language-modal")
               (.modal (js/$ "#newLanguageModal") "show")
               ))

  (subscribe add-language
             (fn [_ [new-lang]]
               (when (= 2 (count new-lang))
                 (swap! t-state-cursor update :languages conj (keyword new-lang)))))

  ;; Now clear all values to nil/default
  (clear-inputs))

(comment

  (publish route-change [:home nil nil])
  (publish language-change :en)
  (publish language-change :de)

  (get-input-default input-groups :neo)

  (count (.querySelectorAll js/document ".has-error"))

  (count (.querySelectorAll js/document "[data-key]")))


(comment
  ; lazy-seq example:

  (declare helper)

  (defn even? [n]
    (lazy-seq
      (if (zero? n)
        [true]
        (cons n (helper (dec n))))))

  (defn helper [n]
    (lazy-seq
      (if (zero? n)
        [false]
        (cons n (even? (dec n)))))))
