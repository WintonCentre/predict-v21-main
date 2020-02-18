(ns predict3.content
  (:require [graphics.simple-icons :refer [icon]]
            [pubsub.feeds :refer [publish]]
            [predict3.router :refer [navigate-to iref docroot]]
            [predict3.components.styles :refer [panel-header-fill]]
            [rum.core :as rum]
            [predict3.results.util :refer [alison-blue-1 alison-blue-2 alison-blue-3]]
            )
  )

;;
;; We'll attempt to keep text content here inside a useful structure - a sort of static database.
;;
;; The structure isn't too important so long as we can process it easily, and it makes some kind of sense.
;; Main idea is to use a collection of clojure nested vectors - i.e. things in square brackets loosely mimicking an
;; HTML document.
;;
;; The first element of the vector should be tagged with an identifier starting with a colon: e.g. :section, :p.
;;
;; Optionally, immediately after the tag, there may be an id, starting with a hash - such as #what-it-does.
;; The code may use this id to index and locate the text. Ids should be unique.
;;
;; Optionally, there may also be a style or position indicator. These start with a '.'. e.g.  .input-box might be used to
;; indicate that the text is closely attached to an input box, whereas '.input-widget' suggests it is relevant to one
;; input in particular. But really, anything that makes sense is fine.
;;
;; It is also possible to pass parameters to the HTML element such as CSS styling. See the examples below.
;;
;; All text should be inside double quotes. Prefer single quotes inside text. If you need a double quote inside text,
;; preface it with a backslash.
;;
;; This file can be edited on github.
;;
;; Example Template:
;; =================
;;
;; [:section#id.class "Sample paragraphs"
;;   [:p "First paragraph."
;;   [:p.emphasise "Second emphasised paragraph."]
;;   [:p "A paragraph with some " [:strong "bold"] "text."]
;;   [:p {:style {:font-size "120%"}} "A paragraph of 120% larger text than normal."]
;;
;; This syntax is known as 'hiccup' format in clojure.
;; See further examples at https://github.com/yokolet/hiccup-samples.
;;

(defn hr []
  [:hr {:style {:height     1
                :color      alison-blue-3
                :background alison-blue-3}}])

#_(defn unknown []
  "A placeholder returning whatever we decide the unknown option should be called"
  [:strong "Unknown"])

#_(rum/defc progress [percent]
  [:.progress {:style {:background-color "#94d3f0"}}
   [:.progress-bar {:role          "progress-bar"
                    :aria-valuenow percent
                    :aria-valuemin 0
                    :aria-valuemax 100
                    :style         {:width            percent
                                    :background-color panel-header-fill}}
    [:span.sr-only (str percent) "% Complete"]]])

#_(defn edit [text] [:span {:style {:color "#f00"}} text])  ;use to flag text that needs attention to authors

#_(defn faq-item
  "Render a faq question (q) and multiple answer paragraphs (as)"
  [q & as]
  [:div [:h3 q]
   (reduce conj [:div] (map (fn [a] [:div {:style {:font-size 16}} a]) as))])

