(ns abstract-machine-collection.secd
  (:require [clojure.spec.alpha :as s]))

(s/def ::value any?)
(s/def ::env (s/coll-of (s/coll-of ::value :kind vector?)))

(s/fdef locate
  :args (s/cat :env ::env :i int? :j int?)
  :ret ::value)

(defn locate [env i j]
  (-> env (nth i) (nth j)))

(s/def ::insn
  (s/or :nil  (s/cat :op #{:nil})
        :ldc  (s/cat :op #{:ldc} :x ::value)
        :ld   (s/cat :op #{:ld} :i int? :j int?)

        :atom (s/cat :op #{:atom})
        :null (s/cat :op #{:null})
        :car  (s/cat :op #{:car})
        :cdr  (s/cat :op #{:cdr})

        :cons (s/cat :op #{:cons})
        :add  (s/cat :op #{:add})
        :sub  (s/cat :op #{:sub})
        :mul  (s/cat :op #{:mul})
        :div  (s/cat :op #{:div})
        :eq   (s/cat :op #{:eq})
        :gt   (s/cat :op #{:gt})
        :lt   (s/cat :op #{:lt})
        :gte  (s/cat :op #{:gte})
        :lte  (s/cat :op #{:lte})

        :sel  (s/cat :op #{:sel} :ct ::insns :cf ::insns)
        :join (s/cat :op #{:join})

        :ldf  (s/cat :op #{:ldf} :f ::insns)
        :ap   (s/cat :op #{:ap})
        :rtn  (s/cat :op #{:rtn})

        :dum  (s/cat :op #{:dum})
        :rap  (s/cat :op #{:rap})
        ))

(s/def ::insns (s/* ::insn))
