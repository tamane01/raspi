(ns over-quil.visualizer
  (:require [quil.core :as q])
  (:use [overtone.live]))

(defonce vb1 (buffer 512))
(defsynth recv2 [] (record-buf (in 0) vb1))
(def vr1 (recv2 [:tail 8]))

(defn setup []
  (q/background 0)
  (q/frame-rate 30)
  )

(defn draw []
  (q/fill 0 50)
  (q/no-stroke)
  (q/rect 0 0 1600 800)
  (q/translate 0 450 )
  (q/stroke 255)
  (let [data (buffer-data vb1)
        
        div 64
        mul 64 ]
      
        (doseq [n (range div)]
            (let [x (* mul  n)
                  y (* 1200 (nth data (* n 4))) 
                   
                  r 10]
              (q/ellipse x y r r)
              ;(q/line x y x (+ (* 10 r) y))
              ))))

(q/defsketch foo
  :size :fullscreen 
  :setup setup
  :draw draw)
