(ns abstract-machine-collection.secd
  (:refer-clojure :exclude [push pop replace])
  (:require [clojure.spec.alpha :as s]
            [lambdaisland.uniontypes :as union]))

(s/def ::value
  (s/or :nil nil?
        :int int?
        :bool boolean?
        :list (s/keys :req-un [::car ::cdr])
        :fn (s/keys :req-un [::body ::env])))
(s/def ::car ::value)
(s/def ::cdr ::value)
(s/def ::body ::insns)
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

        :sel  (s/cat :op #{:sel}
                     :ct (s/spec ::insns)
                     :cf (s/spec ::insns))
        :join (s/cat :op #{:join})

        :ldf  (s/cat :op #{:ldf} :f (s/spec ::insns))
        :ap   (s/cat :op #{:ap})
        :rtn  (s/cat :op #{:rtn})

        :dum  (s/cat :op #{:dum})
        :rap  (s/cat :op #{:rap})
        ))

(s/def ::insns (s/* ::insn))

(s/def ::s (s/* ::value))
(s/def ::e ::env)
(s/def ::c ::insns)
(s/def ::d (s/* (s/keys :req-un [::c] :opt-un [::s ::e])))

(s/def ::state (s/keys :req-un [::s ::e ::c ::d]))

(s/fdef push
  :args (s/cat :state ::state :x ::value)
  :ret ::state)

(defn- push [state x]
  (update state :s conj x))

(s/fdef pop
  :args (s/cat :state ::state :n (s/? int?))
  :ret ::state)

(defn- pop [state & [n]]
  (let [n (or n 1)]
    (update state :s #(drop n %))))

(s/fdef replace
  :args (s/cat :state ::state :n (s/? int?) :f ifn?)
  :ret ::state)

(defn- replace
  ([state f] (replace state 1 f))
  ([{:keys [s] :as state} n f]
   (-> state (pop n) (push (apply f (reverse (take n s)))))))

(s/def ::ret (s/or :state ::state :value ::value))

(s/fdef step
  :args (s/cat :state ::state)
  :ret ::ret)

(defn step [{:keys [s e c d] :as state}]
  (if (empty? c)
    (if (empty? d)
      (first s)
      (let [[state' & d] d]
        (-> state'
            (update :s conj (first s))
            (assoc :d d))))
    (let [[insn & c] c, next #(assoc % :c c)]
      (union/case-of ::insn insn
        :nil _
        (-> state (push nil) next)

        :ldc _
        (let [[_ x] insn]
          (-> state (push x) next))

        :ld {:keys [i j]}
        (-> state (push (locate e i j)) next)

        :atom _
        (-> state (replace (complement seq?)) next)

        :null _
        (-> state (replace nil?) next)

        :car _
        (-> state (replace :car) next)

        :cdr _
        (-> state (replace :cdr) next)

        :cons _
        (-> state (replace 2 #(array-map :car %1 :cdr %2)) next)

        :add _
        (-> state (replace 2 +) next)

        :sub _
        (-> state (replace 2 -) next)

        :mul _
        (-> state (replace 2 *) next)

        :div _
        (-> state (replace 2 quot) next)

        :eq _
        (-> state (replace 2 =) next)

        :gt _
        (-> state (replace 2 >) next)

        :lt _
        (-> state (replace 2 <) next)

        :gte _
        (-> state (replace 2 >=) next)

        :lte _
        (-> state (replace 2 <=) next)

        :sel _
        (let [[_ ct cf] insn]
          (assoc (pop state)
                 :c (if (false? (first s)) cf ct)
                 :d (cons {:c c} d)))

        :join _
        (let [[{:keys [c]} & d] d]
          (next (assoc state :c c :d d)))

        :ldf _
        (let [[_ f] insn]
          (next (push state {:body f :env e})))

        :ap _
        (let [[{:keys [body env]} v & s] s]
          (-> state
              (assoc :s nil
                     :e (cons [v] env)
                     :c body
                     :d (cons {:s s :e e :c c} d))))

        :rtn _
        (let [v (first s)
              [{:keys [s e c]} & d] d]
          {:s (cons v s) :e e :c c :d d})

        :dum _
        nil

        :rap _
        nil
        ))))

(s/fdef run
  :args (s/cat :code (s/spec ::insns))
  :ret ::value)

(defn run [code]
  (loop [state {:s () :e () :c (seq code) :d ()}]
    (let [ret (step state)]
      (union/case-of ::ret ret
        :state _
        (recur ret)

        :value _
        ret))))
