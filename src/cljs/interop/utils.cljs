(ns interop.utils)

(defn scrollTo [offset]
  (-> (js/$ "html, body")
      (.animate #js {:scrollTop offset} 250))
  nil)