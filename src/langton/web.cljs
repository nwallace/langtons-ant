(ns langton.web
  [:require [reagent.core :as r]
            [datascript.core :as d]
            [posh.core :refer [pull q db-tx pull-tx q-tx after-tx! transact! posh!]]
            [langton.grid :as grid]
            [langton.ant :as ant]
            [langton.rules :as rules]
            [langton.runner :as runner]])

(enable-console-print!)

(def conn (d/create-conn))

(posh! conn)

;(transact! conn [[:db/add 1 :controls/rules "RL"]
;                 [:db/add 1 :controls/steps 53]
;                 [:db/add 1 :controls/interval 100]
;                 [:db/add 2 :grid []]
;                 [:db/add 3 :current-step 0]
;                 [:db/add 4 :running? false]])

(defonce app-state (r/atom {:controls {:rules "RL"
                                       :steps 53
                                       :interval 100}
                            :grid []
                            :current-step 0
                            :running? false}))

(defn render-simulation [steps simulation interval]
  (if (pos? steps)
    (do
      (first simulation) ; realize next step
      (js/setTimeout #(render-simulation (dec steps) (drop 1 simulation) interval)
                     interval))
    (swap! app-state assoc :running? false)))

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
    (swap! app-state assoc :current-step 0 :running? true)
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
  (let [state @app-state
        {{:keys [rules steps interval]} :controls running? :running?} state]
    [:form {:on-submit new-run}
     [:input {:type "text" :pattern "[RL]+" :max-length (count rules/all-colors)
              :disabled running?
              :value rules
              :on-change (state-update [:controls :rules])}]
     [:input {:type "number" :min 0
              :disabled running?
              :value steps
              :on-change (state-update [:controls :steps] js/parseInt)}]
     [:input {:type "number" :min 0
              :disabled running?
              :value interval
              :on-change (state-update [:controls :interval] js/parseInt)}]
     [:input {:type "submit" :disabled running? :value "Go!"}]]))

(defn root []
  (let [state @app-state]
    [:div
     [:h1 "Langton's Ant"]
     [:div.controls (controls)]
     [:div.progress
      [:progress (if (:running? state)
                   {:value (:current-step state)
                    :max (get-in state [:controls :steps])}
                   {:value 1 :max 1})]]
     [:div.grid (map row (:grid state) (range))]]))

(defn main []
  (println "Loaded")
  (r/render-component
    [root]
    (js/document.getElementById "app")))

(def on-js-reload main)
