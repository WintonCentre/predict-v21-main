#!/usr/bin/env bb

;;
;; Run this from the bash script getdictionary.sh in the project root
;;
;; Install babashka with homebrew for bb command

(require '[clojure.string :refer [split]]
  '[clojure.edn :refer [read-string]])

(let [[initial] *command-line-args*])

(def edit-dictionary (read-string (slurp "resources/public/dictionary.txt")))
(def prod-dictionary (read-string (slurp "resources/public/prod_dictionary.txt")))

; create runtime product dictionary
(->> (concat prod-dictionary
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

