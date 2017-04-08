(ns evo-images.core
  (:gen-class)
  (:require [evo-images.drawing :refer [dominant-color draw setup-sketch!]]
            [evo-images.evolution :refer [evolve init-state]]
            [quil.core :as q]
            [quil.middleware :as m]))

(defn setup [img-src]
  (fn []
    (setup-sketch! img-src)
    (init-state (dominant-color img-src))))

(defn start
  ([] (start "botw.jpg"))
  ([img-src]
   (q/defsketch evo-images
     :title "Evolving images"
     :size [800 325]

     :setup  (setup img-src)
     :update evolve
     :draw   draw

     ;; :features [:keep-on-top]
     :middleware [m/fun-mode])))

;; (start)
