(ns evo-images.evolution
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]
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
   (gen/generate (s/gen :evo-images.evolution/creature)))
  ([tint]
   (let [creature (create-creature)
         tint-shape (fn [shape] (assoc shape :color tint))]
     (mapv tint-shape creature))))

(defn init-state [dominant-color]
  (let [initial-creature (create-creature dominant-color)]
    [0 0.0 initial-creature initial-creature]))


                                        ; Mutation

(defn mutate-shape-points [points]
  (let [roulette (rand)]
    (cond
      (< roulette (:swap-point mutation-thresholds)) (assoc points (rand-int max-points) (gen/generate (s/gen :evo-images.evolution/point)))
      :else (assoc-in points [(rand-int max-points) (rand-nth [:x :y])] (rand-int (+ img-size 1))))))

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
      :else                                          (update shape :points mutate-shape-points))))

(defn- mutate [creature]
  (update creature (rand-int max-shapes) mutate-shape))


                                        ; Survival

(defn- compete [max-fitness best competing]
  (let [competing-fitness (compute-fitness competing)]
    (if (> competing-fitness max-fitness)
      [competing-fitness competing]
      [max-fitness best])))

(defn evolve [state]
  (let [[iteration max-fitness best competing] state
        [max-fitness best] (compete max-fitness best competing)]
    [(inc iteration)
     max-fitness
     best
     (mutate best)]))


                                        ; Instrumentation

;; Initializers

(s/fdef init-state
        :ret :evo-images.evolution/state)

(s/fdef create-creature
        :args (s/cat :tint (s/? (s/coll-of int? :count 4)))  ;; ::color
        :ret  :evo-images.evolution/creature)

;; Mutation

(s/fdef mutate-shape-points
        :args (s/cat :points :evo-images.evolution/points)
        :ret  :evo-images.evolution/points)

(s/fdef mutate-shape-color
        :args (s/coll-of :evo-images.evolution/color :count 1)
        :ret  :evo-images.evolution/color)

(s/fdef mutate-shape
        :args (s/cat :shape :evo-images.evolution/shape)
        :ret  :evo-images.evolution/shape)

(s/fdef mutate
        :args (s/cat :creature :evo-images.evolution/creature)
        :ret  :evo-images.evolution/creature)

;; Survival

(s/fdef compete
        :args (s/cat :max-fitness :evo-images.evolution/fitness
                     :best        :evo-images.evolution/creature
                     :competing   :evo-images.evolution/creature)
        :ret  (s/cat :max-fitness :evo-images.evolution/fitness
                     :best        :evo-images.evolution/creature))

(s/fdef evolve
        :args (s/coll-of :evo-images.evolution/state :count 1)
        :ret  :evo-images.evolution/state)

;; (do (stest/unstrument `mutate-shape-points)
;;     (stest/unstrument `mutate-shape-color)
;;     (stest/unstrument `mutate-shape)
;;     (stest/unstrument `mutate)
;;     (stest/unstrument `compete)
;;     (stest/unstrument `evolve)
;;     (stest/unstrument `create-creature)
;;     (stest/unstrument `init-state))

;; (do (stest/instrument `mutate-shape-points)
;;     (stest/instrument `mutate-shape-color)
;;     (stest/instrument `mutate-shape)
;;     (stest/instrument `mutate)
;;     (stest/instrument `compete)
;;     (stest/instrument `evolve)
;;     (stest/instrument `create-creature)
;;     (stest/instrument `init-state))
