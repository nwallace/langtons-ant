(ns langton.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [langton.runner :as runner]
            [langton.ant :as ant]
            [langton.grid :as grid]
            [langton.rules :as rules]
            [langton.renderer :as renderer]))

(def cli-options
  [["-i" "--interval INTERVAL" "Interval"
    :default 1000
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be greater than zero"]]
   ["-s" "--steps STEPS" "Steps"
    :default 100
    :parse-fn #(Integer/parseInt %)
    :validate [pos? "Must be greater than zero"]]])

(defn -main [& args]
  (let [opts       (parse-opts args cli-options)
        rule-str   (first (:arguments opts))
        iterations (get-in opts [:options :steps])
        interval   (get-in opts [:options :interval])
        error      (:error opts)]
    (when error (throw (str "Illegal usage:" error)))
    (print (str (char 27) "[2J")) ; clear screen
    (doall
      (take iterations
        (runner/run-simulation (fn [cells]
                                 (Thread/sleep interval)
                                 (renderer/render cells))
                               {:rules (rules/create rule-str)
                                :ant (ant/create)
                                :grid (grid/create)})))))
