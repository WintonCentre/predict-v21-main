(ns predict3.pages.contact
  (:require [rum.core :as rum]
            [predict3.layout.header :refer [header header-banner footer footer-banner]]
            [predict3.content-reader :refer [section all-subsections]]
            [predict3.state.run-time :refer [route-change]]
            [graphics.simple-icons :refer [icon]]
            [interop.utils :refer [scrollTo]]
            [pubsub.feeds :refer [publish]]
            [predict3.components.bs3-modal :refer [editor-modal]]
            ))

(rum/defc contact < rum/static [ttt route]

  (let [[_ {page :page}] route]
    [:.container-fluid
     (header ttt)
     (header-banner ttt "contact-preamble")
     [:#main-content.row {:tab-index -1}
      [:.col-sm-10.col-sm-offset-1.col-lg-8.col-lg-offset-2 {:style {:min-height "calc(100vh - 700px)"}}
       (all-subsections ttt "contact")
       ]
      ]
     (scrollTo 0)
     (editor-modal)
     (footer-banner ttt)
     (footer ttt)]
    ))
