(ns evo-images.drawing
  (:require [clojure.java.io :as io]
            [clojure.math.numeric-tower :refer [abs]]
            [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [predominance.core :refer [color]]
            [quil.core :as q]))

(def img (ref nil))
(def size 200)
(def fitness-norm-coef (* size size 3 255))

(defn setup-sketch! [img-src]
  (dosync
   (ref-set img (q/load-image img-src)))

  (q/frame-rate 10000)
  (q/background 240)                   ; Clear the sketch by filling it with light-grey color.
  (q/image @img 50 50)                 ; Draw our image
  )

(defn dominant-color [img-src]
  (let [c (color (io/as-file (str "resources/" img-src)))]
    [(.getRed c) (.getGreen c) (.getBlue c) 255]))

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

(defn- color-distance [c1 c2]
  (let [to-rgb  (fn [c] (map (fn [f] (f c)) [q/red q/green q/blue]))
        rgb1    (to-rgb c1)
        rgb2    (to-rgb c2)
        distance (fn [p1 p2] (abs (- p1 p2)))]
    (reduce + (map distance rgb1 rgb2))))

(defn- compute-real-fitness [creature]
  (let [creature-img    (q/get-pixel 550 50 size size)
        creature-pixels (q/pixels creature-img)
        original-pixels (q/pixels @img)]
    (reduce + (map color-distance creature-pixels original-pixels))))

(defn compute-fitness
  "A normalized fitness..."
  [creature]
  (* 100 (- 1 (/ (compute-real-fitness creature) fitness-norm-coef))))


                                        ; Instrumentation

(s/fdef setup-sketch!
        :args (s/cat :img-src string?))

(s/fdef dominant-color
        :args (s/cat :img-src string?)
        :ret  :evo-images.evolution/color)

(s/fdef draw-creature
        :args (s/cat :cx int? :cy int? :creature :evo-images.evolution/creature))

(s/fdef draw
        :args (s/coll-of :evo-images.evolution/state :count 1))

(s/fdef color-distance
        :args (s/cat :c1 int? :c2 int?)
        :ret  int?)

(s/fdef compute-real-fitness
        :args (s/coll-of :evo-images.evolution/creature :count 1)
        :ret  int?)

(s/fdef compute-fitness
        :args (s/coll-of :evo-images.evolution/creature :count 1)
        :ret  double?)

;; (do (stest/unstrument `setup-sketch!)
;;     (stest/unstrument `dominant-color)
;;     (stest/unstrument `draw-creature)
;;     (stest/unstrument `draw)
;;     (stest/unstrument `color-distance)
;;     (stest/unstrument `compute-real-fitness)
;;     (stest/unstrument `compute-fitness))

;; (do (stest/instrument `setup-sketch!)
;;     (stest/instrument `dominant-color)
;;     (stest/instrument `draw-creature)
;;     (stest/instrument `draw)
;;     (stest/instrument `color-distance)
;;     (stest/instrument `compute-real-fitness)
;;     (stest/instrument `compute-fitness))
