(ns predict3.layout.navbar
  (:require [rum.core :as rum]
            [predict3.state.run-time :refer [model input-changes]]
            [predict3.state.mutations :refer [clear-inputs]]))


(rum/defc clear-all-button < rum/static [{:keys [ttt on-click] :as props}]
  [:button#reset.btn.navbar-btn.btn-danger.btn-lg.screen-only {:on-click on-click}
   (ttt [:tool/reset-button " Reset "])])


