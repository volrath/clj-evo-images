(ns evo-images.specs
  (:require [clojure.spec :as s]))

                                        ; Global Vars

(def max-shapes 50)
(def max-points 6)

                                        ; Evolution

(s/def ::x (s/int-in 0 200))
(s/def ::y (s/int-in 0 200))
(s/def ::point (s/keys :req-un [::x ::y]))
(s/def ::polygon (s/coll-of ::point :kind vector? :count max-points))
(s/def ::color (s/cat :R (s/int-in 0 256)
                      :G (s/int-in 0 256)
                      :B (s/int-in 0 256)
                      :A (s/int-in 0 256)))
(s/def ::shape (s/keys :req-un [::color ::polygon]))
(s/def ::creature (s/coll-of ::shape :kind vector? :count max-shapes))

(s/def ::iteration (s/or :zero zero? :pos-int pos-int?))
(s/def ::mutation (s/or :zero zero? :pos-int pos-int?))
(s/def ::fitness (s/double-in 0 100))
(s/def ::state (s/cat :iteration    ::iteration
                      :improvements ::mutation
                      :max-fitness  ::fitness
                      :best         ::creature
                      :competing    ::creature))
