(ns evo-images.evolution
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [evo-images.drawing :refer [compute-fitness img-size]]
            evo-images.specs
            [evo-images.specs :refer [max-points max-shapes]]))


(def mutation-thresholds
  "Probability of swaping an entire shape/color/point when mutating"
  {:swap-shape 0
   :swap-color 0
   :swap-point 0})


                                        ; Initializers

(defn create-creature
  ([]
   (gen/generate (s/gen :evo-images.specs/creature)))
  ([tint]
   (let [creature (create-creature)
         tint-shape (fn [shape] (assoc shape :color tint))]
     (mapv tint-shape creature))))

(defn init-state
  ([]
   (let [initial-creature (create-creature)]
     [0 0 0.0 initial-creature initial-creature]))
  ([dominant-color]
   (let [initial-creature (create-creature dominant-color)]
     [0 0 0.0 initial-creature initial-creature])))


                                        ; Mutation

(defn mutate-shape-polygon [polygon]
  (let [roulette (rand)]
    (cond
      (< roulette (:swap-point mutation-thresholds)) (assoc polygon (rand-int max-points) (gen/generate (s/gen :evo-images.evolution/point)))
      :else (assoc-in polygon [(rand-int max-points) (rand-nth [:x :y])] (rand-int (+ img-size 1))))))

(defn mutate-shape-color [color]
  (let [roulette (rand)]
    (cond
      (< roulette (:swap-color mutation-thresholds)) (gen/generate (s/gen :evo-images.evolution/color))
      :else (assoc (into [] color) (rand-int 4) (rand-int 256)))))

(defn- mutate-shape [shape]
  (let [roulette (rand)
        whole-piece-threshold (:swap-shape mutation-thresholds)]
    (cond
      (< roulette whole-piece-threshold)             (gen/generate (s/gen :evo-images.evolution/shape)) ; Change the whole shape for a new one
      (< roulette (/ (- 1 whole-piece-threshold) 2)) (update shape :color mutate-shape-color)
      :else                                          (update shape :polygon mutate-shape-polygon))))

(defn- mutate [creature]
  (update creature (rand-int max-shapes) mutate-shape))


                                        ; Survival

(defn- compete [max-fitness best competing]
  (let [competing-fitness (compute-fitness competing)]
    (if (> competing-fitness max-fitness)
      [competing-fitness competing]
      [max-fitness best])))

(defn evolve [state]
  (let [[iteration improvements old-max-fitness best competing] state
        [max-fitness best] (compete old-max-fitness best competing)]
    [(inc iteration)
     (if (> max-fitness old-max-fitness) (inc improvements) improvements)
     max-fitness
     best
     (mutate best)]))


                                        ; Instrumentation

;; Initializers

(s/fdef init-state
        :ret :evo-images.specs/state)

(s/fdef create-creature
        :args (s/cat :tint (s/? (s/coll-of int? :count 4)))  ;; ::color
        :ret  :evo-images.specs/creature)

;; Mutation

(s/fdef mutate-shape-polygon
        :args (s/cat :polygon :evo-images.specs/polygon)
        :ret  :evo-images.specs/polygon)

(s/fdef mutate-shape-color
        :args (s/coll-of :evo-images.specs/color :count 1)
        :ret  :evo-images.specs/color)

(s/fdef mutate-shape
        :args (s/cat :shape :evo-images.specs/shape)
        :ret  :evo-images.specs/shape)

(s/fdef mutate
        :args (s/cat :creature :evo-images.specs/creature)
        :ret  :evo-images.specs/creature)

;; Survival

(s/fdef compete
        :args (s/cat :max-fitness :evo-images.specs/fitness
                     :best        :evo-images.specs/creature
                     :competing   :evo-images.specs/creature)
        :ret  (s/cat :max-fitness :evo-images.specs/fitness
                     :best        :evo-images.specs/creature))

(s/fdef evolve
        :args (s/coll-of :evo-images.specs/state :count 1)
        :ret  :evo-images.specs/state)
