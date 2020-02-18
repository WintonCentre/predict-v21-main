(ns predict3.components.bs3-navbar
  (:require [rum.core :as rum]
            [predict3.router :refer [router set-location]]
            [bide.core :as r]
            [predict3.state.run-time :refer [route route-change language-change
                                             t-state-cursor ttt-cursor add-language-modal]]
            [interop.utils :refer [scrollTo]]
            [pubsub.feeds :refer [publish]]
            [predict3.components.bs-mixins :refer [dont-manage]]))


(rum/defc collapsing-navbar < rum/static [{:keys [id] :as props} navbar-menu]
  [:.collapse.navbar-collapse {:id id}
   navbar-menu])


(rum/defc navbar [{:keys [id class-name logo] :as props} navbar-menu]
  [:nav {:class-name (str "navbar " class-name)}
   [:.container
    [:.navbar-header
     [:button.navbar-toggle {:type        "button"
                             :data-toggle "collapse"
                             :data-target (str "#" id)
                             :aria-label  "menu"
                             }
      (map (fn [i] [:span.icon-bar {:key i}]) (range 3))]
     (when logo [:img.navbar-brand {:src logo :alt "logo"}])]
    (collapsing-navbar {:id id} navbar-menu)]
   ])


(defrecord Nav-item [text on-click target])


(defn navigate-to [target]
  (when (= target [:about {:page :faqs}])                   ; nasty fix to always force a scroll to top of FAQs
    (scrollTo 0))
  (if (not= target [:no-op])
    (publish route-change target)))


