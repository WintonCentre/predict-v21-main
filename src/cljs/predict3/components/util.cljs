(ns predict3.components.util)

(defn ttt-key
  [widget-id widget-key]
  (keyword (str widget-id "/" (name widget-key))))

(defn widget-ttt
  "Wrap text in a ttt call for a particular widget context"
  [ttt widget-id widget-key text]
  (ttt [(ttt-key widget-id widget-key) text]))

