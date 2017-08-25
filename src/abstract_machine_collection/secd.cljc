(ns abstract-machine-collection.secd
  (:require [clojure.spec.alpha :as s]))

(s/def ::value any?)
(s/def ::env (s/coll-of (s/coll-of ::value :kind vector?)))

(s/fdef locate
  :args (s/cat :env ::env :i int? :j int?)
  :ret ::value)

(defn locate [env i j]
  (-> env (nth i) (nth j)))
