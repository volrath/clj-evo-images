(ns evo-images.drawing
  (:require [clojure.math.numeric-tower :refer [abs]]
            [quil.core :as q]))

(def img (ref nil))
(def size 200)

(defn setup-sketch! []
  (dosync
   (ref-set img (q/load-image "botw.jpg")))

  (q/background 240)                   ; Clear the sketch by filling it with light-grey color.
  (q/image @img 50 50)                 ; Draw our image
  )

(defn- draw-creature [cx cy creature]
  (q/with-translation [cx cy]
    (q/fill 255 255 255)  ;; white background, but maybe black is better?
    (q/rect 0 0 size size)

    (doseq [shape creature]
      (apply q/fill (:color shape))

      (q/begin-shape)
      (doseq [{:keys [x y]} (:points shape)]
        (q/vertex x y))
      (q/end-shape))

    ;; Debug text
    ;; (q/fill 255 255 255)
    ;; (q/rect 0 200 200 25)
    ;; (q/fill 0)
    ;; (q/text-align :left)
    ;; (let [used-color (q/get-pixel 100 100)]
    ;;   (q/text (str (q/red used-color) " "
    ;;                (q/green used-color) " "
    ;;                (q/blue used-color) " "
    ;;                (q/alpha used-color)) 0 215))
    ))


(defn draw [state]
  (q/no-stroke)
  (let [[iteration max-fitness best competing] state]
    (draw-creature 300 50 best)
    (draw-creature 550 50 competing)

    ;; Debug text
    (q/fill 255 255 255)
    (q/rect 0 300 800 25)
    (q/fill 0)
    (q/text-align :center)
    (q/text (str "Iteration: " iteration ", Max Fitness: " max-fitness) 400 315)))

(defn compute-fitness [creature]
  (let [creature-img    (q/get-pixel 550 50 size size)
        creature-pixels (q/pixels creature-img)
        original-pixels (q/pixels @img)
        distance        (fn [p1 p2] (abs (- p1 p2)))]
    (reduce + (map distance creature-pixels original-pixels))))
