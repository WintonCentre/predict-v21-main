(ns predict3.results.text
  (:require [rum.core :as rum]
            [predict3.components.helpful-form-groups :refer [form-entry]]
            [predict3.state.config :refer [input-groups get-input-default]]
            [predict3.components.button :refer [year-picker]]
            [predict3.results.common :refer [common-results-text population-text stacked-yearly-values stacked-bar-yearly-props
                                             result-scroll-height labelled-year-picker press-and-hold]]
            [predict3.state.run-time :refer [input-change input-cursor input-label results-cursor
                                             year-selected rounded-cursor h-cache-cursor ttt-cursor]]
            [predict3.results.util :refer [treatment-keys* avoid-decimals larger-overall-survival without-h10 h10-benefit] :as util]
            [predict3.results.marshalling :refer [additional-benefit-map additional-benefit-kvs all-rounded-benefits]]
            [predict3.mixins :refer [set-default]]
            [graphics.simple-icons :refer [icon]]
            [pubsub.feeds :refer [publish]]
            ))

(defn benefit [data key]
  (key data))

(defn benefits [data & keys]
  (apply + (map #(% data) keys)))

(defn emph [n]
  (if false
    [:span {:style {:font-size "16px" :font-weight "bold"}} n]
    n))

(comment
  ; later
  (def icons? false)
  (defn woman [] (icon {:family :ionicon} "ion-woman"))
  (defn line [data treatments key]
    [:p [:span {:style {:font-size "14px" :color fill :line-height "0px"}}] (map woman (range (benefits data key)))])

  (def ttt @ttt-cursor)
  (ttt [:info/horm-8])
  )

(defn h-t [ttt] (ttt [:texts/h-t "hormone therapy"]))
(defn r-t [ttt] (ttt [:ttt/r-t "radiotherapy"]))
(defn c-t [ttt] (ttt [:ttt/c-t "chemotherapy"]))
(defn t-t [ttt] (ttt [:ttt/t-t "trastuzumab"]))
(defn b-t [ttt] (ttt [:ttt/b-t "bisphosphonates"]))
(defn comma [ttt] (ttt [:ttt/comma ", "]))
(defn ox-comma [ttt] (ttt [:ttt/ox-comma ", and "]))

;
; duplicate keys on comma - maybe keep as string?
;


(defn last-treatment-class
  [horm radio chemo tra bis]
  (cond
    bis "bis"
    tra "tra"
    chemo "chemo"
    radio "radio"
    horm "horm"))


(defn treatment-list
  [ttt]
  (let [v [(h-t ttt) (r-t ttt) (c-t ttt) (t-t ttt) (b-t ttt) (comma ttt) (comma ttt) (comma ttt) (ox-comma ttt)]
        [h-t r-t c-t t-t b-t comma1 comma2 comma3 ox-comma] (if (string? (first v))
                                                              v ; no need for react keys
                                                              (map-indexed #(rum/with-key %2 %1) v))]
    {
     [true, false, false, false, false] h-t
     [false, true, false, false, false] r-t
     [false, false, true, false, false] c-t
     [false, false, false, true, false] t-t
     [false, false, false, false, true] b-t

     [true, true, false, false, false]  [:span h-t ox-comma r-t]
     [true, false, true, false, false]  [:span h-t ox-comma c-t]
     [true, false, false, true, false]  [:span h-t ox-comma t-t]
     [true, false, false, false, true]  [:span h-t ox-comma b-t]
     [false, true, true, false, false]  [:span r-t ox-comma c-t]
     [false, true, false, true, false]  [:span r-t ox-comma t-t]
     [false, true, false, false, true]  [:span r-t ox-comma b-t]
     [false, false, true, true, false]  [:span c-t ox-comma t-t]
     [false, false, true, false, true]  [:span c-t ox-comma b-t]
     [false, false, false, true, true]  [:span t-t ox-comma b-t]

     [false, false, true, true, true]   [:span c-t comma1 t-t ox-comma b-t]
     [false, true, false, true, true]   [:span r-t comma1 t-t ox-comma b-t]
     [false, true, true, false, true]   [:span r-t comma1 c-t ox-comma b-t]
     [false, true, true, true, false]   [:span r-t comma1 c-t ox-comma t-t]

     [true, false, false, true, true]   [:span h-t comma1 t-t ox-comma b-t]
     [true, false, true, false, true]   [:span h-t comma1 c-t ox-comma b-t]
     [true, false, true, true, false]   [:span h-t comma1 c-t ox-comma t-t]
     [true, true, false, false, true]   [:span h-t comma1 r-t ox-comma b-t]
     [true, true, false, true, false]   [:span h-t comma1 r-t ox-comma t-t]
     [true, true, true, false, false]   [:span h-t comma1 r-t ox-comma c-t]

     [false true true true true]        [:span r-t comma1 c-t comma2 t-t ox-comma b-t]
     [true false true true true]        [:span h-t comma1 c-t comma2 t-t ox-comma b-t]
     [true true false true true]        [:span h-t comma1 r-t comma2 t-t ox-comma b-t]
     [true true true false true]        [:span h-t comma1 r-t comma2 c-t ox-comma b-t]
     [true true true true false]        [:span h-t comma1 r-t comma2 c-t ox-comma t-t]

     [true true true true true]         [:span h-t comma1 r-t comma2 c-t comma3 t-t ox-comma b-t]

     }))

(comment
  (get (treatment-list ttt) [true, false, true, false, true])
  ;=> "hormone therapy, radiotherapy and trastuzumab"

  (get (treatment-list ttt) [true, false, true, false, false])
  )


(rum/defc texts < rum/static rum/reactive [{:keys [ttt year data treatments delay hd printable]}]
  (let [surg (data :surgery)
        horm (data :horm)
        radio (data :radio)
        chemo (data :chemo)
        tra (data :tra)
        bis (data :bis)
        rounded? (rum/react rounded-cursor)

        list-item (fn [horm radio chemo tra bis]
                    (let [shrctb (util/round (+ surg horm radio chemo tra bis) rounded?)
                          hrctb (util/round (+ horm radio chemo tra bis) rounded?)
                          treatment-mask [(pos? horm) (pos? radio) (pos? chemo) (pos? tra) (pos? bis)]]
                      (if (pos? hrctb)
                        [:li [:p (str shrctb)
                              [:span {:key 1}
                               (ttt [:texts/out-of-1 " out of "]) "100"
                               (ttt [:texts/out-of-2 " women "])]

                              (if hd
                                [:span {:key 2} (ttt [:texts/out-of-3 "who received a further 5 years hormone therapy"])]
                                [:span {:key 3}
                                 (ttt [:texts/out-of-4 "treated with "])
                                 (get (treatment-list ttt) treatment-mask)])
                              [:span {:key 5} (ttt [:texts/out-of-5 " are alive (an extra "])] (str hrctb)
                              (ttt [:texts/out-of-6 ")."])
                              ]]
                        (when hd [:li
                                  [:span.no-benefit {:key 1}
                                   (ttt [:texts/no-h10-benefit-1 "There is no benefit of further hormone therapy "]) year]
                                  [:span {:key 2}
                                   (ttt [:texts/no-h10-benefit-2 " years after surgery"])]]))))

        ]

    [:.row
     (when-not printable
       [:.col-sm-12 {:style {:margin-top 15 :margin-left 0 :margin-bottom 10 :display "inline-block" :font-size 16}}
        (labelled-year-picker ttt)
        ])

     [:.col-sm-12

      [:p.surgery (emph (util/round surg rounded?)) (ttt [:texts/h10-out-of-1 " out of "]) (emph 100) (ttt [:texts/h10-out-of-2 " women "])
       (if hd
         (ttt [:texts/h10-out-of-3 "who have not received further hormone treatment"])
         (ttt [:texts/h10-out-of-4 "treated with surgery only"]))
       "," (ttt [:texts/h10-out-of-5 " live at least "])
       year (ttt [:texts/out-of-6-1 " years from surgery"])
       (when hd
         [:b
          (ttt [:table/year-pick-2-2 " (that is "])
          (- year 5)
          (ttt [:table/year-pick-2-3 " years from now). "])])]

      [:ul
       [:span.horm {:key 1} (list-item horm 0 0 0 0)]
       (when-not hd
         (reduce conj []
                 [(when (pos? radio)
                    [:span.radio {:key 2} (list-item horm radio 0 0 0)])
                  (when (pos? chemo)
                    [:span.chemo {:key 3} (list-item horm radio chemo 0 0)])
                  (when (pos? tra)
                    [:span.tra {:key 5} (list-item horm radio chemo tra 0)])]))
       (when (pos? bis)
         [:span.bis {:key 6} (list-item horm radio chemo tra bis)])
       ]]]))

(rum/defc results-in-text < rum/reactive (set-default :result-year)
  [{:keys [ttt printable]}]

  (let [year (rum/react (year-selected))
        treatments treatment-keys*
        delay (rum/react (input-cursor :delay))
        hd (and (rum/react (input-cursor :horm)) (= :ys5 delay))
        cache (rum/react h-cache-cursor)
        results (rum/react results-cursor)
        additionals (all-rounded-benefits {:annual-benefits results
                                           :year            year
                                           :tks             treatment-keys*})
        data (if hd
               (assoc (into {} additionals)
                 :surgery (* 100 (without-h10 cache year))  ;#_(apply + ((juxt :surgery :radio :chemo :tra :bis :horm) h5))
                 :horm (* 100 (h10-benefit results cache year))
                   :bis 0
                   :radio 0
                   :chemo 0
                 :tra 0)
               additionals)

        ]


    [:div
     [:.row
      [:.col-sm-12
       (texts {:ttt ttt :year year :data data :treatments treatments :printable printable :hd hd :delay delay})]]

     [:.row
      [:.col-sm-12
       [:p.other (ttt [:text/die-anyway-1 "Of the women who would not survive, "])
        (util/round (:oth data) (rum/react rounded-cursor))
        (ttt [:text/die-anyway-2 " would die due to causes not related to breast cancer."])]
       ]]
     (when-not printable
       [:row
        [:col-sm-12 (press-and-hold ttt)]])
     ]))

(comment
  (all-rounded-benefits {:annual-benefits @results-cursor
                         :year            @(year-selected)
                         :tks             treatment-keys*})
  (additional-benefit-kvs {:annual-benefits @results-cursor
                           :year            @(year-selected)
                           :tks             treatment-keys*})
  (nth @results-cursor 10)

  )