(ns evo-images.evolution
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
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

(s/def ::fitness (s/and int? #(>= % 0)))
(s/def ::iteration (s/and int? #(>= % 0)))
(s/def ::state (s/cat :iteration ::iteration
                      :max-fitness ::fitness
                      :best ::creature
                      :competing ::creature))


(s/fdef mutate
        :args ::creature
        :ret  ::creature)

(s/fdef compete
        :args (s/cat :max-fitness ::fitness :best ::creature :competing ::creature)
        :ret  (s/cat :max-fitness ::fitness :best ::creature))

(s/fdef evolve
        :args ::state
        :ret  ::state)

(s/fdef initial-state
        :ret ::state)


;; And the actual implementation...

(defn- mutate [creature]
  creature)

(defn- compete [max-fitness best competing]
  (let [competing-fitness (compute-fitness competing)]
    (if (< competing-fitness max-fitness)
      [competing-fitness competing]
      [max-fitness best])))

(defn evolve [state]
  (let [[iteration max-fitness best competing] state
        [max-fitness best] (compete max-fitness best competing)]
    [(inc iteration)
     max-fitness
     best
     (mutate best)])
  )

;; (defn evolve [state]
;;   state)

(defn create-creature []  ;; it'd be awesome to transform it into a gen
  ;; the custom generators in https://clojure.org/guides/spec story
  ;; (into [] (take max-shapes (repeatedly #(gen/generate (s/gen ::shape)))))
  (gen/generate (s/gen ::creature)))

(defn init-state []
  (let [initial-creature (create-creature)]
    [0 0 initial-creature initial-creature]))
