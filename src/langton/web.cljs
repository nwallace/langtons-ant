(ns langton.web
  [:require [reagent.core :as r]
            [langton.grid :as grid]
            [langton.ant :as ant]
            [langton.rules :as rules]
            [langton.runner :as runner]])

(enable-console-print!)

(defonce app-state (r/atom {:controls {:rules "RL"
                                       :steps 53
                                       :interval 100}
                            :grid []
                            :current-step 0}))

(defn render-simulation [steps simulation interval]
  (if (pos? steps)
    (do
      (first simulation) ; realize next step
      (js/setTimeout #(render-simulation (dec steps) (drop 1 simulation) interval)
                     interval))))

(defn run [rules steps interval]
  (println "Running. Rules:" rules "Steps:" steps "Interval:" interval)
  (letfn [(render-fn [prerendered-world]
            (let [current-step (:current-step @app-state)]
              (swap! app-state assoc :grid prerendered-world
                                     :current-step (inc current-step))))]
    (render-simulation
      steps
      (runner/run-simulation render-fn {:ant (ant/create)
                                        :grid (grid/create)
                                        :rules (rules/create rules)})
      interval)))

(defn new-run [event]
  (.preventDefault event)
  (let [{{:keys [rules steps interval]} :controls} @app-state]
    (swap! app-state assoc :current-step 0)
    (run rules steps interval)))

(defn state-update
  ([value-path] (state-update value-path identity))
  ([value-path transform]
   (fn [event]
     (let [new-val (.. event -target -value)]
       (swap! app-state assoc-in value-path (transform new-val))))))

(defn cell [{:keys [color ant] :as cell} i]
  [:div.cell
   {:key i
    :class (str "cell-"
                (name color)
                (when ant (str " cell-ant cell-ant-" (name ant))))}])

(defn row [row i]
  [:div.row {:key i} (map cell row (range))])

(defn controls []
  (let [{{:keys [rules steps interval]} :controls} @app-state]
    [:form {:on-submit new-run}
     [:input {:type "text" :pattern "[RL]+" :max-length (count rules/all-colors)
              :value rules
              :on-change (state-update [:controls :rules])}]
     [:input {:type "number" :min 0
              :value steps
              :on-change (state-update [:controls :steps] js/parseInt)}]
     [:input {:type "number" :min 0
              :value interval
              :on-change (state-update [:controls :interval] js/parseInt)}]
     [:input {:type "submit" :value "Go!"}]]))

(defn root []
  (let [state @app-state]
    [:div
     [:h1 "Langton's Ant"]
     [:div.controls (controls)]
     [:div.progress
      [:progress {:value (:current-step state)
                  :max (get-in state [:controls :steps])}]]
     [:div.grid (map row (:grid state) (range))]]))

(defn main []
  (println "Loaded")
  (r/render-component
    [root]
    (js/document.getElementById "app")))

(def on-js-reload main)
