(ns interop.bundled)

; See IsMediaType script from http://cssmedia.pemor.pl/#download

(defn media? [coll]
  (= 1 (apply (partial (.-IsMediaType js/window)) (map str coll))))

(comment
  (media? #{"print"})
  )

;; these do not work in chrome or ff - at least not with dev tools emulator set to "print", or in mobile modes.
(defn mobile? [] (= 1 (.IsMediaType js/window "handheld")))
(defn printing? [] (= 1 (.IsMediaType js/window "print")))

;; this works reliably in FF and Chrome and Safari
(defn on-screen? [] (= 1 (.IsMediaType js/window "screen")))
