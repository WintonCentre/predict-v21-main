(ns predict3.pages.faqs
  (:require [rum.core :as rum]
            [predict3.layout.header :refer [header header-banner footer footer-banner]]
            [predict3.content-reader :refer [section all-subsections]]
            [predict3.state.run-time :refer [route]]
            [predict3.results.util :refer [alison-blue-1 alison-blue-2 alison-blue-3 alison-pink]]
            [graphics.simple-icons :refer [icon]]
            [pubsub.feeds :refer [publish]]
            [interop.utils :refer [scrollTo]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            ))

#_(defn hr [] [:hr {:style {:color "red"}}])
(defn hr []
  [:hr {:style {:height     1
                :color      alison-blue-3
                :background alison-blue-3}}])

(defn faq-item
  "Render a faq question (q) and multiple answer paragraphs (as)"
  [q & as]
  [:div [:h3 q]
   (reduce conj [:div] (map (fn [a] [:div {:style {:font-size 16}} a]) as))])

(defn faqs
  [ttt]
  [:div
   [:h2#faqs (ttt [:faqs/faqs "FAQS"])]

   (faq-item (ttt [:faq-head-1 "Looking for advice?"])
     [:span {:style {:font-size 18}} (ttt [:faqs/head-2 "Information about treatments:"])]
     [:ul {:style {:font-size 16 :list-style-type "none"}}
      [:li {:key 1}
       [:a {:href "http://www.cancerresearchuk.org/about-cancer/breast-cancer/treatment" :rel "noopener" :target "blank "}
        "Cancer research UK "]]
      [:li {:key 2}
       [:a {:href "https://www.nhs.uk/conditions/breast-cancer/treatment/" :rel "noopener" :target "blank"} "NHS"]]]
     [:span {:style {:font-size 18}} (ttt [:faqs/head-3 "More information about side effects: "])]
     [:ul {:style {:font-size 16 :list-style-type "none"}}
      [:li {:key 3}
       [:a {:href   "https://www.breastcancercare.org.uk/information-support/facing-breast-cancer/going-through-treatment-breast-cancer/side-effects"
            :rel    "noopener"
            :target "blank"} "Breast Cancer Care"]]
      [:li {:key 2}
       [:a {:href "https://www.macmillan.org.uk/information-and-support/breast-cancer/coping/side-effects-and-symptoms" :rel "noopener" :target "blank"} "Macmillan"]]]
     [:span {:style {:font-size 18}} (ttt [:faqs/sources "Sources of advice and support: "])]
     [:ul {:style {:font-size 16 :list-style-type "none"}}
      [:li {:key 3}
       [:a {:href   "https://www.breastcancercare.org.uk/information-support/support-you/someone-talk" :rel "noopener"
            :target "blank "} "Breast Cancer Care "]]
      [:li {:key 0}
       [:a {:href   "https://www.nhs.uk/conditions/breast-cancer/treatment/#psychological-help" :rel "noopener"
            :target "_blank "} "NHS "]]
      [:li {:key 1}
       [:a {:href "http://www.healthtalk.org/peoples-experiences/cancer/breast-cancer-women/topics" :rel "noopener" :target "_blank "}
        (ttt [:faq/health-talk "Health Talk - videos of women's experiences with breast cancer and treatment options "])]]])


   (hr)
   (faq-item (ttt [:faqs1-q "What if I don’t have all the details needed for the input section?"])
     (ttt [:faqs1-a1 "If you select 'Unknown' for an input, the Predict tool will use the average value. This will simply make the results less personalised."]))
   (hr)
   (faq-item (ttt [:faqs2-q "How do I know that Predict gives the right answers?"])
     (ttt [:faqs2-a1-1-1 "Predict estimates what would be expected to happen to women with similar characteristics based on past
     data. The findings are based on women treated in the East of England but we have also tested that they
     give the same results on nearly 5,500 women treated in the West Midlands and a large database of women
     diagnosed under 40 in Nottingham. To the best of our knowledge
     the Predict tool works equally well for all women in the UK. We have also tested Predict on over 3,000
     women treated in British Columbia, Canada and a large group of women from the Netherlands. Other researchers have
     also validated Predict using patient groups from the Netherlands and Malaysia. Five scientific papers describing
     the work have been reviewed by scientists and clinicians (see Publications for details). In summary, Predict
     generates estimates based on large numbers of women who have undergone treatment in the past, but it can never
     say what will actually happen to an individual woman."]))
   (hr)
   (faq-item (ttt [:faqs3-q "If the data used is from patients decades ago won't the predictions it gives be out of date?"])
     (ttt [:faqs3-a1 "These predictions are based on patients diagnosed between 1999 and 2004, and include follow-up for
     up to 15 years. This is because in order to carry out long term predictions older data have to be used. It is
     possible that outcomes of these treatments will be different in patients diagnosed today - the use of older data is
     likely to slightly overestimate the benefit of treatment."]))
   (hr)
   (faq-item (ttt [:faqs4-q "What use are these kinds of statistics when as a patient I will either be cured or not?"])
     (ttt [:faqs4-a1 "Medical treatments don't work for everyone - whilst some people may get a huge benefit, others may get no benefit - only the harmful side effects.
     This makes choosing whether to try a treatment a difficult and personal choice. For cancer, treatments may be able to delay
     a cancer coming back or stop it coming back at all. From statistics, based on what has happened to people with similar cancers in the past
     when they tried a treatment, Predict tries to give the 'best guess' at the sort of benefits that a treatment option might give a particular patient.
     This can help inform a personal decision on whether to try it or not. Any potential benefits, though, should always be weighed against the possible harms of the side effects."]))
   (hr)
   (faq-item (ttt [:faqs5-q "What about radiotherapy?"])
     (ttt [:faqs5-a1 "For some women, radiotherapy is a potential treatment option. We plan to include
     it in the next version of the model."]))
   (hr)
   (faq-item (ttt [:faqs6-q "What about other treatments?"])
     (ttt [:faqs6-a1 "Many new types of treatment for breast cancer are being researched.  However, we are not able to include new treatments
     in Predict until large clinical trials have demonstrated the size of the benefit in breast cancer patients."]))
   (hr)
   (faq-item (ttt [:faqs7-q "What about neo-adjuvant treatment?"])
     (ttt [:faqs7-a1 "Sometimes chemotherapy is advised before initial surgery ('neo-adjuvant' chemotherapy).  The current version of Predict is not designed to be used under these circumstances."]))
   (hr)
   (faq-item (ttt [:faqs8-q "What about men with breast cancer?"])
     (ttt [:faqs8-a1 "Predict has only been designed and tested with data from women and should not be used to make predictions for men with breast cancer."]))
   (hr)
   (faq-item (ttt [:faqs9-q "What about metastatic cancer?"])
     (ttt [:faqs10-a1 "Predict is only relevant for women with early breast cancer. It does not make predictions for women whose breast cancer is already metastatic when diagnosed."]))
   (hr)
   (faq-item (ttt [:faqs10-q "What about DCIS or LCIS?"])
     (ttt [:faqs10-a1 "The calculations in Predict are only for women who have invasive breast cancer. These are not for use by women with DCIS (Ductal carcinoma in situ) or
     LCIS (Lobular carcinoma in situ)."]))
   (hr)
   (faq-item (ttt [:faqs11-q "Does Predict account for different types of surgery?"])
     (ttt [:faqs11-a1 "Predict is designed to be used for helping make decisions about treatment after the initial surgery. There is not currently enough
     data available to be able to take into account the effects of the different types of surgery on outcomes."]))
   (hr)
   (faq-item (ttt [:faqs12-q "What about side effects?"])
     (ttt [:faqs12-a1 "At the moment Predict only gives information about the benefits of each treatment, but every
     treatment also has the potential to cause side effects and it is important to weigh these up when considering
     treatment options. Charities such as Breast Cancer Care and Macmillan give good information on the side
     effects of each treatment. We plan to
     include an estimate of the likelihood of different side effects from each treatment in the next version of Predict."]))
   (hr)
   (faq-item (ttt [:faqs13-q "Who developed the Predict programme?"])
     (ttt [:faqs13-a1 "Development of the model was a collaborative project between the Cambridge Breast Unit, University of
      Cambridge Department of Oncology and the Eastern Cancer Information and Registration Centre (ECRIC) and was
      supported by an unrestricted educational grant from Pfizer Limited.
      They welcome any feedback you may have about Predict. If you have questions about its development or there are
      features you would like to have added to the model please let them know by emailing "])
     [:a {:href "mailto:info@predict.nhs.uk" :rel "noopener"} "info@predict.nhs.uk."])
   (hr)
   (faq-item (ttt [:faqs14-q "How was the computer programme developed?"])
     [:span (ttt [:faqs14-a1 "The team used information held by the Eastern Cancer Registry
                 and Information Centre, now part of the
                "]) [:a {:href "http://www.ncin.org.uk/collecting_and_using_data/national_cancer_data_repository/" :rel "noopener" :style {:text-decoration "underline"}}
                                                  "National Cancer Registration and Analysis Service"]
      (ttt [:faqs14-a2 " on nearly 5700 women treated for breast cancer between 1999 and 2003. Using this information they were able to
      see how individual factors, such as age and treatments received, affected survival at five years and ten years."])])
   (hr)
   (faq-item (ttt [:faqs15-q "Who designed the website?"])
     [:span (ttt [:faqs15-a1 "The website has been built by the "])
      [:a {:href "https://wintoncentre.maths.cam.ac.uk" :rel "noopener" :style {:text-decoration "underline"}}
       "Winton Centre for Risk & Evidence Communication"]
      (ttt [:faqs15-a2 " at the University of Cambridge. The site functionality and visualisation software is trademarked by the Winton Centre as
                 4U2C. However, we are happy for others to use it for similar purposes. Do contact us to discuss this at "])
      [:a {:href "mailto:wintoncentre@maths.cam.ac.uk" :rel "noopener"} "wintoncentre@maths.cam.ac.uk"] "."])

   (hr)
   (faq-item (ttt [:faqs16-q "Where can I find more information on breast cancer?"])
     (ttt [:faqs16-a1 "There is a great deal of information on breast cancer on the web. One of best and most reliable
     sources is Cancer Research UK, along with those from Macmillan and Breast Cancer Care. Their information is written by experts, is up to date and in a style
     that is easy to understand."]))
   ]
  )