(defn make-nav-item [key rt {:keys [text on-click target submenus] :as item}]
  (if (seq submenus)
    [:li {:key key :tab-index 0 :style {:cursor "pointer"} :class-name (str "dropdown" (when (= (first target) (first rt)) " active"))}
     ; removing data-toggle in next line means clicking on a dropdown button does not toggle open status
     [:a.dropdown-toggle {:data-toggle "dropdown"
                          :on-click #(on-click target)} text [:span.caret]]
     [:ul.dropdown-menu
      (map-indexed #(make-nav-item %1 rt %2) submenus)]]
    [:li {:key key :tab-index 0 :style {:cursor "pointer"} :class-name (when (= (first target) (first rt)) "active")}
     [:a {:on-click    #(on-click target)
          :data-toggle "collapse"
          :data-target ".navbar-collapse.in"
          } text]]))


(defn nav-items
  [ttt lang supported-languages]

  [(Nav-item. (ttt [:navbar/home "Home"]) navigate-to [:home {:page :home}])

   (assoc (Nav-item. (ttt [:navbar/about "About Predict"]) navigate-to [:no-op]) ;this suppresses a page change before the dropdown item has been selected
     :submenus [(Nav-item. (ttt [:navbar/overview "Overview"]) navigate-to [:about {:page :overview :section :overview}])
                (Nav-item. (ttt [:navbar/wiif "– Who is it for?"]) navigate-to [:about {:page :overview :section :whoisitfor}])
                (Nav-item. (ttt [:navbar/hpw "– How Predict works"]) navigate-to [:about {:page :overview :section :howpredictworks}])
                (Nav-item. (ttt [:navbar/wbp "– Who built Predict?"]) navigate-to [:about {:page :overview :section :whobuiltpredict}])
                (Nav-item. (ttt [:navbar/tech "Technical"]) navigate-to [:about {:page :technical}])
                (Nav-item. (ttt [:navbar/dh "– Development History"]) navigate-to [:about {:page :technical :section :history}])
                #_(Nav-item. (ttt [:navbar/pv "– Previous Versions"]) navigate-to [:about {:page :technical :section :preversions}])
                (Nav-item. (ttt [:navbar/pubs "– Publications"]) navigate-to [:about {:page :technical :section :publications}])
                (Nav-item. (ttt [:navbarfaqs "FAQs"]) navigate-to [:about {:page :faqs}])])

   (Nav-item. (ttt [:navbar/tool "Predict Tool"]) navigate-to [:tool])
   (Nav-item. (ttt [:navbar/contact "Contact"]) navigate-to [:contact])
   (assoc (Nav-item. (ttt [:navbar/legal "Legal"]) navigate-to [:no-op])
     :submenus [(Nav-item. (ttt [:navbar/disclaimer"Disclaimer"]) navigate-to [:legal {:page :disclaimer}])
                (Nav-item. (ttt [:navbar/alg "Algorithm"]) navigate-to [:legal {:page :algorithm}])
                (Nav-item. (ttt [:navbar/privacy "Privacy & Data Protection"]) navigate-to [:legal {:page :privacy}])])
   #_(Nav-item. (ttt [:navbar/earlier "Earlier versions"]) #(set-location "/all_versions.html") nil)
   (assoc (Nav-item. (ttt [:navbar/change-language "Change Language "]) navigate-to [:no-op])
     :submenus (conj (mapv (fn [lang]
                             (Nav-item. (ttt lang) #(publish language-change lang) nil))
                       supported-languages)
                 #_(Nav-item. (ttt [:navbar/add-lang "+ Add language"]) #(publish add-language-modal nil) nil)))])


(def smartmenus {:did-mount (fn [state]
                              (js/jQuery.SmartMenus.Bootstrap.init)
                              state)})

(rum/defc simple-navbar < rum/static rum/reactive  [[menu-items]]
  (let [rt (rum/react route)]
    (navbar {:id         "navbar"
             :class-name "navbar-inverse"
             :logo       nil}
            [:ul.nav.navbar-nav
             (map-indexed #(make-nav-item %1 rt %2) menu-items)])))

(defn scroll-handler
  "Returns a 'sticky' scroll event handler for a dom-node"
  [node transition-at tolerance]
  (fn [_]
    (let [y-off (aget js/window "pageYOffset")
          c (+ transition-at (.-offsetTop node))]
      (if (>= y-off (+ c tolerance))
        (.add (.-classList node) "navbar-fixed-top")
        (when (<= y-off (- c tolerance))
          (.remove (.-classList node) "navbar-fixed-top"))))))

(def sticky-mixin
  {:did-mount    (fn [state]
                   (let [comp (:rum/react-component state)
                         dom-node (js/ReactDOM.findDOMNode comp)
                         #_#_sh (scroll-handler dom-node 100)
                         sh (scroll-handler dom-node 60 40)]
                     (js/addEventListener "scroll" sh)
                     (assoc state ::scroll-handler sh
                                  ::y-off (atom (aget js/window "pageYOffset")))))

   :will-unmount (fn [state]
                   (let [comp (:rum/react-component state)
                         dom-node (js/ReactDOM.findDOMNode comp)
                         sh (::scroll-handler state)]
                     (js/removeEventListener "scroll" sh)
                     (dissoc state ::scroll-handler ::y-off)))})

(rum/defc hamburger-navbar < sticky-mixin smartmenus rum/reactive
  [ttt]
  (let [t-state (rum/react t-state-cursor)                  ;reacts to ttt-cursor anyway
        lang (name (:lang t-state))
        supported-languages (:languages t-state)]
  [:nav
     (simple-navbar [(nav-items ttt lang supported-languages)])]))


(comment
  ; a bs navbar for reference
  #_(rum/defc hamburger-navbar* []
      [:nav.navbar.navbar-inverse
       [:.container
        [:.navbar-header
         [:button.navbar-toggle {:type        "button"
                                 :data-toggle "collapse"
                                 :data-target "#navbar"}
          (map (fn [i] [:span.icon-bar {:key i}]) (range 3))]
         #_[:img.navbar-brand {:style {:padding 10} :src "/assets/logo-neg.png"}]]
        [:#navbar.collapse.navbar-collapse
         [:ul.nav.navbar-nav
          [:li.active
           [:a {:href "#"} "Home"]]
          [:li.dropdown {:key 1}
           [:a.dropdown-toggle {:data-toggle "dropdown"
                                :href        "#"}
            "Menu " [:span.caret]]
           [:ul.dropdown-menu
            [:li {:key 1} [:a.link {:href "#/News"} "News"]]
            [:li {:key 2} [:a {:href "#"} "Page 1-2"]]
            [:li {:key 3} [:a {:href "#"} "Page 1-3"]]]]
          [:li [:a {:key  2
                    :href "#"} "Page 2"]]
          [:li [:a {:key  3
                    :href "#"} "Page 3"]]]]]])
  )
