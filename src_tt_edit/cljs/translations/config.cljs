(ns translations.config
  (:require [translations.tranny-api :refer [base-url]]))

(def translation-profile :edit)

; config file for the live dictionary
(def live-dictionary-url "/dictionary.txt")

(def initial-supported-langs #{:en :es :nl :it :fr :ja :pt})

(def predict-edit true)