(ns evo-images.specs
  (:require [clojure.spec :as s]))

                                        ; Global Vars

(def max-shapes 50)
(def max-points 6)

                                        ; Evolution

(s/def :evo-images.evolution/x (s/int-in 0 200))
(s/def :evo-images.evolution/y (s/int-in 0 200))
(s/def :evo-images.evolution/point (s/keys :req-un [:evo-images.evolution/x :evo-images.evolution/y]))
(s/def :evo-images.evolution/points (s/coll-of :evo-images.evolution/point :kind vector? :count max-points))
(s/def :evo-images.evolution/color (s/cat :R (s/int-in 0 256)
                      :G (s/int-in 0 256)
                      :B (s/int-in 0 256)
                      :A (s/int-in 0 256)))
(s/def :evo-images.evolution/shape (s/keys :req-un [:evo-images.evolution/color :evo-images.evolution/points]))
(s/def :evo-images.evolution/creature (s/coll-of :evo-images.evolution/shape :kind vector? :count 50))

(s/def :evo-images.evolution/fitness (s/double-in 0 100))
(s/def :evo-images.evolution/iteration (s/and int? #(>= % 0)))
(s/def :evo-images.evolution/state (s/cat :iteration :evo-images.evolution/iteration
                                          :max-fitness :evo-images.evolution/fitness
                                          :best :evo-images.evolution/creature
                                          :competing :evo-images.evolution/creature))
