(ns translations.config)

(def translation-profile :prod)

; config file for the live dictionary
;(def live-dictionary-url "prod_dictionary.txt")
(def live-dictionary-url "/dict_es.txt")

(def initial-supported-langs #{:en :es})

(def predict-edit false)