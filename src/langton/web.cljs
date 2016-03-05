(ns langton.web
  [:require [reagent.core :as r]
            [langton.grid :as grid]
            [langton.ant :as ant]
            [langton.rules :as rules]
            [langton.runner :as runner]])

(enable-console-print!)

(defonce app-state (r/atom {:grid []}))

(defn cell [{:keys [color ant] :as cell} i]
  [:div.cell
   {:key i
    :class (str "cell-"
                (name color)
                (when ant (str " cell-ant cell-ant-" (name ant))))}])

(defn row [row i]
  [:div.row {:key i} (map cell row (range))])

(defn root []
  (let [state @app-state]
    [:div
     [:h1 "Langton's Ant"]
     [:div.grid (map row (:grid state) (range))]]))

(defn render-simulation [steps simulation interval]
  (if (pos? steps)
    (do
      (first simulation) ; realize next step
      (js/setTimeout #(render-simulation (dec steps) (drop 1 simulation) interval)
                     interval))))

(defn run [rules steps interval]
  (println "Running. Rules:" rules "Steps:" steps "Interval:" interval)
  (letfn [(render-fn [prerendered-world]
            (swap! app-state assoc :grid prerendered-world))]
    (render-simulation
      steps
      (runner/run-simulation render-fn {:ant (ant/create)
                                        :grid (grid/create)
                                        :rules (rules/create rules)})
      interval)))

(defn main []
  (println "Loaded")
  (r/render-component
    [root]
    (js/document.getElementById "app")))

(def on-js-reload main)
