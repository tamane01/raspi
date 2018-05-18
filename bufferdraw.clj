(ns over-quil.fm  
  (:require [quil.core :as q])
  (:use overtone.live))

(defn setup []
 (q/frame-rate 30)
 ;(q/background 0)
  (q/background 0)
 ) 

(defn draw[] 
  (q/stroke 255)
  ;(q/rect 10 10 100 100)
  (let [r (* 16 (buffer-get b1 0)) ] 
    (q/ellipse (- 200 r) (- 200 r) (+ 200 r) (+ 200 r) )
 ))

(q/defsketch fm 
  :size [400 400]
  :setup setup
  
  :draw draw
)
(definst my-sin [] (* 0.2 (sin-osc 440)))
(my-sin)

(def b1 (buffer 440000))
(defsynth rec [buf-name 0]
  (record-buf 0 buf-name))

;(def r1 (rec b1))


