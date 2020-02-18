(ns predict3.pages.not_found
  (:require [rum.core :as rum]
            [bide.core :as r]
            [predict3.router :refer [router]]
            [interop.utils :refer [scrollTo]]
            [predict3.layout.header :refer [header footer]]
            [predict3.content-reader :refer [all-subsections]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            ))

(rum/defc not-found < rum/static [ttt]

  [:.container
   [:.row
    [:.col-sm-12
     (header ttt)

     [:.row
      [:.col-sm-4 {:style {:padding-left  "25px"
                           :padding-right "25px"}}
       [:h2 "Oops!"
        [:p (ttt [:not-found/oops "Try clicking on 'Home' in the navigation bar instead."])]]]

      [:.col-sm-8.col-xs-12 {:style {:min-height "calc(100vh - 200px)"}}
       [:img.img-responsive {:src   "/assets/404.jpg"
                             :alt   "The Winton Centre retreat in Berlin"
                             :style {:margin-top "3ex"}}]]]
     (editor-modal)
     (scrollTo 0)
     (footer ttt)]]])
