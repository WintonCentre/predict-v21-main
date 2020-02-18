(ns predict3.pages.about
  (:require [rum.core :as rum]
            [predict3.layout.header :refer [header header-banner footer footer-banner]]
            [predict3.router :refer [navigate-to iref docroot]]
            [predict3.content :refer [hr]]
            [predict3.content-reader :refer [section all-subsections]]
            [predict3.state.run-time :refer [route]]
            [predict3.results.util :refer [alison-blue-1 alison-blue-2 alison-blue-3 alison-pink]]
            [graphics.simple-icons :refer [icon]]
            [pubsub.feeds :refer [publish]]
            [interop.utils :refer [scrollTo]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            [predict3.pages.faqs :refer [faqs]]
            ))


(defn page-link [route text]
  [:button.btn-link {:on-click #(navigate-to route)}
   [:span {:style {:color     alison-blue-3
                   :font-size 18}} text]]
  )

(defn save-offsets [state]
  ;(println "*save-offsets* args" (:rum/args state))
  (let [[ttt k ref-offsets] (:rum/args state)
        comp (:rum/react-component state)
        dom-node (js/ReactDOM.findDOMNode comp)]
    ;(println "***save-offsets***")
    ;(println "k" k)
    ;(println "***offsets***" (.-offsetTop dom-node))
    (swap! ref-offsets assoc k (.-offsetTop dom-node))
    ;(println "ref-offsets" @ref-offsets)
    state))


(def scroll-to-mixin {:did-mount save-offsets
                      #_#_:did-update save-offsets})

(rum/defcs overview-intro < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#overview (ttt [:about/overview "Overview"])]

   [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
    [:li (page-link [:about {:page :overview :section :whoisitfor}] (ttt [:about/over-menu-1 "Who is it for?"]))]
    [:li (page-link [:about {:page :overview :section :howpredictworks}] (ttt [:about/over-menu-2 "How Predict works"]))]
    [:li (page-link [:about {:page :overview :section :whobuiltpredict}] (ttt [:about/over-menu-3 "Who built Predict"]))]
    [:li (page-link [:about {:page :technical :section :technical}] (ttt [:about/over-menu-4 "Technical"]))]
    [:li {:style {:list-style "none"}}
     [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
      [:li (page-link [:about {:page :technical :section :history}] (ttt [:about/over-menu-5 "Development History"]))]
      #_[:li (page-link [:about {:page :technical :section :preversions}] (ttt [:about/over-menu-6 "Previous Versions"]))]
      [:li (page-link [:about {:page :technical :section :publications}] (ttt [:about/over-menu-7 "Publications"]))]]]
    ]

   [:section
    [:p (ttt [:about/over-0 "Predict is a tool that helps show how breast cancer treatments after surgery might improve survival rates.
    Once details about the patient and their cancer have been entered, the tool will show how different treatments would be expected to improve survival
    rates up to 15 years after diagnosis. This is based on data from similar women in the past. "])
     [:i (ttt [:about/over-0-1 "It is important to note that these treatments have side effects which
    should also be considered when deciding on a treatment."])]]]
   ])

(rum/defcs who-is-it-for < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#whoisitfor (ttt [:about/over-1 "Who is it for?"])]
   [:section
    [:p (ttt [:about/over-1-1-1 "The tool applies to women who have had surgery for early invasive breast cancer and are deciding which other
     treatments to have."])]
    [:p (ttt [:about/over-1-1-2 "The data on which PREDICT is based did not include information on the presence of bilateral disease. The
     performance of the model on women with bilateral breast cancer is not known. The benefits of chemotherapy applied
     in the model were taken from the Early Breast Cancer Trialist Collaborative Group meta-analysis of adjuvant
     chemotherapy trials. A similar benefit is likely to apply to neo-adjuvant chemotherapy. It is not designed for
     women whose cancer has already spread to distant parts of the body at the time it is diagnosed or for women who
     only have non-invasive breast cancer, such as as Ductal Carcinoma In Situ or Lobular Carcinoma In Situ. "])]

    [:p (ttt [:about/over-1-2 "The current version of the tool does not include all the treatments that are currently available. We are
     working to include more in the near future. In the meantime, an appropriate clinician will be able to advise on
     all the treatment options suitable for a particular patient."])]

    [:p (ttt [:about/over-1-3 "Predict only asks for certain information about the cancer. The inputs it asks for are the ones that we
    have enough information about to predict how they affect survival rates. It does not differentiate between the
    types of surgery (for example, mastectomy or lumpectomy) or ask for lifestyle factors
    such as smoking or exercise. These will affect survival, but at the moment we can't say by how much."])]
    (hr)]

   #_[:section
      [:p (ttt [:about/over-1-1 "The tool applies to women who have had surgery for early invasive breast cancer and are
    deciding which other treatments to have. It is not designed for women who have had neo-adjuvant treatments
    (chemotherapy given before surgery) or have already been treated for cancer, for women whose breast cancer is in
    both breasts or has already spread to distant parts of the body at the time it is diagnosed or for women with
    non-invasive breast cancer, such as as Ductal Carcinoma In Situ or Lobular Carcinoma In Situ.  It is also not
    designed for men with breast cancer. "])]

      [:p (ttt [:about/over-1-2 "The current version of the tool does not include all the treatments that are currently
    available. We are working to include more in the near future. In the meantime, an appropriate clinician will be able
    to advise on all the treatment options suitable for a particular patient."])]

      [:p (ttt [:about/over-1-3 "Predict only asks for certain information about the cancer. The inputs it asks for are the ones that we
    have enough information about to predict how they affect survival rates. It does not differentiate between the
    types of surgery (for example, mastectomy or lumpectomy) or ask for lifestyle factors
    such as smoking or exercise. These will affect survival, but at the moment we can't say by how much."])]
      (hr)]])

(rum/defcs how-predict-works < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#howpredictworks (ttt [:about/over-2 "How Predict works"])]
   [:section
    [:p (ttt [:about/over-2-1 "The estimates that Predict produces are based on scientific studies that have been conducted into how effective
     breast cancer treatments are. We know from studies involving many thousands of women that the benefit from treatment
     is affected by the size and type of the cancer at diagnosis, whether the cancer has spread to involve lymph
     nodes, and whether or not the cancer expresses markers such as the oestrogen receptor (ER),
     HER2 and KI67. By analysing the results of these studies, statisticians are able to say
     how these aspects of the cancer are likely to affect average survival and how much benefit might be gained on average from
     different treatment options."])]
    [:p (ttt [:about/over-2-2 "Predict has been tested to make sure that the estimates it produces are as accurate as they can be given current knowledge. Predict was originally
     developed using data from over 5000 women with breast cancer. Its predictions were then tested on data from another
     23,000 women from around the world to make sure that they gave as good an estimate as possible."])]
    [:p (ttt [:about/over-2-3 "Although Predict produces good estimates, it cannot say whether an individual patient will survive their
     cancer or not. It can only provide the average survival rate for people in the past of a similar age and with similar cancers. "])]
    [:p (page-link [:about {:page :technical :section :technical}] (ttt [:about/over-2-4 "The technical section"]))
     (ttt [:about/over-2-5 " has more detail on how Predict was developed and tested."])]
    (hr)
    ]]
  )

(rum/defcs who-built-predict < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#whobuiltpredict (ttt [:about/over-3 "Who built Predict?"])]
   [:section
    [:p (ttt [:about/over3-1 "Development of the model was a collaborative project between the Cambridge Breast Unit, University of
         Cambridge Department of Oncology and the UK's Eastern Cancer Information and Registration Centre (ECRIC)
         (now part of the National Cancer Registration and Analysis Service) and was
         supported by an unrestricted educational grant from Pfizer Limited (the company had no input into the model
         at all)."])]

    [:p (ttt [:about/over3-2 "The website has been built by the "]) [:a {:href  "https://wintoncentre.maths.cam.ac.uk"
                                                                         :rel   "noopener"
                                                                         :style {:text-decoration "underline"}} "Winton Centre for Risk & Evidence Communication"]
     (ttt [:about/over3-3 " at the University of Cambridge who are funded by a generous donation from the David and Claudia Harding Foundation and the Winton Charitable Foundation."])]
    [:p (ttt [:about/over3-4 "Predict has been endorsed by the American Joint Committee on Cancer."])]
    [:img {:src "/assets/AJCC_logo_RGB.png" :alt "American Joint Committee on Cancer"}]
    ]])


(defn overview
  [ttt state]
  [:div
   (overview-intro ttt :overview (::offsets state))
   (who-is-it-for ttt :whoisitfor (::offsets state))
   (how-predict-works ttt :howpredictworks (::offsets state))
   (who-built-predict ttt :whobuiltpredict (::offsets state))
   ])

(rum/defcs technical-intro < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#technical (ttt [:about/technical-header "Technical"])]
   [:section
    [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
     [:li (page-link [:about {:page :technical :section :history}] (ttt [:about/tech-menu-1 "Development History"]))]
     #_[:li (page-link [:about {:page :technical :section :preversions}] (ttt [:about/tech-menu-2 "Previous Versions"]))]
     [:li (page-link [:about {:page :technical :section :publications}] (ttt [:about/tech-menu-3 "Publications"]))]
     [:li (page-link [:about {:page :overview :section :overview}] (ttt [:about/tech-menu-4 "Back to Overview"]))]
     [:li {:style {:list-style "none"}}
      [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
       [:li (page-link [:about {:page :overview :section :whoisitfor}] (ttt [:about/tech-menu-5 "Who is it for?"]))]
       [:li (page-link [:about {:page :overview :section :howpredictworks}] (ttt [:about/tech-menu-6 "How Predict works"]))]
       [:li (page-link [:about {:page :overview :section :whobuiltpredict}] (ttt [:about/tech-menu-7 "Who built Predict"]))]
       [:li (page-link [:about {:page :technical :section :technical}] (ttt [:about/tech-menu-8 "Technical"]))]
       ]]
     ]
    [:p (ttt [:about/tech-0-1 "Predict is an online tool designed to help clinicians and patients make informed
    decisions about treatment following surgery for early invasive breast cancer."])]
    [:p (ttt [:about/tech-0-2 "The model is easy to use: simply enter data for an individual patient including patient age, tumour size,
     tumour grade, number of positive nodes, ER status, HER2 status, KI67 status and mode of detection. Survival
     estimates, with and without adjuvant therapy (chemotherapy, hormone therapy, trastuzumab and bisphosphonates
      for post-menopausal patients) are then presented in visual and text formats. Treatment benefits for
     hormone therapy and chemotherapy are calculated by applying the estimated proportional reductions in the mortality rate from the Early Breast
     Cancer Trialists' Collaborative Group to
     the breast cancer specific mortality. The proportional reduction for hormone therapy is based on 5 years of tamoxifen.
      Predicted mortality reductions are available for both second generation
     (anthracycline-containing, >4 cycles or equivalent) and third generation (taxane-containing) chemotherapy
     regimens."])]
    [:p (ttt [:about/tech-0-3 "The Cambridge Breast Unit (UK) uses the absolute 10-year survival benefit from chemotherapy to guide decision
     making for adjuvant chemotherapy as follows: <3% chemotherapy not recommended; 3-5% chemotherapy discussed as a possible option;
     >5% chemotherapy recommended. "])]
    [:p (ttt [:about/tech-0-4 "Click here to "])
     (page-link [:legal {:page :algorithm} nil] (ttt [:about/tech-0-5 "find out more about the algorithm."]))]]])

(rum/defcs history < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#history (ttt [:about/history-header "Development History"])]
   [:section
    [:p (ttt [:about/hist-1 "The original model (v1.0) was derived from cancer registry information on 5,694 women treated in East Anglia from 1999-2003.
      They were followed until 31 December 2007 so that the median length of follow-up was 5.6 years and the maximum was 8 years
      Breast cancer mortality models for ER positive and ER negative tumours were constructed using Cox proportional
     hazards, adjusted for known prognostic factors and mode of detection (symptomatic versus screen-detected). The
     survival estimates for an individual patient are based on the average co morbidity for women with breast cancer of
     a similar age. Further information about v1.0 is provided in a paper published in "])
     [:a {:href "http://breast-cancer-research.com/content/12/1/R1" :rel "noopener"} "Breast Cancer Research in January 2010."]]]

   [:h2 (ttt [:about/hist-2 "Model validation"])]
   [:p (ttt [:about/hist-2-1 "The clinical validity of a prediction model can be defined as the accuracy of the model to
     predict future events. The two key measures of clinical validity are calibration and discrimination."])]
   [:p (ttt [:about/hist-2-2 "Calibration is how well the model predicts the total number of events in a given data set. A perfectly
     calibrated model is one where the observed (or actual) number of events in a given patient cohort is the same as
     the number of events predicted by the model. Discrimination is how well the model predicts the occurrence of an
     event in individual patients. The discrimination statistic is a number between zero and one. It is generally
     obtained from the area under a receiver-operator characteristic (ROC) curve, which is a plot of the true positive rate (sensitivity) against
      the false positive rate (probability of false alarm)."])]
   [:p (ttt [:about/hist-2-3 "Predict was originally validated using
     a dataset of over 5000 breast cancer patients from the West Midlands Cancer Intelligence Unit also diagnosed during 1999-2003 and followed for a median of 4.8 years."])]
   [:p (ttt [:about/hist-2-4 "We also validated Predict using a dataset from British Columbia that had been previously used for a validation
     of Adjuvant! Online. The British Columbia dataset included women diagnosed with breast cancer 1989-2003 and followed for 10 years.
      Predict v1.0 provided overall and breast cancer specific survival estimates that were at least as
     accurate as estimates from Adjuvant! The results of this validation were published in the "])
    [:a {:href "https://www.ejso.com/article/S0748-7983(11)00051-5/fulltext" :rel "noopener"} "European Journal of Surgical Oncology."]]

   [:h2 (ttt [:about/hist-3 "Model extension: HER2 status (version 1.1)"])]
   [:p (ttt [:about/hist-3-1 "The model was updated in October 2011 to include HER2 status.
     Estimates for the prognostic effect of HER2 status were based on an analysis of 10,179 cases collected by the Breast
     Cancer Association Consortium (BCAC). A validation of the new model in the original British Columbia dataset was published
     in the "])
    [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3425970/" :rel "noopener"} "British Journal of Cancer"] ". "
    (ttt [:about/hist-3-2 "This showed that inclusion of HER2 status in the model improved the estimates of
     breast cancer-specific mortality, especially in HER2 positive patients."])]
   [:p (ttt [:about/hist-3-3 "The benefit of trastuzumab (Herceptin) is based on the estimated proportional reduction of 31 percent in the mortality rate up to five years
     in published trials."])]

   [:h2 (ttt [:about/hist-4 "Model extension: KI67 status (version 1.2)"])]
   [:p (ttt [:about/hist-4-1 "In v1.2, KI67 status was added to the model. The
     prognostic effect of KI67 was taken from published data showing that ER positive tumours that express KI67 are
     associated with a 30 percent poorer relative survival."])]
   [:p (ttt [:about/hist-4-2 "KI67 positivity for the Predict model was defined as
     greater than 10 percent of tumour cells staining positive."])]
   [:p (ttt [:about/hist-4-3 "We have validated the version of Predict that includes KI67 using a data set from Nottingham of 1,274 women diagnosed in 1989-98 and followed for 10 years. The addition of
     KI67 led to a small improvement in calibration and discrimination in 1,274 patients with ER positive disease - the area
     under the ROC curve improved from 0.7611 to 0.7676 (p=0.005). These data were published in "])
    [:a {:href "https://bmccancer.biomedcentral.com/articles/10.1186/1471-2407-14-908" :rel "noopener"} "BMC Cancer"] "."
    ]

   [:h2 (ttt [:about/hist-5 "Model re-fitting (version 2.0)"])]
   [:p (ttt [:about/hist-5-1 "While the overall fit of Predict version 1 was good in multiple independent case
     series, Predict had been shown to underestimate breast cancer specific mortality in women diagnosed under the age
     of 40, particularly those with ER positive disease (See publication in "])
    [:a {:href "https://www.spandidos-publications.com/10.3892/ol.2014.2589" :rel "noopener"} "Oncology Letters"] "
     ). "
    (ttt [:about/hist-5-2 "Another limitation of version 1 was the
     use of discrete categories for tumour size and node status which result in “step” changes in risk estimates on
     moving from one category to the next. For example, a woman with an 18mm or 19mm tumour will be predicted to have
     the same breast cancer specific mortality if all the other prognostic factors are the same whereas breast cancer
     specific morality of women with a 19mm or 20mm tumour will differ. "])]
   [:p (ttt [:about/hist-5-3 "In order to take
     into account age at diagnosis and to smooth out the survival function for tumour size and node status we refitted the Predict
     prognostic model using the original cohort of cases from East Anglia with follow-up extended to 31 December 2012 and including 3,787 women with 10 years of follow-up. The fit of
     the model was tested in the three independent data sets that had also been used to validate the original version
     of Predict."])]
   [:p (ttt [:about/hist-5-4 "Calibration in ER negative disease validation data set: Predict v1.2 over-estimated the number of breast
     cancer deaths by 10 per cent (observed 447 compared to 492 predicted). This over-estimation was most notable in the
     larger tumours and in the high-grade tumours. In contrast, the calibration of Predict v2.0 in ER negative cases was
     excellent (predicted 449). Calibration in ER negative disease validation data set: The calibration of both
     Predict v1.2 and Predict v2.0 was good in ER positive cases (observed breast cancer deaths 633 compared to 643
     (v1.2) and 634 (v2.0) predicted). However, as previously described, Predict v1.2 significantly under-estimated
     breast cancer specific mortality in women diagnosed with ER positive disease at younger ages, whereas the fit of
     Predict v2.0 was good in all age groups."])]


   [:h2 (ttt [:about/hist-6 "Model extension and correction (version 2.1)"])]
   [:h4 (ttt [:about/hist-6-1 "Addition of bisphosphonates treatment option and addition of 15 year outcomes"])]
   [:p (ttt [:about/hist-6-2 "Predict v2.0 used an inaccurate method to estimate the absolute benefit of therapy that resulted in a small
     overestimation of the benefits of treatment.  Benefit is calculated in v2.0 as the difference in breast cancer
     specific mortality with and without treatment but it is more appropriate to estimate benefit as the difference in
     all cause mortality with and without treatment because, if breast cancer mortality is reduced, competing non breast
     cancer mortality will increase slightly.  Consequently, the over estimation of benefit was greater in older women
     with a higher competing mortality from causes other than breast cancer.  The table below shows the predicted
     benefits of anthracycline based chemotherapy (2nd generation) for a woman with a 22mm, grade 2, HER2 negative,
     KI67 negative, clinically detected tumour with 2 positive nodes by age and ER status."])]

   [:table.table.table-bordered.table-responsive {:style {:width 400 :font-size 16}}
    [:thead
     [:tr
      [:th {:row-span 2} (ttt [:about/hist-table-1 "Age"])]
      [:th {:row-span 2} (ttt [:about/hist-table-2 "ER"])]
      [:th {:col-span 2} (ttt [:about/hist-table-3 "Estimated benefit at ten years (%)"])]]
     [:tr
      [:th "v2.0"] [:th "v2.1"]]]
    [:tbody
     [:tr
      [:td 60] [:td "Neg"] [:td 7.0] [:td 6.9]]
     [:tr
      [:td 75] [:td "Neg"] [:td 6.5] [:td 5.5]]
     [:tr
      [:td 60] [:td "Pos"] [:td 3.7] [:td 3.7]]
     [:tr
      [:td 75] [:td "Pos"] [:td 3.6] [:td 3.1]]]]

   [:p (ttt [:about/hist-6-3 "The proportional reduction in the mortality rate following bisphosphonate therapy (18%) was taken from the Early Breast
     Cancer Trialists' Collaborative Group (2015)(3).  This is assumed to be applicable only to post-menopausal women
     (menopausal status is now an input in the tool). It is possible to switch off the option of bisphosphonates in the
     'Settings' tab for institutions who do not offer bisphosphonates as a treatment option."])]

   [:p (ttt [:about/hist-6-4 "We have extended the predictions to 15 years.  While the Eastern Region Cancer Registry data used to derive
     the model included up to fifteen years of survival the data used for model validation only included validation of
     the ten-year mortality predictions. The fifteen-year mortality predictions have not been validated.  We have
     assumed that the treatment benefits of all the treatments persist long term with the same proportional reductions in the mortality rate
     from year 10 to 15 as from diagnosis to year 10.  There is good evidence from some long term follow ups (1, 4, 5) to justify this assumption, but long term follow-up
     data are not available either for trastuzumab therapy or bisphosphonates therapy."])]

   [:h2 (ttt [:about/ee-add-1 "Addition of extended hormone therapy (v2.2)"])]
   [:p (ttt [:about/ee-add-2-1 "Women with ER positive disease who have survived 5 years have an option of stopping their hormone therapy or continuing for another five years. We have added an option for estimating the ten-year outcomes for a woman surviving five years with the benefit of an additional 5 years of hormone therapy – i.e. survival from years 5 to 15 after diagnosis. "])]
   [:p (ttt [:about/ee-add-2-2
             "The evidence for the effect of this comes from the ATLAS and aTTom trials. Over 12,000 breast cancer patients were randomised to 5 or 10 years of tamoxifen in the Atlas study. The relative risk reduction was 0.29 for years 10 to 15 with little effect in years 5 to 10 (relative risk reduction 0.03) [5]. The aTTom study randomised 6,900 women to stop tamoxifen or continue to year 10 (reported only as an abstract). The relative risk reduction in breast cancer specific mortality from year ten on was 0.23. An inverse variance weighted meta-analysis of the two studies gives a relative risk reduction of 0.26 (95% C.I. 0.12 – 0.37"])]
   [:p (ttt [:about/ee-add-3 "We have assumed that the relative benefit of an additional five years hormone therapy is the same in all sub-groups. The Atlas trial investigators do not report on any subgroup analyses based on the patient/tumour characteristics at the time of diagnosis. However, given the absence of interaction for 5-year adjuvant hormone therapy as established by the Early Breast Cancer Trialists' Collaborative Group, 2011 [6], this seems a reasonable assumption for prolonged hormone therapy."])]
   [:p (ttt [:about/ee-add-4 "Note that the Atlas and aTTom trial data were both based on women who had been treated with tamoxifen for the first five years rather than an aromatase inhibitor."])]

   [:h2 (ttt [:about/hist-7 "Future version"])]
   #_[:h4 (ttt [:about/hist-7-1 "Addition of extended hormone therapy, radiotherapy, addition of PR status as an input variable, and presentation of potential harms as well as benefits"])]
   #_[:p (ttt [:about/hist-7-2 "In late 2018 or early 2019 we hope to be able to release a new version of Predict which includes various additions to the algorithm. We hope to introduce
      the effect of progesterone receptor status (PR status) on outcomes, and offer two additional treatment options: radiotherapy, and an additional
      five years of hormone therapy. In the longer term we also hope to be able to display data on the recurrence of disease, showing women how long
      they might be able to expect without their cancer coming back."])]
   [:p (ttt [:about/hist-7-1-1 "We hope to introduce the effect of tumour progesterone receptor expression (PR status) on outcomes, and the benefit of radiotherapy. In the longer term we also hope to be able to display data on the recurrence of disease, showing women how long they might be able to expect without their cancer coming back."])]
   [:p (ttt [:about/hist-7-3 "We also plan to extend the site so as to be able to display a quantification of the potential harms of treatments (i.e. the proportion of
      similar women expected to suffer each potential side effect or adverse event). This will enable the potential harms to be considered alongside the potential benefits
      of each treatment."])]
   ])

#_(rum/defcs preversions < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#preversions (ttt [:about/prev "Links to previous versions of the tool"])]
   [:p [:a {:href (docroot "predict_v2.0.html") :rel "noopener"} "Predict v2.0"]]
   [:p [:a {:href (docroot "predict_v1.2.html") :rel "noopener"} "Predict v1.2"]]
   ])

(rum/defcs publications < scroll-to-mixin
  [state ttt k ref]
  [:div
   [:h2#publications (ttt [:about/pubs "Publications"])]
   [:a {:name "publications"}]
   [:ol
    [:li [:p "PREDICT: a new UK prognostic model that predicts survival following surgery for invasive breast cancer. by
      Wishart GC, Azzato EM, Greenberg DC, Rashbass J, Kearins O, Lawrence G, Caldas C, Pharoah PDP. Breast Cancer Res.
      2010; 12(1): R1. Published online 2010 January 6. doi: 10.1186/bcr2464. PMCID: PMC2880419. "
          [:a {:href "http://breast-cancer-research.com/content/12/1/R1" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "A population-based validation of the prognostic model PREDICT for early breast cancer. by Wishart GC, Bajdik
      CD, Azzato EM, Dicks E, Greenberg DC, Rashbass J, Caldas C, Pharoah PDP. Eur. J. Surg. Oncol. 2011; 37(5): 411-7. "
          [:a {:href "https://www.ejso.com/article/S0748-7983(11)00051-5/fulltext" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "PREDICT Plus: development and validation of a prognostic model for early breast cancer that includes
      HER2. by Wishart GC, Bajdik CD, Dicks E, Provenzano E, Schmidt MK, Sherman M, Greenberg DC, Green AR, Gelmon KA,
      Kosma VM, Olson JE, Beckmann MW, Winqvist R, Cross SS, Severi G, Huntsman D, Pylkas K, Ellis I, Nielsen TO, Giles
      G, Blomqvist C, Fasching PA, Couch FJ, Rakha E, Foulkes WD, Blows FM, Begin LR, Van't Veer LJ, Southey M,
      Nevanlinna H, Mannermaa A, Cox A, Cheang M, Baglietto L, Caldas C, Garcia-Closas M, Pharoah PD. Br. J. Cancer
      2012;107(5):800-7. "
          [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3425970/" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "Inclusion of KI67 significantly improves performance of the PREDICT prognostication and prediction model for
      early breast cancer. by Wishart GC, Rakha E, Green A, et al. BMC Cancer.; 2014;14:908. "
          [:a {:href "https://bmccancer.biomedcentral.com/articles/10.1186/1471-2407-14-908" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "Effect of PREDICT on chemotherapy/trastuzumab recommendations in HER2-positive patients with
      early-stage breast cancer. by SK Down, O Lucas, JR Benson, GC Wishart. Oncol. Lett.; 2014;8(6):2757-2761. "
          [:a {:href "https://www.spandidos-publications.com/10.3892/ol.2014.2589" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "An evaluation of the prognostic model PREDICT using the POSH cohort of women aged 40 years at breast cancer
      diagnosis. by Maishman T, Copson E, Stanton L, et al. Br. J. Cancer.; 2015; Mar 17;112(6):983-91. "
          [:a {:href "https://www.nature.com/articles/bjc201557" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "* Validity of the online PREDICT tool in older patients with breast cancer: a population-based study. by de
      Glas NA, Bastiaannet E, Engels CC, de Craen AJ, Putter H, van de Velde CJ, Hurria A, Liefers GJ, Portielje JE. 
      Br. J. Cancer. ; 2016; 114(4):395-400. "
          [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4815772/" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "* The predictive accuracy of PREDICT: a personalized decision-making tool for Southeast Asian women with
      breast cancer. by Wong HS, Subramaniam S, Alias Z, Taib NA, Ho GF, Ng CH, Yip CH, Verkooijen HM, Hartman M,
      Bhoo-Pathy N. Medicine (Baltimore) ; 2015; 94(8):e593. "
          [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4554151/" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "* Personalized Prognostic Prediction Models for Breast Cancer Recurrence and Survival Incorporating
      Multidimensional Data. by Wu X, Ye Y, Barcenas CH, et al. J Natl Cancer Inst.; 2017;109(7). "
          [:a {:href "https://doi.org/10.1093/jnci/djw314" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "Accuracy of the online prognostication tools PREDICT and Adjuvant! for early-stage breast cancer patients
      younger than 50 years. by Engelhardt EG, van den Broek AJ, Linn SC, et al.Eur J Cancer ; 2017;78:37-44. "
          [:a {:href "https://www.ejcancer.com/article/S0959-8049(17)30833-X/fulltext" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]
    [:li [:p "An updated PREDICT breast cancer prognostication and treatment benefit prediction model with independent
      validation. By Candido Dos Reis FJ, Wishart GC, Dicks EM, et al.Breast Cancer Res. ; 2017;19(1):58. "
          [:a {:href "https://breast-cancer-research.biomedcentral.com/articles/10.1186/s13058-017-0852-3" :rel "noopener"} (ttt [:full-paper-online "[Full paper online]"])]]]]
   [:p (ttt [:about/pubs-footnote "* This work was carried out independently of the Predict development team."])]
   ])

(defn technical
  [ttt state]
  [:div
   (technical-intro ttt :technical (::offsets state))
   (history ttt :history (::offsets state))
   #_(preversions ttt :preversions (::offsets state))
   (publications ttt :publications (::offsets state))])

;;
;; code from here!
;;
(def page-components {:overview  ["overview" "whoisitfor" "howpredictworks" "whobuiltpredict"]
                      :technical ["technical" "history" #_"preversions" "publications"]
                      })

(def scroller
  "Mixin which causes the page to scroll to the scroll-section given in the component arguments.
  The scroll offsets for each section should have been saved in state by the parent component"
  {:did-update (fn [state]
                 ;(println "*scroller* args:" (:rum/args state))
                 (let [[_ [_ {scroll-section :section}]] (:rum/args state)]
                   ;(println "scroller scroll-section:" scroll-section)
                   (scrollTo (if scroll-section (get @(::offsets state) (keyword scroll-section)) 0)))
                 state)})

(rum/defcs about
  "Renders a (text) page given the bide parsed route containing a page, and optionally a section.
  The page should scroll to that section. Those offsets are stored in an atom in our local state."
  < (rum/local {} ::offsets) rum/static scroller
  [state ttt route]
  (let [[_ {page :page scroll-section :section}] route
        page (keyword page)
        scroll-section (keyword scroll-section)]

    ;(println "about page" page "section" scroll-section)

    [:.container-fluid
     (header ttt)
     (header-banner ttt "about-preamble")

     [:#main-content.row {:tab-index -1}
      [:.col-sm-10.col-sm-offset-1.col-lg-8.col-lg-offset-2
       (condp = page
         :about (overview ttt state)
         :overview (overview ttt state)
         :technical (technical ttt state)
         :faqs (faqs ttt)
         )]]

     (editor-modal)
     (footer-banner ttt)
     (footer ttt)]))


(comment
  (def dev-hist-route [:about {:page "technical", :section "history"} nil])
  (def pg (:page (second dev-hist-route)))
  (def ss (:section (second dev-hist-route)))

  (defn ttt [[k s]] s)
  (sectionc ttt)
  (all-subsections ttt "faqs")


  p
  ;=> "technical"

  ss
  ;=> "history"

  )

