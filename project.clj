(defproject abstract-machine-collection "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/core.match "0.3.0-alpha5"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-eftest "0.3.1"]
            [lein-figwheel "0.5.13"]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "abstract-machine-collection.core/on-js-reload"}

                :compiler {:main abstract-machine-collection.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/abstract_machine_collection.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/abstract_machine_collection.js"
                           :main abstract-machine-collection.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :eftest {:report eftest.report.pretty/report}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [orchestra "2017.07.04-1"]
                                  [pinpointer "0.1.0-SNAPSHOT"]]
                   :source-paths ["src" "env"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
