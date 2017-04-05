(ns evo-images.core
  (:gen-class)
  (:require [evo-images.drawing :refer [draw setup-sketch!]]
            [evo-images.evolution :refer [evolve init-state]]
            [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  (setup-sketch!)
  (init-state))


(q/defsketch evo-images
  :title "Evolving images"
  :size [800 325]

  :setup  setup
  :update evolve
  :draw   draw

  ;; :features [:keep-on-top]
  :middleware [m/fun-mode])
