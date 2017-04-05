(ns evo-images.evolution
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]
            [evo-images.drawing :refer [compute-fitness]]))

(def max-shapes 50)
(def max-points 6)

;; Specs

(s/def ::x (s/int-in 0 200))
(s/def ::y (s/int-in 0 200))
(s/def ::point (s/keys :req-un [::x ::y]))
(s/def ::points (s/coll-of ::point :kind vector? :count max-points))
(s/def ::color (s/cat :R (s/int-in 0 256)
                      :G (s/int-in 0 256)
                      :B (s/int-in 0 256)
                      :A (s/int-in 0 256)))
(s/def ::shape (s/keys :req-un [::color ::points]))
(s/def ::creature (s/coll-of ::shape :kind vector? :count 50))

(s/def ::fitness (s/double-in 0 100))
(s/def ::iteration (s/and int? #(>= % 0)))
(s/def ::state (s/cat :iteration ::iteration
                      :max-fitness ::fitness
                      :best ::creature
                      :competing ::creature))


;; And the actual implementation...
;; --------------------------------

                                        ; Mutation

(s/fdef mutate-shape-points
        :args (s/cat :points ::points)
        :ret  ::points)

(s/fdef mutate-shape-color
        :args (s/cat :color (s/coll-of int? :count 4))
        :ret  ::color)

(s/fdef mutate-shape
        :args (s/cat :shape ::shape)
        :ret  ::shape)

(s/fdef mutate
        :args (s/cat :creature ::creature)
        :ret  ::creature)

(defn mutate-shape-points
  "Mutates just one point of the list"
  [points]
  (assoc points (rand-int max-points) (gen/generate (s/gen ::point))))

(defn mutate-shape-color [color]
  (let [roulette (rand)]
    (cond
      (< roulette 0.2) (gen/generate (s/gen ::color))
      :else (assoc (into [] color) (rand-int 4) (rand-int 256)))))

(defn- mutate-shape [shape]
  (let [roulette (rand)]
    (cond
      (< roulette 0.1)  (gen/generate (s/gen ::shape)) ; Change the whole shape for a new one
      (< roulette 0.55) (update shape :color mutate-shape-color)
      :else             (update shape :points mutate-shape-points))))

(defn- mutate [creature]
  (update creature (rand-int max-shapes) mutate-shape))


                                        ; Evolution

(s/fdef compete
        :args (s/cat :max-fitness ::fitness :best ::creature :competing ::creature)
        :ret  (s/cat :max-fitness ::fitness :best ::creature))

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


                                        ; Initializers

(s/fdef init-state
        :ret ::state)

(defn create-creature []  ;; it'd be awesome to transform it into a gen
  ;; the custom generators in https://clojure.org/guides/spec story
  ;; (into [] (take max-shapes (repeatedly #(gen/generate (s/gen ::shape)))))
  (gen/generate (s/gen ::creature)))

(defn init-state []
  (let [initial-creature (create-creature)]
    [0 0.0 initial-creature initial-creature]))


;; Instrumentation

(do (stest/unstrument `mutate-shape-points)
    (stest/unstrument `mutate-shape-color)
    (stest/unstrument `mutate-shape)
    (stest/unstrument `mutate)
    (stest/unstrument `compete)
    ;; (stest/unstrument `evolve)
    (stest/unstrument `init-state))

(do (stest/instrument `mutate-shape-points)
    (stest/instrument `mutate-shape-color)
    (stest/instrument `mutate-shape)
    (stest/instrument `mutate)
    (stest/instrument `compete)
    ;; (stest/instrument `evolve)
    (stest/instrument `init-state))
