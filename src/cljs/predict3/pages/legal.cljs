(ns predict3.pages.legal
  (:require [rum.core :as rum]
            [predict3.router :refer [navigate-to iref docroot]]
            [predict3.layout.header :refer [header header-banner footer footer-banner]]
            [predict3.content-reader :refer [section all-subsections]]
            [predict3.state.run-time :refer [route-change]]
            [graphics.simple-icons :refer [icon]]
            [pubsub.feeds :refer [publish]]
            [interop.utils :refer [scrollTo]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            ))

(defn disclaimer
  [ttt]
  [:section {:key 0}
   [:h2 {:style {:color "#1f6bc4"}} (ttt [:legal/disc-title "Disclaimer"])]
   [:p
    {:key "k0"}
    (ttt [:legal/disc-1 "Predict uses an algorithm based on information from many thousands of women diagnosed in
     England and large randomised controlled trials of different treatment options. However, it can only provide
     a 'best guess' of likely outcomes based on current knowledge, and it can never provide an accurate prediction
     for an individual. Patients should always consult their own specialist, who will be able to discuss the
     results in a more personalised context."])]
   [:p
    {:key "k1"}
    (ttt [:legal/disc-2 "The theory behind the model has been subject to open academic peer review in "])
    [:a {:href "http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2880419/?tool=pubmed", :rel "noopener"}
     (ttt [:legal/disc-3 "Breast Cancer Research"])]
    (ttt [:legal/disc-4 ", the "])
    [:a {:href "http://www.ncbi.nlm.nih.gov/pubmed/21371853", :rel "noopener"}
     (ttt [:legal/disc-5 "European Journal of Surgical Oncology"])]
    (ttt [:legal/disc-6 ", the "])
    [:a {:href "http://www.nature.com/bjc/journal/v107/n5/full/bjc2012338a.html", :rel "noopener"}
     (ttt [:legal/disc-7 "British Journal of Cancer "])]
    (ttt [:legal/disc-8 "and "])
    [:a {:href "http://bmccancer.biomedcentral.com/articles/10.1186/1471-2407-14-908", :rel "noopener"}
     (ttt [:legal/disc-9 "BMC Cancer."])]
    (ttt [:legal/disc-10 " The model has been validated on multiple independent breast cancer cohorts with over
     23,000 women diagnosed with breast cancer from England, the Netherlands, Canada and Malaysia with the results
     published in open academic peer review journals. Every effort has been made to ensure that the data used are
     accurate, the statistical procedures sound and the computer algorithm robust."])]])

(defn algorithm
  [ttt]
  [:div
   [:h2 (ttt [:legal/alg-title "Algorithm"])]
   [:h3 (ttt [:legal/alg-section "Explanation of the Predict Algorithm"])]


   [:p (ttt [:legal/alg-1 "The model is based on a precise mathematical form for the cumulative hazard function: this specifies an
      individual’s chance of dying in any period of time following surgery from breast cancer, assuming they do not
      die of some other cause.  The model also contains a cumulative hazard function for dying of other causes,
      assuming no deaths from breast cancer.  These two processes are assumed to be independent and together comprise
      a competing risks model, where the overall chance of being alive at a certain number of years following
      surgery is given by the chance of neither of these two events having occurred."])]

   [:p (ttt [:legal/alg-2 "Details of the form for the baseline cumulative hazards are given in the "])
    [:a {:href (iref "/predict-mathematics.pdf") :rel "noopener"} [:i.fa.fa-file-pdf-o {:aria-hidden true}]
     (ttt [:legal/alg-3 " mathematical description"])] "."]

   [:p (ttt [:legal/alg-4 "For deaths from breast cancer, Predict uses a proportional hazards model in which each risk factor and treatment
      multiplies the baseline cumulative hazard by a fixed amount known as the hazard ratio or relative risk -
      essentially the proportional change in annual mortality risk.  This means the cumulative hazard is the product
      of three components: the baseline hazard (chances of dying from something other than breast cancer), the hazard ratios
      for the risk factors (the increased risk of death due to breast cancer) and the hazard ratios for
      the treatments (the decreased risk thanks to the treatments)."])]

   [:.table-responsive
    [:table.table.table-bordered {:style {:max-width 600}}
     [:caption {:style {:color "#686868"}}
      (ttt [:legal/table1 "Table 1: Risk-factor coefficients for Breast Cancer mortality in ER+ patients (numbers rounded for table)"])]
     [:thead
      [:tr
       [:th (ttt [:legal/table1-1 "Risk Factor"])]
       [:th #_{:style {:text-style "bold"}} (ttt [:legal/table1-2 "Logarithm of multiplier of baseline hazard"])]]]
     [:tbody
      [:tr
       [:th (ttt [:legal/table1-3 "Age at surgery (years)"])]
       [:td "34.5((" [:i (ttt [:legal/m-2 "age"])] "/10)" [:sup "-2"] " -0.0288)   - 34.2((" [:i (ttt [:legal/m-2 "age"])] "/10)" [:sup "-2"] "ln(" [:i (ttt [:legal/m-2 "age"])] "/10) - .051)"]]
      [:tr
       [:th (ttt [:legal/m-4 "Size of tumour (mm)"])]
       [:td "+ 0.75 (ln (" [:i (ttt [:legal/m-5 "size"])] "/100)  +1.55)"]]
      [:tr
       [:th (ttt [:legal/m-6 "Number of nodes"])]
       [:td "+ 0.71 (ln((" [:i (ttt [:legal/m-7 "nodes"])] "+1)/10)   +1.39)"]]
      [:tr
       [:th (ttt [:legal/table1-4 "Grade (1,2,3)"])]
       [:td "+ 0.75 " [:i (ttt [:legal/table1-5 "grade"])]]]
      [:tr
       [:th (ttt [:legal/table1-6 "Screen detected (0,1)"])]
       [:td "- 0.228"]]
      [:tr
       [:th "Her2 (0,1)"]
       [:td "-0.076 " (ttt [:if "if "]) [:i "her2"] " = 0" [:br] "0.241 " (ttt [:if "if "]) [:i "her2"] " = 1"]]
      [:tr
       [:th "Ki67 (0,1)"]
       [:td "-0.113  " (ttt [:if "if "]) [:i "ki67"] " = 0" [:br] "0.149 " (ttt [:if "if "]) [:i "ki67"] " = 1"]]]]
    ]

   [:.table-responsive
    [:table.table.table-bordered {:style {:max-width 600}}
     [:caption {:style {:color "#686868"}}
      (ttt [:legal/table2 "Table 2: Risk-factor coefficients for Breast Cancer mortality in ER- patients (numbers rounded for table)"])]
     [:thead
      [:tr
       [:th (ttt [:legal/table2-1 "Risk Factor"])]
       [:th (ttt [:legal/table2-2 "Logarithm of multiplier of baseline hazard"])]]]
     [:tbody
      [:tr
       [:th (ttt [:legal/table2-3 "Age at surgery (years)"])]
       [:td "0.0089 (" [:i (ttt [:legal/m-2 "age"])] " - 56.3)"]]
      [:tr
       [:th (ttt [:legal/table2-10 "Size of tumour (mm)"])]
       [:td "+ 2.09(√(" [:i "size"] "/100) -0.509)"]]
      [:tr
       [:th (ttt [:legal/table2-11 "Number of nodes"])]
       [:td "+ .626  (ln((" [:i (ttt [:legal/m-7 "nodes"])] " + 1)/10) + 1.09)"]]
      [:tr
       [:th (ttt [:legal/table2-12 "Grade (1,2,3)"])]
       [:td "+ 1.13    " (ttt [:if "if "]) [:i (ttt [:legal/table1-5 "grade"])] " = 2 or 3"]]
      [:tr
       [:th "Her2 (0,1)"]
       [:td "-0.076 " (ttt [:if "if "])  [:i "her2"] " = 0" [:br] "0.241 " (ttt [:if "if "]) [:i "her2"] " = 1"]]
      ]]]

   [:.table-responsive
    [:table.table.table-bordered {:style {:max-width 600}}
     [:caption {:style {:color "#686868"}}
      (ttt [:legal/table3 "Table 3: Treatment Risk-factor coefficients"])]
     [:thead
      [:tr
       [:th (ttt [:legal/table3-1 "Treatment"])]
       [:th "log(" [:i "RR"] ")"]
       [:th (ttt [:legal/table3-2 "approx"]) [:br] (ttt [:legal/table3-3 "se of"]) [:br] "log(" [:i "RR"] ")"]
       [:th (ttt [:legal/table3-3 "Hazard ratio"]) [:br] (ttt [:legal/table3-4 "Relative risk"])]
       [:th (ttt [:legal/table3-5 "Source"])]]
      ]
     [:tbody
      [:tr
       [:th (ttt [:legal/table3-6 "hormone therapy up to 10 years (if ER+)  "])]
       [:td "-0.386"]
       [:td "0.08"]
       [:td "0.68"]
       [:td "Early Breast Cancer Trialists' Collaborative Group (2011) p777"]]
      [:tr
       [:th (ttt [:legal/table3-6 "extended tamoxifen therapy (if ER+)  "])]
       [:td "-0.296"]
       [:td "0.07"]
       [:td "0.74"]
       [:td (ttt [:legal/table3-15 "Meta-analysis of ATLAS and aTTom trials"])]]
      [:tr
       [:th (ttt [:legal/table3-8 "trastuzumab (if HER2+)"])]
       [:td "-0.357"]
       [:td "0.08"]
       [:td "0.70"]
       [:td (ttt [:legal/table3-9 "unpublished meta-analysis of 4 large randomised trials"])]]
      [:tr
       [:th (ttt [:legal/table3-10 "Bisphosphonates (if post-menopausal)"])]
       [:td "-0.198"]
       [:td "0.06"]
       [:td "0.82"]
       [:td "Early Breast Cancer Trialists' Collaborative Group (2015)"]]
      [:tr
       [:th (ttt [:legal/table3-12 "Second gen chemotherapy"])]
       [:td "-0.248"]
       [:td "0.12"]
       [:td "0.78"]
       [:td "Early Breast Cancer Trialists' Collaborative Group (2012)"]]
      [:tr
       [:th (ttt [:legal/table3-14 "Third gen chemotherapy   "])]
       [:td "-0.446"]
       [:td "0.13"]
       [:td "0.64"]
       [:td "Early Breast Cancer Trialists' Collaborative Group (2012)"]]
      ]]]


   [:h3 (ttt [:legal/impl-1 "Implementation of the Algorithm"])]
   [:p (ttt [:legal/impl-2 "The model used to drive this tool is a clojurescript implementation of the Predictv2.1
    model written in R maintained by Professor Paul Pharoah. The full implementation is available in a collection of "])
    [:a {:href "https://github.com/WintonCentre/predict-v21-main" :rel "noopener"}
     (ttt [:legal/impl-3 " open source repositories on GitHub."])]]])

(rum/defc privacy
  [ttt]
  [:div
   [:h2#privacy (ttt [:legal/priv "Privacy"])]
   [:h3 (ttt [:legal/priv-1 "Site Privacy"])]
   [:p (ttt [:legal/priv-2 "Information entered into the Predict tool never leaves your local machine. The information entered in
     'Settings' is limited to tool configuration settings and is stored on your local machine."])]
   [:h3 (ttt [:legal/cookie "Cookie Policy"])]
   [:p (ttt [:legal/cookie-1 "A cookie is a small amount of data sent your computer that your web browser stores when you visit some
     websites. Cookies allow a website to recognise a user’s device e.g. computer, mobile phone. "])]
   [:p (ttt [:legal/cookie-2 "The law on website cookies changed on 26 May 2011 and requires that sites state which cookies are being used
     and their purpose. It also makes clear that cookies only be used with your consent. You can find out more
     information about this law by visiting the "])
    [:a {:href "https://ico.org.uk/for-the-public/online/cookies/" :rel "noopener"}
     (ttt [:legal/cookie-3 "Information Commissioner’s Office website."])]]
   [:p (ttt [:legal/cookie-4 "In using the Predict website you are implicitly giving consent that cookies may be used, however you may
     disable cookie use, see below for details."])]
   [:h3 (ttt [:legal/cookie-5 "Which cookies does the Predict website use?"])]
   [:p (ttt [:legal/cookie-6 "Predict uses Google Analytics to measure website traffic. All the information collected is anonymous and is
     not used for any other purpose. For more details see the "])
    [:a {:href "http://www.google.co.uk/intl/en/policies/privacy/" :rel "noopener"} (ttt [:legal/cookie-7 "Google privacy policy."])]]
   [:p (ttt [:legal/cookie-8 "The following cookies are used:"])]
   [:table.table.table-bordered {:style {:max-width "600px" :margin-top "10px" :font-size "16px"}}
    [:thead
     [:tr {:style {:background-color "#005EB4"
                   :color            "white"}}
      [:th (ttt [:priv/table-1 "Name"])] [:th (ttt [:priv/table-2 "Details"])] [:th (ttt [:priv/table-3 "Expires"])]]]
    [:tbody
     [:tr
      [:td "_utma"] [:td (ttt [:priv/table-4 "stores each user’s number of visits, time of visit etc."])]
      [:td (ttt [:priv/table-5 "two years"])]]
     [:tr
      [:td "_utmb"]
      [:td (ttt [:priv/table-6 "checks approximately how long a user stays on the site"])]
      [:td (ttt [:priv/table-7 "30 minutes"])]]
     [:tr
      [:td "_utmc"]
      [:td (ttt [:priv/table-8 "stores each user’s number of visits"])]
      [:td (ttt [:priv/table-9 "End of browsing session"])]]
     [:tr
      [:td "_utmz"]
      [:td (ttt [:priv/table-10 "stores where a visitor came from"])]
      [:td (ttt [:priv/table-11 "two years"])]]
     [:tr
      [:td "_hjDonePolls"]
      [:td (ttt [:priv/table-12 "Hotjar cookie. This cookie is set once a visitor completes a poll using the Feedback
      Poll widget. It is used to ensure that the same poll does not re-appear if it has already been filled in."])]
      [:td (ttt [:priv/table-13 "one year"])]]
     [:tr
      [:td "_hjMinimizedPolls"]
      [:td (ttt [:priv/table-14 "Hotjar cookie. This cookie is set once a visitor minimizes a Feedback Poll widget.
      It is used to ensure that the widget stays minimizes when the visitor navigates through the site."])]
      [:td (ttt [:priv/table-15 "one year"])]]
     ]]

   [:h3 (ttt [:priv/disable "Can I disable cookies?"])]
   [:p (ttt [:priv/disable-1 "You can opt out of cookies at any time, following the instructions in your browser. These are typically found
     under ‘Tools’ and ‘Options’ (PC) or ‘Preferences’ (Mac) though the details vary from browser to browser.
     Also, Google offers a "])
    [:a {:href "https://tools.google.com/dlpage/gaoptout" :rel "noopener"}
     (ttt [:priv/disable-2 "tool that you can use to opt out"])]
    (ttt [:priv/disable-3 " of being tracked by Google Analytics. You can add this plugin to your browser by going to Google.
    For more details about controlling cookies visit the help pages for the browser that you are using."])]])

(rum/defc legal < rum/static [ttt route]
  (let [[_ {page :page}] route]
    [:.container-fluid
     (header ttt)
     (header-banner ttt "legal-preamble")
     [:#main-content.row {:tab-index -1}
      [:.col-sm-10.col-sm-offset-1.col-lg-8.col-lg-offset-2 {:style {:min-height "calc(100vh - 700px)"}}
       (condp = page
         "disclaimer" (disclaimer ttt)
         "algorithm" (algorithm ttt)
         "privacy" (privacy ttt))
       ]]
     ;(scrollTo 0)
     (editor-modal)
     (footer-banner ttt)
     (footer ttt)]
    ))

(comment
  (defn ttt [[k s]] s)

  (header ttt)
  (header-banner ttt)
  (disclaimer ttt)
  (algorithm ttt)

  (all-subsections ttt (name :disclaimer))
  )