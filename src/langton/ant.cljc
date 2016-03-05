(ns langton.ant)

(def directions [:north :west :south :east])

(defn- next-pos [[x y] faces]
  (condp = faces
    :north [     x  (inc y)]
    :west  [(dec x)      y ]
    :south [     x  (dec y)]
    :east  [(inc x)      y ]))

(defn- index-of [coll item]
  #? (:clj (.indexOf coll item)
      :cljs (loop [c coll i 0]
              (cond
                (empty? c) -1
                (= item (first c)) i
                :else (recur (drop 1 c) (inc i))))))

(defn create [& {:keys [pos faces]
                 :or {pos [0 0] faces :west}}]
  {:pos pos :faces faces})

(defn turn [{:keys [faces] :as ant} left-or-right]
  {:pre [(left-or-right #{:left :right})]}
  (let [current-index (index-of directions faces)
        inc-or-dec (left-or-right {:left inc :right dec})
        next-index (mod (inc-or-dec current-index) (count directions))]
    (assoc ant :faces (get directions next-index))))

(defn step [{:keys [pos faces] :as ant}]
  (assoc ant :pos (next-pos pos faces)))
