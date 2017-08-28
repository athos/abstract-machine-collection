(ns abstract-machine-collection.secd-test
  (:require [abstract-machine-collection.secd :as secd]
            [clojure.test :refer [deftest is are use-fixtures]]
            [orchestra.spec.test :as st]))

(def +names-defined+
  `#{secd/step secd/run})

(use-fixtures :once
  (fn [f]
    (st/instrument +names-defined+)
    (f)
    (st/unstrument +names-defined+)))

(deftest step-test
  (are [state state'] (= state' (secd/step state))
    '{:s (42) :e () :c () :d ()}
    42

    '{:s (42) :e () :c () :d ({:s (100) :e () :c ([:rtn])})}
    '{:s (42 100) :e () :c ([:rtn]) :d nil}

    '{:s () :e () :c ([:ldc 42]) :d ()}
    '{:s (42) :e () :c nil :d ()}

    '{:s ()
      :e ({:car 42 :cdr nil} {:car 43 :cdr nil})
      :c ([:ld 1 0])
      :d ()}
    '{:s (43) :e ({:car 42 :cdr nil} {:car 43 :cdr nil}) :c nil :d ()}

    '{:s (42) :e () :c ([:atom]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s ({:car 42 :cdr nil}) :e () :c ([:atom]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s (nil) :e () :c ([:null]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s ({:car 42 :cdr nil}) :e () :c ([:null]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s ({:car 42 :cdr 43}) :e () :c ([:car]) :d ()}
    '{:s (42) :e () :c nil :d ()}

    '{:s ({:car 42 :cdr 43}) :e () :c ([:cdr]) :d ()}
    '{:s (43) :e () :c nil :d ()}

    '{:s (42 43) :e () :c ([:cons]) :d ()}
    '{:s ({:car 42 :cdr 43}) :e () :c nil :d ()}

    '{:s (1 2) :e () :c ([:add]) :d ()}
    '{:s (3) :e () :c nil :d ()}

    '{:s (2 1) :e () :c ([:sub]) :d ()}
    '{:s (1) :e () :c nil :d ()}

    '{:s (2 3) :e () :c ([:mul]) :d ()}
    '{:s (6) :e () :c nil :d ()}

    '{:s (7 3) :e () :c ([:div]) :d ()}
    '{:s (2) :e () :c nil :d ()}

    '{:s (nil nil) :e () :c ([:eq]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s ({:car 42 :cdr nil} {:car 42 :cdr nil}) :e () :c ([:eq]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s (2 1) :e () :c ([:gt]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s (1 2) :e () :c ([:gt]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s (1 2) :e () :c ([:lt]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s (2 1) :e () :c ([:lt]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s (2 1) :e () :c ([:gte]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s (1 2) :e () :c ([:gte]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s (1 2) :e () :c ([:lte]) :d ()}
    '{:s (true) :e () :c nil :d ()}

    '{:s (2 1) :e () :c ([:lte]) :d ()}
    '{:s (false) :e () :c nil :d ()}

    '{:s (true)
      :e ()
      :c ([:sel
           ([:ldc true] [:join])
           ([:nil] [:join])]
          [:null])
      :d ()}
    '{:s () :e () :c ([:ldc true] [:join]) :d ({:c ([:null])})}

    '{:s (nil) :e () :c ([:join]) :d ({:c ([:null])})}
    '{:s (nil) :e () :c ([:null]) :d nil}

    '{:s () :e ({:car 42 :cdr nil}) :c ([:ldf ([:ld 0 0] [:rtn])]) :d ()}
    '{:s ({:body ([:ld 0 0] [:rtn]) :env ({:car 42 :cdr nil})})
      :e ({:car 42 :cdr nil})
      :c nil
      :d ()}

    '{:s ({:body ([:ld 0 0] [:rtn]) :env ()} {:car 42 :cdr nil})
      :e ({:car 43 :cdr nil})
      :c ([:ap] [:rtn])
      :d ()}
    '{:s nil
      :e ({:car 42 :cdr nil})
      :c ([:ld 0 0] [:rtn])
      :d ({:s nil :e ({:car 43 :cdr nil}) :c ([:rtn])})}

    '{:s (42)
      :e ({:car 42 :cdr nil})
      :c ([:rtn])
      :d ({:s (43) :e ({:car 43 :cdr nil}) :c ([:null])})}
    '{:s (42 43) :e ({:car 43 :cdr nil}) :c ([:null]) :d nil}))
