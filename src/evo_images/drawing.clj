(ns evo-images.drawing
  (:require [clojure.java.io :as io]
            [clojure.math.numeric-tower :refer [abs expt round]]
            [clojure.spec :as s]
            evo-images.specs
            [predominance.core :refer [color]]
            [quil.core :as q]))

(def img (ref nil))
(def img-size 200)
(def fitness-norm-coef (* img-size img-size 3 255))

(defn round-places [number decimals]
  (let [factor (expt 10 decimals)]
    (double (/ (round (* factor number)) factor))))

(defn setup-sketch! [img-src]
  (dosync
   (ref-set img (q/load-image img-src)))

  (q/frame-rate 10000)                 ; High framerate so that Quil tries to iterate as fast as possible.
  (q/background 240)                   ; Clear the sketch by filling it with light-grey color.
  (q/image @img 50 50)                 ; Draw our image.
  )

(defn dominant-color [img-src]
  (let [c (color (io/as-file (str "resources/" img-src)))]
    [(.getRed c) (.getGreen c) (.getBlue c) 255]))

(defn- draw-creature [cx cy creature]
  (q/with-translation [cx cy]
    (q/fill 255 255 255)  ;; white background, but maybe black is better?
    (q/rect 0 0 img-size img-size)

    (doseq [shape creature]
      (apply q/fill (:color shape))

      (q/begin-shape)
      (doseq [{:keys [x y]} (:polygon shape)]
        (q/vertex x y))
      (q/end-shape))))


(defn draw-text-box [text bx by bw bh]
  (q/with-translation [bx by]
    (q/fill 232)
    (q/rect 0 0 bw bh)
    ;; Set the text
    (q/fill 0)
    (q/text-align :center)
    (q/text text (/ bw 2) (+ 4 (/ bh 2)))))

(defn draw [state]
  (q/no-stroke)
  (let [[iteration improvements max-fitness best competing] state]
    (draw-creature 300 50 best)
    (draw-creature 550 50 competing)

    ;; Debug text
    (draw-text-box (str "Fitness: " (round-places max-fitness 6)) 300 25 200 25)
    (draw-text-box (str "Improvements: " improvements) 300 250 200 25)

    (draw-text-box (str "M/s: " (round-places (q/current-frame-rate) 1)) 550 25 200 25)
    (draw-text-box (str "Mutations: " iteration) 550 250 200 25)

    (draw-text-box (str "Elapsed time: " (round-places (/ (q/millis) 1000) 2) "s") 50 25 200 25)))


                                        ; Fitness Calculation

(defn- to-rgb
  "Returns RGB values for a Quil color"
  [color]
  (map (fn [f] (f color)) [q/red q/green q/blue]))

(defn- color-distance
  "Computes a distance between the RGB values of two colors `c1` and `c2`"
  [c1 c2]
  (let [rgb1     (to-rgb c1)
        rgb2     (to-rgb c2)
        distance (fn [p1 p2] (abs (- p1 p2)))]
    (reduce + (map distance rgb1 rgb2))))

(defn- compute-real-fitness
  "Returns the sum of the color differences in each pixel of both the creature's
  image and the original image"
  [creature]
  (let [creature-img    (q/get-pixel 550 50 img-size img-size)
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
        :ret  :evo-images.specs/color)

(s/fdef draw-creature
        :args (s/cat :cx int? :cy int? :creature :evo-images.specs/creature))

(s/fdef draw
        :args (s/coll-of :evo-images.specs/state :count 1))

(s/fdef color-distance
        :args (s/cat :c1 int? :c2 int?)
        :ret  int?)

(s/fdef compute-real-fitness
        :args (s/coll-of :evo-images.specs/creature :count 1)
        :ret  int?)

(s/fdef compute-fitness
        :args (s/coll-of :evo-images.specs/creature :count 1)
        :ret  double?)
