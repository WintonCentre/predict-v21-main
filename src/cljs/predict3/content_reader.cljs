(ns predict3.content-reader
  (:require [predict3.content :refer [content]]
            [predict3.results.util :refer [alison-blue-1 alison-blue-2 alison-blue-3]]
            [rum.core :as rum]))

(def ^{:doc     "Regular expression that parses a CSS-style id and class from an element name."
       :private true}
re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn match-id [tag id]
  "return true if tag contains a hash tag-id matching id."
  (let [[_ _ tag-id _] (re-find re-tag (str tag))]
    (= id tag-id)))

(defn match-node [ttt node id]
  (let [[x & xs] node]
    (cond
      (nil? x) nil
      (keyword x) (if (match-id x id) {:x x :xs xs} (match-node ttt xs id))
      (vector? x) (if-let [rv (match-node ttt x id)] rv (match-node ttt xs id))
      (seq xs) (match-node ttt xs id)
      :else nil
      )
    ))

(defn add-hiccup-key [key [tag & args :as hiccup]]
  (let [[m & rest-args] args
        [mk rest-args*] (if (map? m) [(assoc m :key key) rest-args] [{:key key} args])]
    ; (prn "check: " tag mk m rest-args*)
    (into [] (concat [tag mk] rest-args*))))

(comment
  (add-hiccup-key "key3" [:p])
  (add-hiccup-key "key3" [:p.emphasise])
  ;=> [:p {:key "key3"}]
  (add-hiccup-key "key3" [:p "Hello there"])
  ;=> [:p {:key "key3"} "Hello there"]
  (add-hiccup-key "key3" [:p [:p "Hello there"] [:p "Bye then!"]])
  ;=> [:p {:key "key3"} [:p "Hello there"] [:p "Bye then!"]]
  (add-hiccup-key "key3" [:p {:foo 1}])
  ;=> [:p {:foo 1, :key "key3"}]
  (add-hiccup-key "key3" [:p {:foo 2} [:p "Hello there"]])
  ;=> [:p {:foo 2, :key "key3"} [:p "Hello there"]]
  (add-hiccup-key "key3" [:p {:foo 3} [:p "Hello there"] [:p "Bye then!"]])
  ;=> [:p {:foo 3, :key "key3"} [:p "Hello there"] [:p "Bye then!"]]
  )

(defn section
  ([ttt node id]
   (let [{:keys [x xs]} (match-node ttt node id)]
     xs))

  ([ttt id] (section ttt (content ttt) id))
  )

(defn all-subsections
  "loop through subsections of a section identified by id adding keys."
  [ttt id]
  (let [node (section ttt id)]
    (for [k (range (count (rest node)))
          :let [[sec title & content] (nth (rest node) k)]]
      [:section {:key k}
       [:h2 {:style {:color alison-blue-3}} title]
       (map-indexed #(add-hiccup-key (str "k" %1) %2)
                    content)
       ])))

(comment

  ;content

  (re-find re-tag ":section#adjuvant-treatments.input-box")

  (defn ttt [[k s]] s)

  (defn mock-data [ttt]
    [:div#top
                  [:section#1.ignore
      [:section#this-one.found [:p (ttt [:mock/m1 "this"])]]
      [:section#next-one.found [:p (ttt [:mock/n2 "next"])
                                [:p#foo (ttt [:mock/n3 "foo"])]]]
      [:section#last-one.found [:p (ttt [:mock/n4 "last"])]]
                   ]])

  (rum/defc mock-data-comp []
    mock-data
    )

  mock-data
  mock-data*

  (match-id :div#top "top")

  (match-node (mock-data ttt) "top")
  (match-node (mock-data ttt) "1")
  (match-node (mock-data ttt) "next-one")
  (match-node (mock-data ttt) "foo")

  content
  (match-node content "welcome")
  (match-node content "why")
  (section ttt "welcome")

  (def clin-info (section ttt content "clinician-information"))
  (section ttt clin-info "oncotype")

  (match-id ":section#adjuvant-treatments.input-box" "adjuvant-treatments")
  #_(match-id ":section#treatments.input-box" "adjuvant-treatments")

  )