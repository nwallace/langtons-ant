(defproject langton "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [reagent "0.6.0-alpha"]
                 [clojure-term-colors "0.1.0-SNAPSHOT"]]

  :profiles {:dev {:dependencies [[midje "1.8.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-figwheel "0.5.0-6"]]}}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                ;; If no code is to be run, set :figwheel true for continued automagical reloading
                :figwheel {:on-jsload "langton.web/on-js-reload"}
                :compiler {:main langton.web
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/langton.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}

  :main langton.core)
