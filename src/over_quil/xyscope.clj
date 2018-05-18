(ns over-quil.xyscope
  (:require [quil.core :as q])
  (:use overtone.live))

(defonce xy-buffer (buffer 512 2))
(defsynth x-osc [freq 440 mul 0.5 ] (out 0 (* mul (sin-osc freq))))
(defsynth y-osc [freq 441 mul 0.5 ] (out 64 (* mul (sin-osc freq :phase 3.14159))))

(def x1 (x-osc))
(def y1 (y-osc))

(defsynth rec-xy [] (record-buf (in [0 64])  xy-buffer)) 
(def rec-xy-1 (rec-xy))
(kill x1)

;visual

(defn setup []
  (q/background 0)
  (q/stroke 255)
  (q/frame-rate 90)
  )

(defn draw []
  (q/fill 0 20)
  (q/no-stroke) 
  (q/rect 0 0 1700 900)
  (q/stroke 100 100 256)
  (q/translate 750 430) 
  (let [data (buffer-data xy-buffer)
        mul 200 ]
    (doseq [n (range 0 1024 2)]
      (let [x (* mul (nth data n))
            y (* mul (nth data (inc n)))
            
                 ]
        (q/point x y)))))
        
(q/defsketch xy-scope
  :size [200 200];:fullscreen
  :setup setup
  :draw draw)


