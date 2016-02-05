(ns langton.prerenderer-test
  (:use midje.sweet)
  (:use [langton.prerenderer]))

(def world-1 {:ant {:pos [0 1] :faces :north}
              :grid {0 {0 :grey
                        1 :white}}})
(def world-2 {:ant {:pos [0 0] :faces :north}
              :grid {0 {0 :white
                        1 :grey}
                     1 {0 :grey
                        1 :white}}})

(facts "about turning the world to cells"
  (fact "it returns an array of arrays of renderable cells"
    (world->cells world-1) => [[{:ant :north :color :white}]
                               [{:color :grey}]]
    (world->cells world-2) => [[{:color :grey} {:color :white}]
                               [{:color :white :ant :north} {:color :grey}]]))
