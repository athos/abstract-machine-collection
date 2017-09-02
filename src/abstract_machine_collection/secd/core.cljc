(ns abstract-machine-collection.secd.core
  (:refer-clojure :exclude [push pop replace])
  (:require [clojure.spec.alpha :as s]
            [clojure.core.match :refer [match]]))

(defn- iatom? [x]
  #?(:clj (instance? clojure.lang.IAtom x)
     :cljs (satisfies? IAtom x)))

(s/def ::insns (s/* ::insn))

(s/def ::value
  (s/or :int int?
        :bool boolean?
        :list ::list
        :fn (s/keys :req-un [::body ::env])))

(s/def ::list
  (s/or :nil nil?
        :pair (s/keys :req-un [::car ::cdr])))
(s/def ::car ::value)
(s/def ::cdr ::value)

(s/def ::body ::insns)
(s/def ::env
  (s/coll-of (s/or :args ::list :cell iatom?)))

(s/fdef locate
  :args (s/cat :env ::env :i int? :j int?)
  :ret ::value)

(defn locate [env i j]
  (let [frame (nth env i)
        frame (if (iatom? frame) @frame frame)]
    (:car (nth (iterate :cdr frame) j))))

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

(s/def ::s (s/* ::value))
(s/def ::e ::env)
(s/def ::c ::insns)
(s/def ::d (s/* (s/keys :req-un [(or ::c (and ::s ::e ::c))])))

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
   (-> state (pop n) (push (apply f (take n s))))))

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
      (match insn
        [:nil]
        (-> state (push nil) next)

        [:ldc x]
        (-> state (push x) next)

        [:ld i j]
        (-> state (push (locate e i j)) next)

        [:atom]
        (-> state
            (replace (fn [x] (or (nil? x) (int? x) (boolean? x))))
            next)

        [:null]
        (-> state (replace nil?) next)

        [:car]
        (-> state (replace :car) next)

        [:cdr]
        (-> state (replace :cdr) next)

        [:cons]
        (-> state (replace 2 #(array-map :car %1 :cdr %2)) next)

        [:add]
        (-> state (replace 2 +) next)

        [:sub]
        (-> state (replace 2 -) next)

        [:mul]
        (-> state (replace 2 *) next)

        [:div]
        (-> state (replace 2 quot) next)

        [:eq]
        (-> state
            (replace 2 (fn [x y]
                         (if (and (number? x) (number? y))
                           (== x y)
                           (identical? x y))))
            next)

        [:gt]
        (-> state (replace 2 >) next)

        [:lt]
        (-> state (replace 2 <) next)

        [:gte]
        (-> state (replace 2 >=) next)

        [:lte]
        (-> state (replace 2 <=) next)

        [:sel ct cf]
        (assoc (pop state)
               :c (if (false? (first s)) cf ct)
               :d (cons {:c c} d))

        [:join]
        (let [[{:keys [c]} & d] d]
          (assoc state :c c :d d))

        [:ldf f]
        (next (push state {:body f :env e}))

        [:ap]
        (let [[{:keys [body env]} v & s] s]
          {:s nil :e (cons v env) :c body :d (cons {:s s :e e :c c} d)})

        [:rtn]
        (let [v (first s)
              [{:keys [s e c]} & d] d]
          {:s (cons v s) :e e :c c :d d})

        [:dum]
        (next (update state :e conj (atom nil)))

        [:rap]
        (let [[{:keys [body]} v & s] s]
          (reset! (first e) v)
          {:s nil :e e :c body :d (cons {:s s :e (rest e) :c c} d)})
        ))))

(s/fdef run
  :args (s/cat :code (s/spec ::insns))
  :ret ::value)

(defn run [code]
  (loop [state {:s () :e () :c (seq code) :d ()}]
    (match (step state)
      ({:s _ :e _ :c _ :d _} :as state')
      (recur state')

      ret ret)))