(defn page-link [route text]
  [:button.btn-link {:on-click #(navigate-to route)}
   [:span {:style {:color     alison-blue-3
                   :font-size 18}} text]]
  )

(defn content
  "Text for the site"
  [ttt]
  [
   [:section#not-found "Oops!"
    [:section#oops "Oops!"
     [:p "Try clicking on 'Home' in the navigation bar instead."]]]
   ;
   ; Home page texts
   ;
   [:section#home-what-is "What is Predict?"
    [:p {:key 1}
     (ttt [:home/online-tool "Predict is an online tool that helps patients and clinicians see how different treatments
     for early invasive breast cancer might improve survival rates after surgery."])]
    [:p {:key 2} (ttt [:home/endorsement "It is endorsed by the American Joint Committee on Cancer (AJCC)."])]
    ]

   ;;;
   ;; First of 3 blocks
   ;;;
   [:section#home-how-use (ttt [:home/wdido-title "What does Predict do?"])
    [:p {:key 3} (ttt [:home/wdido-text "Predict asks for some details about the patient and the cancer. It then uses data about the survival
    of similar women in the past to show the likely proportion of such women expected to survive up to fifteen years
    after their surgery with different treatment combinations."])]]

   [:section#home-more-info (ttt [:home/wcifom-title "Where can I find out more?"])
    [:p {:key 5} (ttt [:home/wcifom-text1 "To read more go to "])
     [:a. {:role     "button"
           :on-click #(navigate-to [:about {:page :overview :section :overview}])} [:strong (ttt [:home/wcifom-text2 "About Predict"])]]]]


   [:section#home-how-use-old "How do I use Predict?"
    [:p {:key 3} "The tool asks for some details about the patient and cancer, and will then allow the selection of different possible treatments
     to see how they might affect survival."]
    [:div {:key 2 :style {:border-left  (str "3px solid " panel-header-fill)
                          :padding-left "10px"}}
     [:p "Anyone can use Predict, but we recommend that women considering treatment for early breast cancer use this
     tool in consultation with a medical professional."]]]

   ;;;
   ;; Second of 3 blocks
   ;;;
   [:section#home-what-tell (ttt [:home/wipf-title "Who is Predict for?"])
    [:p {:key 4} (ttt [:home/wipf-text1 "Predict is for clinicians, patients and their families."])]
    [:p {:key 5} [:strong (ttt [:home/wipf-text2 "Patients should use it in consultation with a medical professional."])]]]

   [:section#about-preamble "Preamble"
    [:p.screen-only {:key   0
                     :style {:margin "40px 20px 20px" :font-size "20px"}} "We recommend that patients use this tool in consultation with their
    doctor."]
    [:p.print-only {:key 1} "Predict is a tool that helps show how breast cancer treatments after surgery might improve survival rates.
    This print out shows what characteristics of the patient and the cancer were entered, and then how different treatments would be expected to improve survival
    rates up to 15 years after diagnosis. This is based on data from similar women in the past. Treatments usually have side effects as well as benefits, and it is important to
    consider these as well when making treatment choices. We recommend visiting the sites of charities such as Macmillan and Breast Cancer Now for details about side effects."]]

   #_[:section#overview "Overview"
    [:section "Overview"

     [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
      [:li (page-link [:about {:page :overview :section :whoisitfor}] "Who is it for?")]
      [:li (page-link [:about {:page :overview :section :howpredictworks}] "How Predict works")]
      [:li (page-link [:about {:page :overview :section :whobuiltpredict}] "Who built Predict")]
      [:li (page-link [:about {:page :technical :section :technical}] "Technical")]
      [:li {:style {:list-style "none"}} [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
                                          [:li (page-link [:about {:page :technical :section :history}] "Development History")]
                                          [:li (page-link [:about {:page :technical :section :preversions}] "Previous Versions")]
                                          [:li (page-link [:about {:page :technical :section :publications}] "Publications")]]]
      ]

     [:section
      [:p "Predict is a tool that helps show how breast cancer treatments after surgery might improve survival rates.
    Once details about the patient and their cancer have been entered, the tool will show how different treatments would be expected to improve survival
    rates up to 15 years after diagnosis. This is based on data from similar women in the past. " [:i "It is important to note that these treatments have side effects which
    should also be considered when deciding on a treatment."]]]
     ]]


   #_[:section#whoisitfor "Who is it for?"
    [:section "Who is it for?"
       [:p "The tool applies to women who have had surgery for early invasive breast cancer and are deciding which other treatments to have.
     It is not designed for women who have had neo-adjuvant treatments (chemotherapy given before surgery) or have already been treated for cancer, for women whose breast cancer
      is in both breasts or has already spread to distant parts of the body at the time it is diagnosed or for women with non-invasive breast cancer,
      such as as Ductal Carcinoma In Situ or Lobular Carcinoma In Situ.  It is also not designed for men with breast cancer. "]

       [:p "The current version of the tool does not include all the treatments that are currently available. We are working to include more in the near future. In the meantime, an
      appropriate clinician will be able to advise on all the treatment options suitable for a particular patient."]
     [:p "Predict only asks for certain information about the cancer. The inputs it asks for are the ones that we
    have enough information about to predict how they affect survival rates. It does not differentiate between the
    types of surgery (for example, mastectomy or lumpectomy) or ask for lifestyle factors
    such as smoking or exercise. These will affect survival, but at the moment we can't say by how much."]
     (hr)]]

   #_[:section#howpredictworks "How Predict works"
    [:section "How Predict works"
     [:p "The estimates that Predict produces are based on scientific studies that have been conducted into how effective
     breast cancer treatments are. We know from studies involving many thousands of women that the benefit from treatment
     is affected by the size and type of the cancer at diagnosis, whether the cancer has spread to involve lymph
     nodes, and whether or not the cancer expresses markers such as the oestrogen receptor (ER),
     HER2 and KI67. By analysing the results of these studies, statisticians are able to say
     how these aspects of the cancer are likely to affect average survival and how much benefit might be gained on average from
     different treatment options."]
     [:p "Predict has been tested to make sure that the estimates it produces are as accurate as they can be given current knowledge. Predict was originally
     developed using data from over 5000 women with breast cancer. Its predictions were then tested on data from another
     23,000 women from around the world to make sure that they gave as good an estimate as possible."]
     [:p "Although Predict produces good estimates, it cannot say whether an individual patient will survive their
     cancer or not. It can only provide the average survival rate for people in the past of a similar age and with similar cancers. "]
     [:p (page-link [:about {:page :technical :section :technical}] "The technical section") " has more detail on how Predict was developed and tested."]
     (hr)
     ]
    ]

   #_[:section#whobuiltpredict "Who built Predict?"
    [:section "Who built Predict?"
     [:p "Development of the model was a collaborative project between the Cambridge Breast Unit, University of
         Cambridge Department of Oncology and the UK's Eastern Cancer Information and Registration Centre (ECRIC) (now part of the National Cancer Registration and Analysis Service) and was
         supported by an unrestricted educational grant from Pfizer Limited (the company had no input into the model at all)."]

     [:p "The website has been built by the " [:a {:href  "https://wintoncentre.maths.cam.ac.uk"
                                                   :rel   "noopener"
                                                   :style {:text-decoration "underline"}} "Winton Centre for Risk & Evidence Communication"] "
          at the University of Cambridge who are funded by a generous donation from the David and Claudia Harding Foundation and the Winton Charitable Foundation."]
     [:p "Predict has been endorsed by the American Joint Committee on Cancer."]
     [:img {:src "/assets/AJCC_logo_RGB.png"
            :alt "American Joint Committee on Cancer"}]

     ]]

   #_[:section#technical "Technical"
    [:section "Technical"

     [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
      [:li (page-link [:about {:page :technical :section :history}] "Development History")]
      [:li (page-link [:about {:page :technical :section :preversions}] "Previous Versions")]
      [:li (page-link [:about {:page :technical :section :publications}] "Publications")]
      [:li (page-link [:about {:page :overview :section :overview}] "Back to Overview")]
      [:li {:style {:list-style "none"}} [:ul {:style {:list-style-image "url(/assets/bullet-plus.png)"}}
                                          [:li (page-link [:about {:page :overview :section :whoisitfor}] "Who is it for?")]
                                          [:li (page-link [:about {:page :overview :section :howpredictworks}] "How Predict works")]
                                          [:li (page-link [:about {:page :overview :section :whobuiltpredict}] "Who built Predict")]
                                          [:li (page-link [:about {:page :technical :section :technical}] "Technical")]
                                          ]]
      ]


     [:p "Predict is an online tool designed to help clinicians and patients make informed decisions about treatment following surgery for early invasive breast cancer."]
     [:p "The model is easy to use: simply enter data for an individual patient including patient age, tumour size,
     tumour grade, number of positive nodes, ER status, HER2 status, KI67 status and mode of detection. Survival
     estimates, with and without adjuvant therapy (chemotherapy, hormone therapy, trastuzumab and bisphosphonates 
      for post-menopausal patients) are then presented in visual and text formats. Treatment benefits for
     hormone therapy and chemotherapy are calculated by applying the estimated proportional reductions in the mortality rate from the Early Breast
     Cancer Trialists' Collaborative Group to
     the breast cancer specific mortality. The proportional reduction for hormone therapy is based on 5 years of tamoxifen. 
      Predicted mortality reductions are available for both second generation
     (anthracycline-containing, >4 cycles or equivalent) and third generation (taxane-containing) chemotherapy
     regimens."]
     [:p "The Cambridge Breast Unit (UK) uses the absolute 10-year survival benefit from chemotherapy to guide decision
     making for adjuvant chemotherapy as follows: <3% chemotherapy not recommended; 3-5% chemotherapy discussed as a possible option;
     >5% chemotherapy recommended. "]
     [:p "Click here to " (page-link [:legal {:page :algorithm} nil] "find out more about the algorithm.")]
     ]]

   #_[:section#history "Development History"
    [:section "Development History"
     [:p "The original model (v1.0) was derived from cancer registry information on 5,694 women treated in East Anglia from 1999-2003.
      They were followed until 31 December 2007 so that the median length of follow-up was 5.6 years and the maximum was 8 years
      Breast cancer mortality models for ER positive and ER negative tumours were constructed using Cox proportional
     hazards, adjusted for known prognostic factors and mode of detection (symptomatic versus screen-detected). The
     survival estimates for an individual patient are based on the average co morbidity for women with breast cancer of
     a similar age. Further information about v1.0 is provided in a paper published in "
      [:a {:href "http://breast-cancer-research.com/content/12/1/R1" :rel "noopener"} "Breast Cancer Research in January 2010."]]]
    [:section "Model validation"
     [:p "The clinical validity of a prediction model can be defined as the accuracy of the model to
     predict future events. The two key measures of clinical validity are calibration and discrimination."]
     [:p "Calibration is how well the model predicts the total number of events in a given data set. A perfectly
     calibrated model is one where the observed (or actual) number of events in a given patient cohort is the same as
     the number of events predicted by the model. Discrimination is how well the model predicts the occurrence of an
     event in individual patients. The discrimination statistic is a number between zero and one. It is generally
     obtained from the area under a receiver-operator characteristic (ROC) curve, which is a plot of the true positive rate (sensitivity) against
      the false positive rate (probability of false alarm)."]
     [:p "Predict was originally validated using
     a dataset of over 5000 breast cancer patients from the West Midlands Cancer Intelligence Unit also diagnosed during 1999-2003 and followed for a median of 4.8 years."]
     [:p "We also validated Predict using a dataset from British Columbia that had been previously used for a validation
     of Adjuvant! Online. The British Columbia dataset included women diagnosed with breast cancer 1989-2003 and followed for 10 years.
      Predict v1.0 provided overall and breast cancer specific survival estimates that were at least as
     accurate as estimates from Adjuvant! The results of this validation were published in the "
      [:a {:href "https://www.ejso.com/article/S0748-7983(11)00051-5/fulltext" :rel "noopener"} "European Journal of Surgical Oncology."]
      ]]
    [:section "Model extension: HER2 status (version 1.1)"
     [:p "The model was updated in October 2011 to include HER2 status.
     Estimates for the prognostic effect of HER2 status were based on an analysis of 10,179 cases collected by the Breast
     Cancer Association Consortium (BCAC). A validation of the new model in the original British Columbia dataset was published
     in the "
      [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3425970/" :rel "noopener"} "British Journal of Cancer"] "
      . This showed that inclusion of HER2 status in the model improved the estimates of
     breast cancer-specific mortality, especially in HER2 positive patients."]
     [:p "The benefit of trastuzumab (Herceptin) is based on the estimated proportional reduction of 31 percent in the mortality rate up to five years
     in published trials."]]
    [:section "Model extension: KI67 status (version 1.2)"
     [:p "In v1.2, KI67 status was added to the model. The
     prognostic effect of KI67 was taken from published data showing that ER positive tumours that express KI67 are
     associated with a 30 percent poorer relative survival."]
     [:p "KI67 positivity for the Predict model was defined as
     greater than 10 percent of tumour cells staining positive."]
     [:p "We have validated the version of Predict that includes KI67 using a data set from Nottingham of 1,274 women diagnosed in 1989-98 and followed for 10 years. The addition of
     KI67 led to a small improvement in calibration and discrimination in 1,274 patients with ER positive disease - the area
     under the ROC curve improved from 0.7611 to 0.7676 (p=0.005). These data were published in "
      [:a {:href "https://bmccancer.biomedcentral.com/articles/10.1186/1471-2407-14-908" :rel "noopener"} "BMC Cancer"] "."
      ]]
    [:section "Model re-fitting (version 2.0)"
     [:p "While the overall fit of Predict version 1 was good in multiple independent case
     series, Predict had been shown to underestimate breast cancer specific mortality in women diagnosed under the age
     of 40, particularly those with ER positive disease (See publication in "
      [:a {:href "https://www.spandidos-publications.com/10.3892/ol.2014.2589" :rel "noopener"} "Oncology Letters"] "
     ). Another limitation of version 1 was the
     use of discrete categories for tumour size and node status which result in “step” changes in risk estimates on
     moving from one category to the next. For example, a woman with an 18mm or 19mm tumour will be predicted to have
     the same breast cancer specific mortality if all the other prognostic factors are the same whereas breast cancer
     specific morality of women with a 19mm or 20mm tumour will differ. "]
     [:p "In order to take
     into account age at diagnosis and to smooth out the survival function for tumour size and node status we refitted the Predict
     prognostic model using the original cohort of cases from East Anglia with follow-up extended to 31 December 2012 and including 3,787 women with 10 years of follow-up. The fit of
     the model was tested in the three independent data sets that had also been used to validate the original version
     of Predict."]
     [:p "Calibration in ER negative disease validation data set: Predict v1.2 over-estimated the number of breast
     cancer deaths by 10 per cent (observed 447 compared to 492 predicted). This over-estimation was most notable in the
     larger tumours and in the high-grade tumours. In contrast, the calibration of Predict v2.0 in ER negative cases was
     excellent (predicted 449). Calibration in ER negative disease validation data set: The calibration of both
     Predict v1.2 and Predict v2.0 was good in ER positive cases (observed breast cancer deaths 633 compared to 643
     (v1.2) and 634 (v2.0) predicted). However, as previously described, Predict v1.2 significantly under-estimated
     breast cancer specific mortality in women diagnosed with ER positive disease at younger ages, whereas the fit of
     Predict v2.0 was good in all age groups."]]


    [:section "Model extension and correction (version 2.1)"
     [:h4 "Addition of bisphosphonates treatment option and addition of 15 year outcomes"]
     [:p "Predict v2.0 used an inaccurate method to estimate the absolute benefit of therapy that resulted in a small
     overestimation of the benefits of treatment.  Benefit is calculated in v2.0 as the difference in breast cancer
     specific mortality with and without treatment but it is more appropriate to estimate benefit as the difference in
     all cause mortality with and without treatment because, if breast cancer mortality is reduced, competing non breast
     cancer mortality will increase slightly.  Consequently, the over estimation of benefit was greater in older women
     with a higher competing mortality from causes other than breast cancer.  The table below shows the predicted
     benefits of anthracycline based chemotherapy (2nd generation) for a woman with a 22mm, grade 2, HER2 negative,
     KI67 negative, clinically detected tumour with 2 positive nodes by age and ER status."]

     [:table.table.table-bordered.table-responsive {:style {:width 400 :font-size 16}}
      [:thead
       [:tr
        [:th {:row-span 2} "Age"] [:th {:row-span 2} "ER"] [:th {:col-span 2} "Estimated benefit at ten years (%)"]]
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

     [:p "The proportional reduction in the mortality rate following bisphosphonate therapy (18%) was taken from the Early Breast
     Cancer Trialists' Collaborative Group (2015)(3).  This is assumed to be applicable only to post-menopausal women
     (menopausal status is now an input in the tool). It is possible to switch off the option of bisphosphonates in the
     'Settings' tab for institutions who do not offer bisphosphonates as a treatment option."]

     [:p "We have extended the predictions to 15 years.  While the Eastern Region Cancer Registry data used to derive
     the model included up to fifteen years of survival the data used for model validation only included validation of
     the ten-year mortality predictions. The fifteen-year mortality predictions have not been validated.  We have
     assumed that the treatment benefits of all the treatments persist long term with the same proportional reductions in the mortality rate
     from year 10 to 15 as from diagnosis to year 10.  There is good evidence from some long term follow ups (1, 4, 5) to justify this assumption, but long term follow-up
     data are not available either for trastuzumab therapy or bisphosphonates therapy."]]

    [:section "Future version"
     [:h4 "Addition of extended hormone therapy, radiotherapy, addition of PR status as an input variable, and presentation of potential harms as well as benefits"]
     [:p "In late 2018 or early 2019 we hope to be able to release a new version of Predict which includes various additions to the algorithm. We hope to introduce
      the effect of progesterone receptor status (PR status) on outcomes, and offer two additional treatment options: radiotherapy, and an additional
      five years of hormone therapy. In the longer term we also hope to be able to display data on the recurrence of disease, showing women how long
      they might be able to expect without their cancer coming back."]
     [:p "We also plan to extend the site so as to be able to display a quantification of the potential harms of treatments (i.e. the proportion of
      similar women expected to suffer each potential side effect or adverse event). This will enable the potential harms to be considered alongside the potential benefits
      of each treatment."]

     ]]


   #_[:section#preversions "Previous versions"
    [:section "Links to previous versions of the tool"
     [:p [:a {:href (docroot "predict_v2.0.html") :rel "noopener"} "Predict v2.0"]]
     [:p [:a {:href (docroot "predict_v1.2.html") :rel "noopener"} "Predict v1.2"]]
     ]
    ]

   #_[:section#publications "Publications"
    [:section "Publications"
     [:a {:name "publications"}]
     [:ol
      [:li [:p "PREDICT: a new UK prognostic model that predicts survival following surgery for invasive breast cancer. by
      Wishart GC, Azzato EM, Greenberg DC, Rashbass J, Kearins O, Lawrence G, Caldas C, Pharoah PDP. Breast Cancer Res.
      2010; 12(1): R1. Published online 2010 January 6. doi: 10.1186/bcr2464. PMCID: PMC2880419. "
            [:a {:href "http://breast-cancer-research.com/content/12/1/R1" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "A population-based validation of the prognostic model PREDICT for early breast cancer. by Wishart GC, Bajdik
      CD, Azzato EM, Dicks E, Greenberg DC, Rashbass J, Caldas C, Pharoah PDP. Eur. J. Surg. Oncol. 2011; 37(5): 411-7. "
            [:a {:href "https://www.ejso.com/article/S0748-7983(11)00051-5/fulltext" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "PREDICT Plus: development and validation of a prognostic model for early breast cancer that includes
      HER2. by Wishart GC, Bajdik CD, Dicks E, Provenzano E, Schmidt MK, Sherman M, Greenberg DC, Green AR, Gelmon KA,
      Kosma VM, Olson JE, Beckmann MW, Winqvist R, Cross SS, Severi G, Huntsman D, Pylkas K, Ellis I, Nielsen TO, Giles
      G, Blomqvist C, Fasching PA, Couch FJ, Rakha E, Foulkes WD, Blows FM, Begin LR, Van't Veer LJ, Southey M,
      Nevanlinna H, Mannermaa A, Cox A, Cheang M, Baglietto L, Caldas C, Garcia-Closas M, Pharoah PD. Br. J. Cancer
      2012;107(5):800-7. "
            [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3425970/" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "Inclusion of KI67 significantly improves performance of the PREDICT prognostication and prediction model for
      early breast cancer. by Wishart GC, Rakha E, Green A, et al. BMC Cancer.; 2014;14:908. "
            [:a {:href "https://bmccancer.biomedcentral.com/articles/10.1186/1471-2407-14-908" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "Effect of PREDICT on chemotherapy/trastuzumab recommendations in HER2-positive patients with
      early-stage breast cancer. by SK Down, O Lucas, JR Benson, GC Wishart. Oncol. Lett.; 2014;8(6):2757-2761. "
            [:a {:href "https://www.spandidos-publications.com/10.3892/ol.2014.2589" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "An evaluation of the prognostic model PREDICT using the POSH cohort of women aged 40 years at breast cancer
      diagnosis. by Maishman T, Copson E, Stanton L, et al. Br. J. Cancer.; 2015; Mar 17;112(6):983-91. "
            [:a {:href "https://www.nature.com/articles/bjc201557" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "* Validity of the online PREDICT tool in older patients with breast cancer: a population-based study. by de
      Glas NA, Bastiaannet E, Engels CC, de Craen AJ, Putter H, van de Velde CJ, Hurria A, Liefers GJ, Portielje JE. 
      Br. J. Cancer. ; 2016; 114(4):395-400. "
            [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4815772/" :rel "noopener"} "[Full paper online}"]]]
      [:li [:p "* The predictive accuracy of PREDICT: a personalized decision-making tool for Southeast Asian women with
      breast cancer. by Wong HS, Subramaniam S, Alias Z, Taib NA, Ho GF, Ng CH, Yip CH, Verkooijen HM, Hartman M,
      Bhoo-Pathy N. Medicine (Baltimore) ; 2015; 94(8):e593. "
            [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4554151/" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "* Personalized Prognostic Prediction Models for Breast Cancer Recurrence and Survival Incorporating
      Multidimensional Data. by Wu X, Ye Y, Barcenas CH, et al. J Natl Cancer Inst.; 2017;109(7). "
            [:a {:href "https://doi.org/10.1093/jnci/djw314" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "Accuracy of the online prognostication tools PREDICT and Adjuvant! for early-stage breast cancer patients
      younger than 50 years. by Engelhardt EG, van den Broek AJ, Linn SC, et al.Eur J Cancer ; 2017;78:37-44. "
            [:a {:href "https://www.ejcancer.com/article/S0959-8049(17)30833-X/fulltext" :rel "noopener"} "[Full paper online]"]]]
      [:li [:p "An updated PREDICT breast cancer prognostication and treatment benefit prediction model with independent
      validation. By Candido Dos Reis FJ, Wishart GC, Dicks EM, et al.Breast Cancer Res. ; 2017;19(1):58. "
            [:a {:href "https://breast-cancer-research.biomedcentral.com/articles/10.1186/s13058-017-0852-3" :rel "noopener"} "[Full paper online]"]]]]
     [:p "* This work was carried out independently of the Predict development team."]]]


   #_[:section#faqs "FAQS"
    [:section "FAQs"

     (faq-item "Looking for advice?"
               [:span {:style {:font-size 18}} "Information about treatments:"]
               [:ul {:style {:font-size 16 :list-style-type "none"}}
                [:li {:key 1}
                 [:a {:href "http://www.cancerresearchuk.org/about-cancer/breast-cancer/treatment" :rel "noopener" :target "blank "}
                  "Cancer research UK "]]
                [:li {:key 2}
                 [:a {:href "https://www.nhs.uk/conditions/breast-cancer/treatment/" :rel "noopener" :target "blank"} "NHS"]]]
               [:span {:style {:font-size 18}} "More information about side effects: "]
               [:ul {:style {:font-size 16 :list-style-type "none"}}
                [:li {:key 3}
                 [:a {:href   "https://www.breastcancercare.org.uk/information-support/facing-breast-cancer/going-through-treatment-breast-cancer/side-effects"
                      :rel    "noopener"
                      :target "blank"} "Breast Cancer Care"]]
                [:li {:key 2}
                 [:a {:href "https://www.macmillan.org.uk/information-and-support/breast-cancer/coping/side-effects-and-symptoms" :rel "noopener" :target "blank"} "Macmillan"]]
                #_[:li {:key 0} \n
                   [:a {:href "https://www.nhs.uk/pages/home.aspx" :target "_blank"} "NHS Choices"]]]
               [:span {:style {:font-size 18}} "Sources of advice and support: "]
               [:ul {:style {:font-size 16 :list-style-type "none"}}
                [:li {:key 3}
                 [:a {:href "https://www.breastcancercare.org.uk/information-support/support-you/someone-talk" :rel "noopener" :target "blank "} "Breast Cancer Care "]]
                [:li {:key 0}
                 [:a {:href "https://www.nhs.uk/conditions/breast-cancer/treatment/#psychological-help" :rel "noopener" :target "_blank "} "NHS "]]
                [:li {:key 1}
                 [:a {:href " http://www.healthtalk.org/peoples-experiences/cancer/breast-cancer-women/topics" :rel "noopener" :target "_blank "} "Health Talk - videos of women's experiences with breast cancer and treatment options "]]])


     (hr)
     (faq-item "What if I don’t have all the details needed for the input section?"
               "If you select 'Unknown' for an input, the Predict tool will use the average value. This will simply make the results less personalised.")
     (hr)
     (faq-item "How do I know that Predict gives the right answers?"
               "Predict estimates what would be expected to happen to women with similar characteristics based on past
               data. The findings are based on women treated in the East of England but we have also tested that they
               give the same results on nearly 5,500 women treated in the West Midlands and a large database of women
               diagnosed under 40 in Nottingham. To the best of our knowledge
               the Predict tool works equally well for all women in the UK. We have also tested Predict on over 3,000
               women treated in British Columbia, Canada and a large group of women from the Netherlands. Other groups
               have also validated Predict using patient groups from the Netherlands and Malaysia. Five scientific papers
               describing the work have been reviewed by scientists and clinicians (see Publications for details).
               But Predict can never say what will happen to an individual woman.")
     (hr)
     (faq-item "If the data used is from patients decades ago won't the predictions it gives be out of date?"
               "These predictions are based on patients diagnosed between 1999 and 2004, and include follow-up for up to 15 years.
               In order to carry out long term predictions older data have to be used.  It is possible that outcomes of these treatments will be different
               in patients diagnosed today - the use of older data is likely to slightly overestimate the benefit
               of treatment.")
     (hr)
     (faq-item "What use are these kinds of statistics when as a patient I will either be cured or not?"
               "Medical treatments don't work for everyone - whilst some people may get a huge benefit, others may get no benefit - only the harmful side effects.
               This makes choosing whether to try a treatment a difficult and personal choice. For cancer, treatments may be able to delay
               a cancer coming back or stop it coming back at all. From statistics, based on what has happened to people with similar cancers in the past
               when they tried a treatment, Predict tries to give the 'best guess' at the sort of benefits that a treatment option might give a particular patient.
               This can help inform a personal decision on whether to try it or not. Any potential benefits, though, should always be weighed against the possible harms of the side effects.")
     (hr)
     (faq-item "What about radiotherapy?"
               "For some women, radiotherapy is a potential treatment option. We plan to include
               it in the next version of the model.")
     (hr)
     (faq-item "What about other treatments?"
               "Many new types of treatment for breast cancer are being researched.  However, we are not able to include new treatments
               in Predict until large clinical trials have demonstrated the size of the benefit in breast cancer patients.")
     (hr)
     (faq-item "What about neo-adjuvant treatment?"
               "Sometimes chemotherapy is advised before initial surgery ('neo-adjuvant' chemotherapy).  The current version of Predict is not designed to be used under these circumstances.")
     (hr)
     (faq-item "What about men with breast cancer?"
               "Predict has only been designed and tested with data from women and should not be used to make predictions for men with breast cancer.")
     (hr)
     (faq-item "What about metastatic cancer?"
               "Predict is only relevant for women with early breast cancer. It does not make predictions for women whose breast cancer is already metastatic when diagnosed.")
     (hr)
     (faq-item "What about DCIS or LCIS?"
               "The calculations in Predict are only for women who have invasive breast cancer. These are not for use by women with DCIS (Ductal carcinoma in situ) or
               LCIS (Lobular carcinoma in situ).")
     (hr)
     (faq-item "Does Predict account for different types of surgery?"
               "Predict is designed to be used for helping make decisions about treatment after the initial surgery. There is not currently enough
               data available to be able to take into account the effects of the different types of surgery on outcomes.")
     (hr)
     (faq-item "What about side effects?"
               "At the moment Predict only gives information about the benefits of each treatment, but every
               treatment also has the potential to cause side effects and it is important to weigh these up when considering
               treatment options. Charities such as Breast Cancer Care and Macmillan give good information on the side
               effects of each treatment. We plan to
               include an estimate of the likelihood of different side effects from each treatment in the next version of Predict.")
     (hr)
     (faq-item "Who developed the Predict programme?"
               "Development of the model was a collaborative project between the Cambridge Breast Unit, University of
                Cambridge Department of Oncology and the Eastern Cancer Information and Registration Centre (ECRIC) and was
                supported by an unrestricted educational grant from Pfizer Limited.
                They welcome any feedback you may have about Predict. If you have questions about its development or there are
                features you would like to have added to the model please let them know by emailing "
               [:a {:href "mailto:info@predict.nhs.uk" :rel "noopener"} "info@predict.nhs.uk."])
     (hr)
     (faq-item "How was the computer programme developed?"
               [:span "The team used information held by the Eastern Cancer Registry
                 and Information Centre, now part of the 
                " [:a {:href "http://www.ncin.org.uk/collecting_and_using_data/national_cancer_data_repository/" :rel "noopener" :style {:text-decoration "underline"}} "National Cancer Registration and Analysis Service"] " on nearly 5700 women treated for breast cancer between 1999 and 2003.
                 Using this information they were able to see how individual factors affected survival at five years and
                 ten years."])
     (hr)
     (faq-item "Who designed the website?"
               [:span "The website has been built by the "
                [:a {:href "https://wintoncentre.maths.cam.ac.uk" :rel "noopener" :style {:text-decoration "underline"}} "Winton Centre for Risk & Evidence Communication"] "
                 at the University of Cambridge. The site functionality and visualisation software is trademarked by the Winton Centre as
                 4U2C. However, we are happy for others to use it for similar purposes. Do contact us to discuss this at "
                [:a {:href "mailto:wintoncentre@maths.cam.ac.uk" :rel "noopener"} "wintoncentre@maths.cam.ac.uk"] "."])

     (hr)
     (faq-item "Where can I find more information on breast cancer?"
               "There is a great deal of information on breast cancer on the web. One of best and most reliable
               sources is Cancer Research UK, along with those from Macmillan and Breast Cancer Care. Their information is written by experts, is up to date and in a style
               that is easy to understand.")
     ]]

   [:section#contact-preamble "Preamble"
    [:p.screen-only {:key   0
                     :style {:margin "40px 20px 20px" :font-size "20px"}} [:contact/preamble "We recommend that patients use this tool in consultation with their
    doctor."]]
    [:p.print-only {:key 1} [:print/contact-preamble "Predict is a tool that helps show how breast cancer treatments after surgery might improve survival rates.
    This print out shows what characteristics of the patient and the cancer were entered, and then how different treatments would be expected to improve survival
    rates up to 15 years after diagnosis. This is based on data from similar women in the past. Treatments usually have side effects as well as benefits, and it is important to
    consider these as well when making treatment choices. We recommend visiting the sites of charities such as Macmillan and Breast Cancer Now for details about side effects."]]]

   [:section#contact (ttt [:contact/title "Contact"])
    [:section (ttt [:contact/title "Contact"])
     [:p "National Cancer Registration and Analysis Service" [:br] "East Regional Office, Victoria House, Capital Park,
     Fulbourn, Cambridge CB21 5XB" [:br] (ttt [:contact/email "Email: "]) [:a {:href "mailto:info@predict.nhs.uk" :rel "noopener"} "info@predict.nhs.uk"]]]]

   [:section#legal-preamble "Preamble"
    [:p.screen-only {:key   0
                     :style {:margin "40px 20px 20px" :font-size "20px"}} "We recommend that patients use this tool in consultation with their
    doctor."]
    [:p.print-only {:key 1} "Predict is a tool that helps show how breast cancer treatments after surgery might improve survival rates.
    This print out shows what characteristics of the patient and the cancer were entered, and then how different treatments would be expected to improve survival
    rates up to 15 years after diagnosis. This is based on data from similar women in the past. Treatments usually have side effects as well as benefits, and it is important to
    consider these as well when making treatment choices. We recommend visiting the sites of charities such as Macmillan and Breast Cancer Now for details about side effects."]]

   [:section#advisoryboard "The Advisory Board"
    [:section "The Advisory Board"
     [:p "The Independent Expert Advisory Group is a panel of named experts prepared to comment on the choice of the specific
    methods and clinical implications of the information provided. The group includes clinicians, statisticians and patients."]
     [:p "Currently the advisory group consists of:"]
     [:ul
      [:li "Angela Wood"]
      [:li "David Dodwell"]
      [:li "Gary Collins"]
      [:li "Frank Harrell"]
      [:li "Cliona Kirwan"]
      [:li "Sanjeev Madaan"]
      [:li "Willi Sauerbrei"]
      [:li "Andreas Makris"]
      [:li "Richard Riley"]
      [:li "Rob Stein"]
      [:li "Alfred Oliver"]
      ]]]

   [:section#disclaimer "Disclaimer"
    [:section "Disclaimer"
     [:p "Predict uses an algorithm based on information from many thousands of women diagnosed in England and large randomised
   controlled trials of different treatment options. However, it can only provide a 'best guess' of likely outcomes based on 
   current knowledge, and it can never provide an accurate prediction for an individual. Patients should always consult their 
   own specialist, who will be able to discuss the results in a more personalised context."]
     [:p "The theory behind the model has been subject to open academic peer review in "
      [:a {:href "http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2880419/?tool=pubmed" :rel "noopener"} "Breast Cancer Research"] ", the 
     " [:a {:href "http://www.ncbi.nlm.nih.gov/pubmed/21371853" :rel "noopener"} "European Journal of Surgical Oncology"] ", the 
     " [:a {:href "http://www.nature.com/bjc/journal/v107/n5/full/bjc2012338a.html" :rel "noopener"} "British Journal of Cancer "] "and 
     " [:a {:href "http://bmccancer.biomedcentral.com/articles/10.1186/1471-2407-14-908" :rel "noopener"} "BMC Cancer."]
      " The model has been validated
     on multiple independent breast cancer cohorts with over 23,000 women diagnosed with breast cancer from England,
     the Netherlands, Canada and Malaysia with the results published in open academic peer review journals. Every
     effort has been made to ensure that the data used are accurate, the statistical procedures sound and the computer
     algorithm robust."]]]

   #_[:section#algorithm "Algorithm"
    [:section "Explanation of the Predict Algorithm"


     [:p "The model is based on a precise mathematical form for the cumulative hazard function: this specifies an
      individual’s chance of dying in any period of time following surgery from breast cancer, assuming they do not
      die of some other cause.  The model also contains a cumulative hazard function for dying of other causes,
      assuming no deaths from breast cancer.  These two process are assumed to be independent and together comprise
      a competing risks model, where the overall chance of being alive at a certain number of years following
      surgery is given by the chance of neither of these two events having occurred."]

     [:p "Details of the form for the baseline cumulative hazards are given in the "
      [:a {:href (iref "/predict-mathematics.pdf") :rel "noopener"} [:i.fa.fa-file-pdf-o {:aria-hidden true}] " mathematical description"] "."]

     [:p "For deaths from breast cancer, Predict uses a proportional hazards model in which each risk factor and treatment
      multiplies the baseline cumulative hazard by a fixed amount known as the hazard ratio or relative risk -
      essentially the proportional change in annual mortality risk.  This means the cumulative hazard is the product
      of three components: the baseline hazard (chances of dying from something other than breast cancer), the hazard ratios 
      for the risk factors (the increased risk of death due to breast cancer) and the hazard ratios for
      the treatments (the decreased risk thanks to the treatments)."]

     [:.table-responsive
      [:table.table.table-bordered {:style {:max-width 600}}
       [:caption {:style {:color "#686868"}}
        "Table 1: Risk-factor coefficients for Breast Cancer mortality in ER+ patients (numbers rounded for table)"]
       [:thead
        [:tr
         [:th "Risk Factor"]
         [:th "Logarithm of multiplier of baseline hazard"]]]
       [:tbody
        [:tr
         [:th "Age at surgery (years)"]
         [:td "34.5((" [:i "age"] "/10)" [:sup "-2"] " -0.0288)   - 34.2((" [:i "age"] "/10)" [:sup "-2"] "ln(" [:i "age"] "/10) - .051)"]]
        [:tr
         [:th "Size of tumour (mm)"]
         [:td "+ 0.75 (ln (" [:i "size"] "/100)  +1.55)"]]
        [:tr
         [:th "Number of nodes"]
         [:td "+ 0.71 (ln((" [:i "nodes"] "+1)/10)   +1.39)"]]
        [:tr
         [:th "Grade (1,2,3)"]
         [:td "+ 0.75 " [:i "grade"]]]
        [:tr
         [:th "Screen detected (0,1)"]
         [:td "- 0.228"]]
        [:tr
         [:th "Her2 (0,1)"]
         [:td "-0.076 if " [:i "her2"] " = 0" [:br] "0.241 if " [:i "her2"] " = 1"]]
        [:tr
         [:th "Ki67 (0,1)"]
         [:td "-0.113  if " [:i "ki67"] " = 0" [:br] "0.149 if " [:i "ki67"] " = 1"]]
        ]]]

     [:.table-responsive
      [:table.table.table-bordered {:style {:max-width 600}}
       [:caption {:style {:color "#686868"}}
        "Table 2: Risk-factor coefficients for Breast Cancer mortality in ER- patients (numbers rounded for table)"]
       [:thead
        [:tr
         [:th "Risk Factor"]
         [:th "Logarithm of multiplier of baseline hazard"]]]
       [:tbody
        [:tr
         [:th "Age at surgery (years)"]
         [:td "0.0089 (" [:i "age"] " - 56.3)"]]
        [:tr
         [:th "Size of tumour (mm)"]
         [:td "+ 2.09(√(" [:i "size"] "/100) -0.509)"]]
        [:tr
         [:th "Number of nodes"]
         [:td "+ .626  (ln((" [:i "nodes"] " + 1)/10) + 1.09)"]]
        [:tr
         [:th "Grade (1,2,3)"]
         [:td "+ 1.13    if " [:i "grade"] " = 2 or 3"]]
        [:tr
         [:th "Her2 (0,1)"]
         [:td "-0.076 if " [:i "her2"] " = 0" [:br] "0.241 if " [:i "her2"] " = 1"]]
        ]]]

     [:.table-responsive
      [:table.table.table-bordered {:style {:max-width 600}}
       [:caption {:style {:color "#686868"}}
        "Table 3: Treatment Risk-factor coefficients"]
       [:thead
        [:tr
         [:th "Treatment"]
         [:th "log(" [:i "RR"] ")"]
         [:th "approx" [:br] "se of" [:br] "log(" [:i "RR"] ")"]
         [:th "Hazard ratio" [:br] "Relative risk"]
         [:th "Source"]]
        ]
       [:tbody
        [:tr
         [:th "hormone therapy up to 10 years (if ER+)  "]
         [:td "-0.386"]
         [:td "0.08"]
         [:td "0.68"]
         [:td "Early Breast Cancer Trialists' Collaborative Group (2011) p777"]]
        [:tr
         [:th "trastuzumab (if HER2+)"]
         [:td "-0.357"]
         [:td "0.08"]
         [:td "0.70"]
         [:td "unpublished meta-analysis of 4 large randomised trials"]]
        [:tr
         [:th "Bisphosphonates (if post-menopausal)"]
         [:td "-0.198"]
         [:td "0.06"]
         [:td "0.82"]
         [:td "Early Breast Cancer Trialists' Collaborative Group (2015)"]]
        [:tr
         [:th "2" [:sup "nd"] " gen chemotherapy"]
         [:td "-0.248"]
         [:td "0.12"]
         [:td "0.78"]
         [:td "Early Breast Cancer Trialists' Collaborative Group (2012)"]]
        [:tr
         [:th "3" [:sup "rd"] " gen chemotherapy   "]
         [:td "-0.446"]
         [:td "0.13"]
         [:td "0.64"]
         [:td "Early Breast Cancer Trialists' Collaborative Group (2012)"]]
        ]]]
     ]

    [:section "Implementation of the Algorithm"
     [:p "The model used to drive this tool is a clojurescript implementation of the Predictv2.1 model written in R maintained by Professor Paul Pharoah. The full implementation is available in a collection of " [:a {:href "https://github.com/WintonCentre/predict-v21-main" :rel "noopener"} " open source repositories on GitHub."]]]]

   #_[:section#privacy "Privacy"
    [:section "Site Privacy"
     [:p "No information entered in the Predict Tool page ever leaves your local machine. The anonymous data collected
     is used solely to provide inputs to a statistical model that runs inside your browser for the purpose of driving the result displays. Once you shut down the
     browser, or press reset, or reload the page, or reuse the browser window to load another web site, that information is destroyed."]]

    [:section "Cookie Policy"
     [:p "A cookie is a small amount of data sent to your computer that your web browser stores when you visit some
     websites. Predict uses cookies for the purpose of improving our understanding of how the web site is used so we
     can make it better."]
     #_[:p "The law on website cookies changed on 26 May 2011 and requires that sites state which cookies are being used
     and their purpose. It also makes clear that cookies only be used with your consent. You can find out more
     information about this law by visiting the " [:a {:href "https://ico.org.uk/for-the-public/online/cookies/" :rel "noopener"} "Information Commissioner’s Office website."]]
     [:p "If you allow cookies, Predict will send anonymous tracking information to Google Analytics. We can then
     query Google Analytics statistics to get a better understanding of site usage patterns.
     Google analytics uses cookies to identify and count unique users, and
     to throttle the request rate so their analytics service does not overload. "]
     [:p "Predict also makes use of HotJar to provide a user feedback box, and
     Hotjar also needs cookies to make this service work."]
     [:p "If you do not allow cookies, you will be able to use all the Predict functionality, except that you will not be
     able to leave feedback via the Hotjar popup box at the bottom of the screen. You may of course provide feedback by
     other means - see the contact page for further details."]

     ]
    [:section "Which cookies does the Predict website use?"
     #_[:p "If you have agreed to cookies, Predict will use Google Analytics to measure website traffic, and will use Hotjar to
     provide a user feedback form - the popup window that appears at the bottom of the page. All the information collected is anonymous and is
     not used for any other purpose.  For more details see the "
      [:a {:href "http://www.google.co.uk/intl/en/policies/privacy/" :rel "noopener"} "Google privacy policy."]]
     [:p "The following cookies are used:"]
     [:table.table.table-bordered {:style {:max-width "600px" :margin-top "10px" :font-size "16px"}}
      [:thead
       [:tr {:style {:background-color "#005EB4"
                     :color            "white"}}
        [:th "Name"] [:th "Details"] [:th "Expires"]]]
      [:tbody
       [:tr
        [:td [:code "_ga"]] [:td "Google Analytics. Randomly generated number used to distinguish users."] [:td "two years"]]
       [:tr
        [:td [:code "_gid"]] [:td "Google Analytics. Randomly generated number used to distinguish users and sessions.
        Needed to understand how a user navigates the site"] [:td "24 hours"]]
       [:tr
        [:td [:code "_gat"]] [:td "Google Analytics. Used to throttle request rate."] [:td "1 minute"]]
       [:tr
        [:td [:code "_hjid"]] [:td "Hotjar cookie. This cookie is set when the customer first lands on a page with the Hotjar script. It is used to persist the Hotjar User ID, unique to that site on the browser. This ensures that behavior in subsequent visits to the same site will be attributed to the same user ID."] [:td "one year"]]
       [:tr
        [:td [:code "_hjIncludedInSample"]] [:td "Hotjar cookie. This session cookie is set to let Hotjar know whether that
        visitor is included in the sample which is used to generate funnels. Predict does not enable Hotjar funnels."] [:td "one year"]]
       [:tr
        [:td [:code "_hjMinimizedPolls"]] [:td "Hotjar cookie. This cookie is set once a visitor minimizes a Feedback Poll widget. It is used to ensure that the widget stays minimized when the visitor navigates through the site."] [:td "one year"]]
       #_[:tr
          [:td "1P_JAR"] [:td "Set by Google. This group sets a unique ID to remember your preferences and other information such as website statistics and track conversion rates."] [:td "one week"]]
       #_[:tr
          [:td "mp_*_mixpanel"] [:td "MixPanel cookie. This cookie allows us to carry out user testing on our website in order to improve the user experience and enable us to deliver content that’s relevant."] [:td "one year"]]
       ]]
     ]
    [:section "Can I disable cookies?"
     [:p "Yes. You must actively accept cookies before Predict will allow Google Analytics or Hotjar to use them. Predict
     stores this decision in your browser's local storage. "]

     [:p "If you change your mind later, go to your browser's help facility and
     find out how to clear local storage and cookies. Then, when you revisit the Predict site you will again have the opportunity
     to change the cookie setting."]

     [:p "Google offers a " [:a {:href "https://tools.google.com/dlpage/gaoptout" :rel "noopener"} "tool that you can use to opt out"]
      " of being tracked by Google Analytics. You can add this plugin to your browser by going to Google.
      For more details about controlling cookies visit the help pages for the browser that you are using."]
     [:p "Hotjar also offers " [:a {:href "https://www.hotjar.com/legal/compliance/opt-out" :rel "noopener"} "a page
     explaining how you can opt out"] "."]]
    [:section "Site local storage"
     [:p "Local storage is a persistent storage area controlled by javascript code running in your browser. Predict uses this
      to store user settings. You can use the Settings button on the Predict tool page to adjust these. They are stored under the key
      " [:code "predict-2.1-settings"] "."]
     [:p "Your choice of whether to enable or disable cookies is stored under the key " [:code "user-tc"] "."
      ]]
    ]


   [:section#predict-tool "Predict tool"


    [:section#when-i-add
     #_when-i-add-or-remove-one-treatment-do-the-results-for-others-sometimes-change?
     "When I add or remove one treatment, why do the results for others sometimes change?"
     [:p.screen-only
      (ttt [:tool/when-i-add "Because of the way that the algorithm behind Predict calculates the benefit that each treatment is likely to give, when you add or remove a treatment option you will sometimes see the display of the potential benefit of the other treatments change. This is because the algorithm calculates the benefits for each treatment option in turn and the order in which it does this makes a difference - the impact of the first treatment means there will be fewer people for the remaining treatments to benefit."])]
     [:p (ttt [:tool/when-I-add-2 "Predict calculates the benefits for hormone therapy first, then chemotherapy, then trastuzumab, then bisphosphonates. If you remove the option of hormone therapy, then, you will see that the benefit from chemotherapy will go up slightly as it is now the first therapy that the algorithm is considering and therefore gets the boost of ‘soaking up’ a bit more of the overall benefit."])]]

    [:section#tool-preamble "Preamble"
     [:p.screen-only {:key   0
                      :style {:margin "40px 20px 20px" :font-size "20px"}} (ttt [:tool/preamble "We recommend that patients use this tool in consultation with their
    doctor."])]
     [:p.print-only {:key 1} (ttt [:print/tool-preamble "Predict is a tool that helps show how breast cancer treatments after surgery might improve survival rates.
    This print out shows what characteristics of the patient and the cancer were entered, and then how different treatments would be expected to improve survival
    rates up to 15 years after diagnosis. This is based on data from similar women in the past. Treatments usually have side effects as well as benefits, and it is important to
    consider these as well when making treatment choices. We recommend visiting the sites of charities such as Macmillan and Breast Cancer Now for details about side effects."])]

     ]

    [:section#tool-postamble "Postamble"
     [:section#dummy ""
      [:h3 "Important"]
      [:p "These results are estimates based on records of what happened to women in the past of a similar age and who had a similar cancer.
     There are other important factors, such as lifestyle, which will affect outcomes. Your doctor will help you put these
     results in context."]
      [:h3 "Side effects"]
      [:p "The treatments listed above can have side effects which should be taken into account when choosing a treatment
      regime. See " [:a {:role "button" :on-click #(navigate-to [:about {:page :faqs}])} "the FAQ 'Looking for advice?'"] " for websites providing excellent advice and information on these treatments and their potential
      side effects:"]]]



    [:section#age (ttt [:info-header "Age"])
     [:p (ttt [:info/age1 "The age when the cancer was diagnosed. An age between 25 and 85 may be entered here."])]


     [:p [:i (ttt [:info/age2 "Either type in the number or use the '+' or '-' buttons to adjust it. You can also use the up and down
     arrow keys to step by 1, or the right and left arrow keys to step by 5. Hold a key down for repeated steps."])]]]

    [:section#post-menopausal (ttt [:info-title/post-meno "Post Menopausal"])
     [:p (ttt [:info/post-meno "Treatment with bisphosphonates is only recommended for post-menopausal women."])]]

    [:section#size (ttt [:info-title/size "Size"])
     [:p (ttt [:info/size "The size of the tumour in millimetres. If there was more than one tumour, enter the size of the largest tumour."])]]

    [:section#grade (ttt [:info-title/grade "Tumour grade"])
     [:p (ttt [:info/size1 "The "]) [:strong (ttt [:info/size2 "grade"])]
      (ttt [:info/size3 " describes how different the cancer cells are from normal cells. In a pathology report these are sometimes listed as 'differentiation':"])]
     [:ul
      [:li [:strong (ttt [:info/size4 "Grade 1 (Well differentiated)"])] (ttt [:info/size5 " - the cells are growing slowly and are similar to healthy cells"])]
      [:li [:strong (ttt [:info/size6 "Grade 2 (Moderately differentiated)"])] (ttt [:info/size7 " - the cells are growing faster and are less similar to the healthy ones"])]
      [:li [:strong (ttt [:info/size8 "Grade 3 (Poorly differentiated)"])] (ttt [:info/size9 " - the cells are very different from healthy ones and often fast-growing"])]]
     ]

    [:section#detected-by (ttt [:info-title/detected "Detected by"])
     [:p (ttt [:info/detected-1 "The breast cancer may have been detected through screening (e.g. a preventive "])
      [:a {:href "https://www.nhs.uk/conditions/nhs-screening/" :rel "noopener" :target "_blank"}
       (ttt [:info/detected-2 "screening programme"])] (ttt [:info/detected-3 " such as the
     NHS Breast Screening Programme) or by the appearance of symptoms, and this affects the likely outcomes for the patient."])]

     [:p (ttt [:info/detected-4 "Click on 'Unknown' if this information is not available."])]]

    [:section#micrometastases-only (ttt [:info-title/mmets "Micrometastases only"])
     [:p [:strong (ttt [:info/mmets-1 "Micrometastases"])] (ttt [:info/mmets-2 " are small groups of cancer cells found in the lymph glands."])]
     [:p (ttt [:info/mmets-3 "Modern AJCC staging criteria define micrometastases as groups of cancer cells larger than 0.2 mm but not
     larger than 2.0 mm in largest dimension. Research suggests that patients who only have micrometastases have a
     better prognosis than those who have groups of cells larger than 2 mm. [1]"])]
     [:p (ttt [:info/mmets-4 "If you enter 1 positive node and “Yes” for “Micrometastases only”, this indicates that only one lymph node was
     found to contain cancer cells and that they were only micrometastases. Predict will model this as equivalent to
     half a positive node."])]
     [:p (ttt [:info/mmets-5 "This input is only relevant if you have entered 1 for the number of positive nodes."])]
     [:p (ttt [:info/mmets-6 "Click on 'Unknown' if this information is not available."])]
     [:p {:style {:font-size 14}} (ttt [:info/mmets-7 "[1] Iqbal J, Ginsburg O, Giannakeas V, et al. The impact of nodal
     micrometastasis on mortality among women with early-stage breast cancer.
     Breast Cancer Res Treat 2017;161(1):103-115."])]
     ]

    [:section#positive-nodes (ttt [:info-title/nodes "Positive nodes"])
     [:p (ttt [:info/nodes-1 "The number of "]) [:strong (ttt [:info/nodes-2"positive nodes"])] (ttt [:info/nodes-3" is the number of lymph nodes to which cancer has spread. Some of
     them will have been removed during surgery and examined. A pathology report may quote a pair of numbers such as 2/3,
     meaning 3 lymph nodes were examined and cancer cells were found in 2 of them. In this case you would select '2'"])]
     [:p (ttt [:info/nodes-4 "If you select '1' here, the "]) [:strong (ttt [:info/nodes-5 "micrometastases"])] (ttt [:info/nodes-6" input will be enabled."])]]

    [:section#dcis-or-lcis-only? (ttt :info-title/dcis-1 "DCIS or LCIS only?")
     [:p (ttt [:info/dcis1"The Predict model is not designed to be used for "])
      [:b (ttt [:info/dcis2 "DCIS (Ductal carcinoma in situ)"])]
      (ttt [:info/dcis3 " or for "])
      [:b (ttt [:info/dcis4 "LCIS (Lobular Carcinoma in Situ)"])]
      (ttt [:info/dcis5 ", unless an invasive tumour was also present."])]
     [:p (ttt [:info/dcis6 "If invasive breast cancer coincides with DCIS or LCIS, please use the invasive tumour size in the Predict model."])]]

    [:section#er-status (ttt [:info-title/er-status "ER status"])
     [:p [:strong (ttt [:info/er-status1 "ER status"])]
      (ttt [:info/er-status2 " refers to whether or not the tumour cells have receptors for the hormone oestrogen. Cells with
   these receptors depend on oestrogen to grow, and so hormone therapies (such as
    tamoxifen or aromatase inhibitors) may be successful."])]

     [:p (ttt [:info/er-status3 "It is essential to know the "])
      [:strong (ttt [:info/er-status4 "ER status"])]
      (ttt [:info/er-status5 " of the tumour in order to use this web tool because it makes such a
    difference to the treatment options and outcomes."])]]

    [:section#her2-status (ttt [:info-title/her2 "HER2 status"])
     [:p [:strong (ttt [:info/her2-1 "HER2 status"])]
      (ttt [:info/her2-2-1 "HER2 status refers to whether or not the tumour cells have high numbers of the HER2
      receptors. HER2 is known as a growth factor, and having a high number of HER2 receptors on the cells can cause
      them to divide too much, leading to tumour growth. This is why some treatments aim to block this receptor. About
      a quarter of breast cancer tumours have a high number of HER2 receptors and are known as ‘HER2 positive’. There
      are some drugs that are specifically targeted to HER2 positive tumours, such as trastuzumab (Herceptin) and others."])]
     [:p (ttt [:info/her2-3 "Click on 'Unknown' if this information is not available."])]]

    [:section#ki-67-status (ttt [:info-title/ki167 "KI67 Status"])
     [:p [:strong (ttt [:info/kI167-1 "KI67"])]
      (ttt [:info/KI67-2 " is a protein found in cells when they are preparing to divide, and so the percentage of KI67 cells in a
    tumour can indicate how fast the tumour is growing.  A ‘positive’ result means there are a high number of cells with the 
   KI67 protein. ‘Negative’ means that there are a low number of cells with the KI67 protein. For Predict, we define 'positive' 
   as more than 10% of cells showing KI67."])]
     [:p (ttt [:info/KI67-3 "Click on 'Unknown' if this information is not available. Not all tumours are tested for KI67."])]
     ]

    [:section#about-the-tumour-biological.input-box "About the tumour (biological characteristics)"

     [:p "It is increasingly possible to determine precise characteristics of a tumour that will show what kinds of
    treatment will work best to defeat it."]

     [:p "This section collects information about the key markers that are now routinely tested for in the English health
    system."]

     [:p [:strong "ER status"] " refers to whether or not the tumour cells have receptors for the hormone oestrogen. Cells with
   these receptors depend on oestrogen to grow, and so hormone therapies (such as
    tamoxifen or aromatase inhibitors) may be successful."]

     ;;   [:p "Whether or not you have been through the menopause will be important in assessing the best treatment for you if
     ;;   your tumour is ER or PR positive."]

     [:p "It is essential to know the " [:strong "ER status"] " of the tumour in order to use this web tool because it makes such a
    difference to the treatment options and outcomes."]

     [:p [:strong "HER2 status"] " refers to whether or not the tumour cells have high numbers of the HER2 receptors.  HER2 is known
    as a growth factor, and having a high number of HER2 receptors on the cells can cause the them to keep growing and
    dividing too much. There are some chemotherapy drugs that are specifically targeted to HER2 positive tumours, such
    as trastuzumab (Herceptin)."]

     [:p [:strong "KI67"] " is a protein found in cells when they are preparing to divide, and so the percentage of KI67 cells in a
    tumour can indicate how fast the tumour is growing."]

     ;;    [:p "Oncotype Dx is one of a number of genomic tests that are now available. These look at a range of genes in a
     ;;    tumour to make an assessment of how likely it might be to recur.  Oncotype Dx is now used in England to test some
     ;;    early-stage cancers to help inform treatment options.  It is not suitable for all tumour types.\n\nYou need to enter
     ;;    a value for all of these tests in the tool, but if the test has not been performed, or you do not know the result,
     ;;    simply select " (unknown) "."]

     ;;[:p "Once this information has been entered for a woman, these boxes will disappear and you will not be able to
     ;;  change them. This is to make the website clearer and simpler."]
     ;;]

     ;;[:section#surgery "Treatments already received"
     ;; [:p "This tool is useful in helping to decide on additional therapies after any surgery has taken place"]
     ;;
     ;; #_[:p "This section records the treatment that the woman has already received – whether their surgery was a complete
     ;;       mastectomy or breast-conserving (‘lumpectomy’), and whether they received chemotherapy before surgery (known as
     ;;       neo-adjuvant therapy)."]
     ;;
     ;; #_[:p "You need to enter a value for these options to proceed.  Once this information has been entered for a woman,
     ;;       these boxes will disappear and you will not be able to change them. This is to make the website clearer and simpler."]
     ;;]



     ;;
     ;; Treatment Options and Results
     ;;
     [:section#settings "Settings"
      [:p "Should " [:strong "Radiotherapy"] " be enabled as a treatment option?"]
      ]]

    [:section#hormone-therapy (ttt [:info-title/horm "Hormone Therapy"])
     [:p [:strong (ttt [:info/horm-1 "Hormone therapy"])] (ttt [:info/horm-2 ", or "])
      [:strong (ttt [:info/horm-3 "endocrine therapy"])] (ttt [:info/horm-4", involves a woman taking drugs to prevent
     the growth of tumour cells that are boosted by the hormone oestrogen. Drugs of this kind include tamoxifen (brand
     names include Nolvadex, Istabul, Valodex, and Soltamox) and aromatase inhibitors such as anastrozole, exemestane,
     and letrozole (brand names Arimidex, Aromasin, and Femara). [1]"])]

     [:p (ttt [:info/horm-5 "Some hormone therapy drugs act by blocking the action of oestrogen on the cells and some work by lowering the
     amount of oestrogen in the body (NB hormone therapy for breast cancer is the opposite of hormone replacement therapy
     or HRT, which is taken by women to help INCREASE oestrogen levels to help deal with side-effects of the menopause)."])]


     [:p (ttt [:info/horm-5-5 "Hormone therapy is usually prescribed for a 5 year period, however, trials have looked at whether some women might benefit from staying on hormone therapy for another 5 years - 10 years in total. If this is being considered, it may be useful to switch between 5 and 10 years hormone therapy to compare the survival outcomes."])]

     [:p (ttt [:info/horm-6 "Treatments usually have the potential to cause harm as well as benefit. It is important to weigh up the risks
     of potential harm against the potential benefits of treatment in order to reach a decision. Some may cause more
     harm than benefit to some people."])]

     #_[:p (ttt [:info/horm-7 "It is useful to switch between 5 and 10 years hormone therapy to compare the
     survival outcomes."])]

     [:hr]

     [:p {:style {:font-size "12px"}} (ttt [:info/horm-8 "[1] Source: "])
      [:a {:href "https://www.nhs.uk/news/cancer/breast-cancer-drugs-set-for-preventative-use/" :rel "noopener" :target "_blank"}
       "https://www.nhs.uk/news/cancer/breast-cancer-drugs-set-for-preventative-use/"]
      [:br]
      [:a {:href "https://www.breastcancer.org/treatment/hormonal/serms/tamoxifen" :rel "noopener" :target "_blank"}
       "https://www.breastcancer.org/treatment/hormonal/serms/tamoxifen"]
      [:br]
      [:a {:href "https://www.breastcancer.org/treatment/hormonal/aromatase_inhibitors" :rel "noopener" :target "_blank"}
       "https://www.breastcancer.org/treatment/hormonal/aromatase_inhibitors"]]]


    [:section#already-received-5-years-hormone-therapy (ttt [:info-title/h5-already "Already received 5 years of hormone therapy?"])
     [:p (ttt [:info/h5-already-1 "If you have "]) [:strong (ttt [:info/h5-already-2 "already received 5 years of hormone therapy"])]
      (ttt [:info/h5-already-3 ", enter 'Yes' here. Predict will then
     calculate how a further 5 years of treatment will affect your survival. It will of course assume that you have
     already survived the first 5 years after surgery. The only decision Predict can help you with at this stage is
     whether or not to go for the extra 5 years of hormone therapy. To see this benefit in its proper context, fill in the
     details of your cancer at time of diagnosis, and also the treatments you have "])
      [:strong (ttt [:info/h5-already-4 "already received"])] (ttt [:info/h5-already-5 " in the first five years."])]
     [:p (ttt [:info/h5-already-6 "If you have just had surgery and are choosing between a 5 or a 10 year course of therapy, enter 'No' here."])]
     ]

    [:section#chemotherapy (ttt [:info-title/chemo "Chemotherapy"])
     [:p [:strong (ttt [:info/chemo-1 "Chemotherapy"])] (ttt [:info/chemo-2" uses drugs to weaken or kill cancer cells throughout the body. There are many different
    chemotherapy drugs which work on different kinds of tumour cell, and they are often given in combinations to
    maximise their effectiveness.  The options in this web tool cover generic chemotherapy regimes used most commonly
    in England: "])]
     [:ul
      [:li [:strong (ttt [:info/chemo-3 "No chemotherapy at all"])]]
      [:li [:strong (ttt [:info/chemo-4 "2nd gen"])] (ttt [:info/chemo-5 " is short for second-generation chemotherapy drug regimes such as FEC (fluorouracil, epirubicin and
      cyclophosphamide)"])]
      [:li [:strong (ttt [:info/chemo-6 "3rd gen"])] (ttt [:info/chemo-7 " is short for third-generation chemotherapy drug regimes that contain taxanes such as paclitaxel (Taxol) and docetaxel (Taxotere)"])]]
     [:p (ttt [:info/chemo-8 "The definitions of the different chemotherapy regimes are found in the Early Breast Cancer Trialists' Collaborative Group paper
      "]) [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3273723/" :rel "noopener"} (ttt [:info/chemo-9 "'Comparisons between different polychemotherapy regimens for
        early breast cancer: meta-analyses of long-term outcome among 100 000 women in 123 randomised trials'"])]
      (ttt [:info/chemo-10 ", published in Lancet, 2012."])]
     [:p (ttt [:info/chemo-11 "High cumulative dose anthracylcine regimen were shown in the EBCTCG 2012 analysis to be equivalent to taxane based regimen and should be regarded as third generation."])]
     [:p (ttt [:info/chemo-12 "Treatments usually have the potential to cause harm as well as benefit. It is important to weigh up the risks
     of potential harm against the potential benefits of treatment in order to reach a decision. Some may cause more
     harm than benefit to some people."])]
     ]

    [:section#bisphosphonates (ttt [:info-title/bis "Bisphosphonates"])
     [:p [:strong (ttt [:info-bis-1 "Bisphosphonates"])] (ttt [:info/bis-2 " are drugs that were developed to help stop bones from thinning (osteoporosis), but have also
     been found to help prevent cancer spreading to the bones in postmenopausal women. The most common bisphosphonate
     drugs are zoledronic acid, ibandronate and clodronate.\n"])]

     [:p (ttt [:info/bis-3 "Treatments usually have the potential to cause harm as well as benefit. It is important to weigh up the risks
     of potential harm against the potential benefits of treatment in order to reach a decision. Some may cause more
     harm than benefit to some people."])]]

    ;[:section#radiotherapy "Radiotherapy"
    ; [:p "Radiotherapy text here"]]

    [:section#trastuzumab (ttt [:info-title/tra "Trastuzumab"])
     [:p [:strong (ttt [:info/tra-1 "Trastuzumab"])] (ttt [:info-tra-2 ", often known by the trade name Herceptin, is a drug that specifically targets HER2 positive tumours."])]
     [:p (ttt [:info/tra-3 "Treatments usually have the potential to cause harm as well as benefit. It is important to weigh up the risks
     of potential harm against the potential benefits of treatment in order to reach a decision. Some may cause more
     harm than benefit to some people."])]
     ]

    [:section#show-ranges (ttt [:info-title/ranges "Show ranges"])
     [:p (ttt [:info/ranges "The default values are the approximate best estimate of the benefit. When you click on show ranges, a 95% prediction
     interval is added to the table and the estimated benefits are no longer rounded to the nearest percent.
     The true value of the benefit is very unlikely to be outside this range"])]]

    [:section#adjuvant-treatments.input-box "Adjuvant treatments"

     [:p "This section allows you to enter potential ongoing treatment options for the woman whose details you have
    entered, to see how they are likely to affect her health in the future.  As you change the options, you should see instant
    changes to the accompanying graphs and numbers to allow an easy comparison between treatments."]

     [:p "The treatment options that are shown as available in this web tool depend on the characteristics of the woman
    and the tumour that you have already entered. This is to make the interface clearer and simpler to use."]

     [:p [:strong "Endocrine therapy"] ", or " [:strong "hormone therapy"] ", involves a woman taking drugs to prevent the growth of tumour cells that
    are boosted by the hormone oestrogen.  Some hormone therapy drugs act by blocking the action of
    oestrogen on the cells and some work by lowering the amount of oestrogen in the body."]

     [:p [:strong "Chemotherapy"] " uses drugs to weaken or kill cancer cells throughout the body. There are many different
    chemotherapy drugs which work on different kinds of tumour cell, and they are often given in combinations to
    maximise their effectiveness.  The options in this web tool cover generic chemotherapy regimes used most commonly
    in England:"]

     ;;[:ul {:style {:font-size "1.2em"}}
     ;; [:li [:strong "No chemotherapy at all"]]
     ;; [:li [:strong "2nd gen"] " is short for second-generation chemotherapy drug regimes such as FEC (fluorouracil, epirubicin and
     ;;    cyclophosphamide), CAF or FAC (Cyclophosphamide, doxorubicin, and 5-fluorouracil), AC-T (doxorubicin/cyclophosphamide
     ;;    followed by paclitaxel) or Docetaxel plus cyclophosphamide"]
     ;; [:li [:strong "3rd gen"] " is short for third-generation chemotherapy drug regimes such as
     ;;    DAC (Docetaxel, doxorubicin, and cyclophosphamide), Sequential FEC-taxane, Dose dense sequential doxorubicin/
     ;;    cyclophosphamide-paclitaxel (AC-T) etc."]]
     ;;
     ;;    [:ul
     ;;     [:li "Taxane-containing drugs help stop cancer cells dividing"]
     ;;     [:li "‘Anthra’ is short for anthracyclines, which damage cancer cells and kill them"]
     ;;     [:li "‘CMF’ stands for the regime of Cytoxan, methotrexate and fluorouracil"]]
     ;;
     ;;    [:p "Radiotherapy uses a high energy beam of radiation targeted at the tumour area to damage and kill cancer cells.
     ;;    The radiation affects cancer cells more than normal cells so normal cells in the path of the beam will repair and
     ;;    recover, whilst the cancer cells will be killed.  The options in this web tool cover three types of radiotherapy:"]
     ;;
     ;;    [:ul
     ;;     [:li "Breast/Chest wall – this is when the radiation oncologist targets the beam just to the affected breast area"]
     ;;     [:li "B/CW +Axilla/SCF – this is a beam that targets both the breast/chest wall and the axilla and supraclavicular
     ;;     fossa, to kill any cancer cells in the lymph glands in the underarm and collarbone areas near the tumour"]
     ;;     [:li "B/CW +Axilla/SCF +IMC – this is when the radiotherapy targets the breast/chest wall, the axilla and
     ;;     supraclavicular fossa and the internal mammary chain (near the breastbone) to reach other lymph nodes."]]
     ;;
     ;;    [:p "It also allows you to enter the dose of radiation (measured in Gray units, Gy) that the woman’s heart will be
     ;;    exposed to, and the lung on the side that she will be treated (the ‘ipsilateral lung’)."]
     ;;
     [:p "Bisphosphonates are drugs commonly used to help slow down bone thinning (osteoporosis), but can also be used in
     some women to help prevent cancer spreading to the bones. They are only suitable for post-menopausal women."]
     ]

    [:section#dashed (ttt [:info-title/dashed1 "If death from breast cancer were excluded"])
     [:p (ttt [:info/dashed-1-1 "The dashed line shows the expected survival rate if the breast cancer did not cause any deaths. It goes down
     over time because everyone is at risk of other causes of death. The line is similar to the survival
     rate for women without breast cancer, but it applies to women who have had breast cancer but die of
     other, unrelated, causes."])
      [:p (ttt [:info/dashed-1-2 " This line represents the maximum survival rate possible – it is what would happen if we
     could guarantee that these women would not die of breast cancer.  (Technically, it is "])] [:i "S"] [:sub "0"] (ttt [:info/dashed-2 " in "])
      [:a {:href (iref "/predict-mathematics.pdf") :rel "noopener" :target "_blank"} (ttt [:info/dashed-3 "the mathematical definition"])] ")."]]

    [:section#nobody (ttt [:info-title/nobody1 "If death from breast cancer were excluded"])
     [:p (ttt [:info/nobody-1 "This is a theoretical percentage, if there were a fictional treatment that guaranteed
     that the women represented would not die of breast cancer. The figure therefore applies to patients with breast
     cancer similar to the characteristics entered, and is not representative of the general female population of this
     age. See "])
      [:a {:href "predict-mathematics.pdf" :rel "noopener" :target "_blank"}
       (ttt [:info/nobody-2 "the Mathematical Description"])] (ttt [:info/nobody-3 " for full details."])]]

    [:section#h10-already-warning (ttt [:info-title/h10-already-warning "Extending hormone therapy by another 5 years"])
     [:p (ttt [:info/h10-already-warning-1 "Women are usually offered 5 years of hormone (endocrine) therapy to help reduce the chances of their breast cancer coming back. However, trials have investigated the effects of offering extended therapy (total duration of hormonr therapy of more than 5 years)."])]
     [:p (ttt [:info/h10-already-warning-2 "In order to illustrate the potential benefits of an extra 5 years of hormone therapy, Predict has used the
     data from "]) [:a {:href "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5937869/" :rel "noopener" :target "_blank "}
                             (ttt [:info/h10-already-warning-3-1 "the ATLAS trial"])]
      (ttt [:info/h10-already-warning-3-2 ", and "])
      [:a {:href "https://ascopubs.org/doi/abs/10.1200/jco.2013.31.18_suppl.5" :rel "noopener" :target "_blank "}
       (ttt [:info/h10-already-warning-3-3 "the aTTom trial"])]". "
      (ttt [:info/h10-already-warning-4 "This showed a small benefit to survival to women who stayed on tamoxifen.
      There are currently no trials on the other major type of hormone therapy – aromatase inhibitors – that have
      reported on breast cancer specific mortality. The benefit to
      extending tamoxifen treatment to 10 years was only apparent in years 10-15 after their surgery. You will therefore
       see no benefit when viewing survival only up to 10 years after surgery. Clicking on the ‘curves’ view of the data,
       and showing up to 15 years after surgery, makes the potential benefits most clear."])]
     [:p (ttt [:info/h10-already-warning-6 "Trials on extending aromatase inhibitor hormone therapy from 5 to 10 years have so far only reported how much they reduce the recurrence of breast cancer, rather than the benefit in survival that might result from that. Their results are summarised in a "])
      [:a {:href "https://www.sciencedirect.com/science/article/pii/S0960977619304916?via%3Dihub" :target "blank"} (ttt [:info/h10-already-warning-6-5 "meta-analysis here"])] ". "
      (ttt [:info/h10-already-warning-7 "Given that the reduction in recurrence of breast cancer reported here from extending aromatase inhibitor hormone therapy is similar to that reported by the trials in tamoxifen, it seems reasonable to assume that the benefit of extending aromatase inhibitor therapy from 5 to 10 years will likely be similar as that seen in tamoxifen, and shown in Predict."])]

     [:p (ttt [:info/h10-already-warning-5 "If you are currently considering the potential benefits for a woman who has
     already had 5 years of hormone therapy and is considering staying on it for a further 5 years, then please select 'Yes' for
     'Already received 5 years of hormone therapy. The data shown when 'Yes' is selected is only applicable to a woman who has
     survived the first 5 years after surgery and so is NOT applicable to someone who has recently had surgery and is
     considering a 10 year programme of hormone therapy starting now."])]]
    ]
   ])
