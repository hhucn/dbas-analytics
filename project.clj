(defproject dbas.analytics "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [reagent "0.7.0"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.3"]
                 [cljs-http "0.1.44"]
                 [lein-doo "0.1.8"]
                 [cljsjs/highcharts "5.0.14-0"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.14"]
            [lein-doo "0.1.8"]]

  :min-lein-version "2.5.0"

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]
   "public/js/test"]

  :resource-paths ["public"]

  :figwheel {:http-server-root "."
             :nrepl-port 7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["public/css"]}

  :doo {:build "test"
        :alias {:default [:phantom]}
        :paths {:phantom "phantomjs"}}

  :cljsbuild {:builds {:app
                       {:source-paths ["src" "env/dev/cljs"]
                        :compiler
                        {:main "dbas.analytics.dev"
                         :output-to "public/js/app.js"
                         :output-dir "public/js/out"
                         :asset-path   "js/out"
                         :source-map true
                         :optimizations :none
                         :pretty-print  true}
                        :figwheel
                        {:on-jsload "dbas.analytics.core/mount-root"
                         :open-urls ["http://localhost:3449/index.html"]}}
                       :release
                       {:source-paths ["src" "env/prod/cljs"]
                        :compiler
                        {:closure-defines {dbas.analytics.charts.dbas-base "https://dbas.cs.uni-duesseldorf.de"}
                         :output-to "public/js/app.js"
                         :output-dir "public/js/release"
                         :asset-path   "js/out"
                         :optimizations :advanced
                         :pretty-print false}}
                       :test
                         {:source-paths ["src" "test" "env/dev/cljs"]
                          :compiler {:main runners.doo
                                     :optimizations :none
                                     :asset-path "public/js/test/out"
                                     :output-dir "public/js/test/out"
                                     :output-to "public/js/test/all-tests.js"}}}}

  :aliases {"package" ["do" "clean" ["cljsbuild" "once" "release"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.8"]
                                  [figwheel-sidecar "0.5.14"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]]}})
