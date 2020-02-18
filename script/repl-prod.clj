(use 'figwheel-sidecar.repl-api)
;(require '[figwheel-sidecar.repl-api :as f])
(require '[figwheel-sidecar.config :as fc])
(require '[simple-lein-profile-merge.core :as lm])

(def edit-profiles
  (assoc lm/default-profiles 4 :dev-prod 5 :dev)
  ;(conj lm/default-profiles :dev-edit)
  )

(defn fetch-edit-config
  []
  (println edit-profiles)
  (assoc (fc/initial-config-source)
    :active-profiles edit-profiles))

(defn start-edit-figwheel!
  []
  (start-figwheel!
    (fetch-edit-config)
    "dev-prod"))

(defn fig-start
  "This starts the figwheel server and watch based auto-compiler."
  []
  ;; this call will only work are long as your :cljsbuild and
  ;; :figwheel configurations are at the top level of your project.clj
  ;; and are not spread across different lein profiles

  ;; otherwise you can pass a configuration into start-figwheel! manually
  (start-edit-figwheel!)
  )

(fig-start)                                           ;; <-- fetches configuration
(cljs-repl)