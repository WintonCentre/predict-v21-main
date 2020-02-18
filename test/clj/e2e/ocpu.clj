(ns e2e.ocpu
  "Accesses predict benefit functions served by opencpu. Start the OPENCPU server from RStudio first.
  See https://github.com/WintonCentre/predict-opencpu-server.git"
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [clojure-csv.core :as csv]
            [semantic-csv.core :as sc]))

(defn csv-url2map [r-function & [param-map]]
  (-> r-function
       (#(str "http://localhost:5656/ocpu/library/Predictv2.2/R/" % "/csv"))
       (client/post {:form-params param-map})
       :body
       (csv/parse-csv)
       (sc/mappify)))

(defn benefits22
  "Get survival benefits"
  [& [param-map]]
  (csv-url2map "benefits22" param-map)
  )

(defn benefits222
  "Get disease free survival benefits"
  [& [param-map]]
  (csv-url2map "benefits222" param-map)
  )

(defn benefits2210
  "Get h10 over h5 benefits when delay = 1"
  [& [param-map]]

  (csv-url2map "benefits2210" (assoc param-map :delay 5 :horm 1))
  )

(comment
  (csv-url2map "http://localhost:5656/ocpu/library/Predictv2.2/R/benefits22/csv")

  (benefits22)
  (benefits22 {:age.start 57
               :screen 1                                 ;screening 1, symptoms 0
               :size 20
               :grade 2
               :nodes 10
               :er 1
               :her2 1
               :ki67 1
               :generation 3                                ;0, 2 or 3
               :horm 1
               :traz 1
               :bis 1
               })

  (benefits222)
  (benefits2210
    {:generation 3, :bis 1, :tra 1, :screen 1, :er 1, :ki67 1, :size 3, :nodes 2, :grade 3, :age.start 57, :her2 1, :horm 1, :delay 5})

  (def params {:generation 3, :bis 1, :tra 1, :screen 1, :er 1, :ki67 1, :size 3, :nodes 1, :grade 3, :age.start 57, :her2 9, :horm 1 :delay 5})

  ; from R docs...

  ;' @param age.start Age at time of surgery
  ;' @param screening Clinically detected = 0, screening detected = 1
  ;' @param size Tumour size mm
  ;' @param grade Tumour grade
  ;' @param nodes Number positive nodes
  ;' @param er ER+ = 1, ER- = 0
  ;' @param her2 HER2+ = 1, HER2- = 0, missing = 9
  ;' @param ki67 KI67+ = 1, KI67- = 0, missing = 9
  ;' @param generation Chemo generation 0, 2 or 3 only
  ;' @param horm Hormone therapy Yes = 1, no = 0
  ;' @param traz Trastuzumab therapy Yes = 1, no = 0
  ;' @param bis Bisphosphonate therapy Yes = 1, no = 0
  ;' @param radio Radiotherapy Yes = 1, no = 0
  ;' @param delay 0 or 5 years since surgery for h10 benefit

  )

