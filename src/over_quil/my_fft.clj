(ns over-quil.my-fft
  (:require [overtone.live :refer :all]
            [quil.core :as q]
            [overtone.studio.scope :refer :all]) )

;sample player
(def my-sample (buffer-alloc-read "resources/wind.wav"))
(defsynth my-sample-player [buf-num 0]
  (out 1 (-> (play-buf 1 buf-num :loop 1)
             (* 0.5))))

(my-sample-player my-sample)
(stop)

;FFT section
(def fft-buf (buffer 1024))
(def fft-buf2 (buffer 1024))
(def fft-display-buf (buffer 1024))
(scope fft-buf)
(defsynth myfft [g 1 mul 0.1] 
  (out 0 (-> (fft fft-buf (play-buf 2 my-sample :loop 1))
             (pv-div (fft fft-buf2 (svf (* 0.3 (+     
                                                      (saw (midi->hz 48))
                                                      (saw (midi->hz 60))
                                                      (saw (midi->hz 63))
                                                      (saw (midi->hz 67))
                                                      (saw (midi->hz 71))
                                                      ))) (+ 220 (* 90 (lf-noise1 0.1)  0.8) )))  
             ;(pv-mag-smear 0.5)
             ;(pv-brick-wall -0.2 )
             ;(pv-copy fft-display-buf)
             ;(pv-rand-comb  0.7 (lf-pulse 5))
             ;(pv-mag-noise)
             (ifft)
             (* mul
                (env-gen (env-asr 3 1 3) g 1 0 1 FREE))
             (free-verb :room 0.9)
             )))
(stop)
(scope)
(def fft-handle (myfft))
;FFT表示
(ctl fft-handle :g 0)
(defn setup [] 
  (q/frame-rate 30)
  (q/background 0)
  (q/stroke 255)
  )

(defn draw []
  ;(q/background 0)
  (q/fill 0 0 0 128)
  (q/rect 0 0 512 512)
  (let [ba (buffer-data fft-buf)]
   
    (loop [i 0] (if (>= i 512)
                   nil
                   (recur 
                   (let [y (+ 512 (* -8 (Math/abs (aget ba i))))]

;(-> (aget ba)  (* 255)  (+ 255)) 
;                                (q/rect 10 10 200 200)
                    (q/stroke 128 128 32)                                
                    (q/line i 512 i y)
                                (inc i)))))))



;(q/defsketch my-fft
;  :size [512 512]
;  :setup setup
;  :draw draw
;  )
