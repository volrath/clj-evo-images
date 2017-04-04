(ns evo-images.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def botw (ref nil))
(def size 200)

(defn setup []
  ;; (q/frame-rate 120)
  (q/background 240)                    ; Clear the sketch by filling it with light-grey color.
  (dosync
   (ref-set botw (q/load-image "botw.jpg")))
  (q/image @botw 50 50)                 ; Draw our image

  {:color -1
   :points []})

(defn evolve [state]
  (if (q/mouse-pressed?)
    (let [mx    (q/mouse-x)
          my    (q/mouse-y)]

      (cond
        (and (<= 50 mx 250) (<= 50 my 250))   (assoc state :color (q/get-pixel mx my))
        (and (<= 300 mx 500) (<= 50 my 250))  (update state :points conj {:x (- mx 300) :y (- my 50)})
        :else state)
      )
    state))

(defn draw-dna [x y color points]
  (q/with-translation [x y]
    (when points  ; when points, put a nice white background.
      (q/fill 255 255 255)
      (q/rect 0 0 size size))

    (q/fill color 100)
    (q/begin-shape)
    (doseq [{:keys [x y]} points]
      (q/vertex x y))
    (q/end-shape)

    ;; Debug text
    (q/fill 255 255 255)
    (q/rect 0 200 200 25)
    (q/fill 0)
    (q/text-align :left)
    (let [used-color (q/get-pixel 100 100)]
      (q/text (str (q/red used-color) " "
                   (q/green used-color) " "
                   (q/blue used-color) " "
                   (q/alpha used-color)) 0 215))))


(defn draw-state [state]
  (q/no-stroke)

  (let [mx    (q/mouse-x)
        my    (q/mouse-y)]
    (draw-dna 300 50 (:color state) (:points state))
    (draw-dna 550 50 (:color state) [{:x 0 :y 0} {:x 200 :y 0} {:x 200 :y 200} {:x 0 :y 200}])

    ;; Debug text
    (q/fill 255 255 255)
    (q/rect 0 300 800 25)
    (q/fill 0)
    (q/text-align :center)
    (q/text (str "[" mx ", " my "] - " state) 400 315)))

(q/defsketch evo-images
  :title "Evolving images"
  :size [800 325]
                                        ; setup function called only once, during sketch initialization.
  :setup setup
                                        ; update-state is called on each iteration before draw-state.
  :update evolve
  :draw draw-state
  ;; :features [:keep-on-top]
                                        ; This sketch uses functional-mode middleware.
                                        ; Check quil wiki for more info about middlewares and particularly
                                        ; fun-mode.
  :middleware [m/fun-mode])
