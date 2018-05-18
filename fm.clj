(ns user  
  (:require [quil.core :as q])
           ; [quil.middleware :as m])
  (:use [overtone.live]))

(def main-group (group "main group"))
(def pre-group (group "pre group" :head main-group))
(def post-group (group "post group" :after main-group ))
(pp-node-tree)



(def pre-fx (audio-bus))
(def main-bus [0 1])

(defsynth fm [base-freq 440 mul 0.03
              mod1-ratio 2/3 mod1-mul 50 
              mod2-ratio 4/5 mod2-mul 20
              mod3-ratio 6/7 mod3-mul 0.2 ] 
   (out pre-fx (
                 * mul
                 (env-gen (env-perc 0.01 9 :curv -1) :action FREE)
                 (sin-osc (+ base-freq (* (mouse-y 0 100)  (sin-osc (* base-freq mod1-ratio)))))))) 
(defsynth redirect [] (out main-bus (in pre-fx)))
(redirect [:tail 8])

(defsynth filt [] (out main-bus (svf (in pre-fx) (+ 880 (* 660 (lf-noise0 8))))))
(filt)
(defsynth wf2 [freq 440 mul 0.1 trig 1]
  (out main-bus (svf (* mul
     (env-gen (asr 6 1 6) :gate trig :action FREE)
      (fold2 (* (+ 2 (* 1 (lf-tri:kr 0.1))) (sin-osc freq)) 1.0)
       ) (mouse-x 110 1660) (mouse-y 0.2 0.95 ))))


(fx-freeverb)
(def s0 [1 4/3 5/3 7/3 3/5 4/5 6/5 1/2 2])  
(def s1 (conj (map #(* 2 %) s0)))  
(def tmp (metronome 60))
(defn seq-player [m sq]
 (let [beat (m)] 
   (at beat (fm [:tail 6] (* 220 (choose sq))
                :mod1-ratio (choose sq)))
   (apply-by (m (+ (rand 2)  beat)) #'seq-player [m sq]))) 
                           
(def p1 (seq-player tmp s1))
(def drone1 (wf2 110 0.05))
(def drone2 (wf2 (* 2/3 220) 0.03))

(def b1 (buffer 48000))
(defsynth rec [buf-name 0 rv 0.5 pv 0.5 input-ch 0]
 (record-buf (in pre-fx) buf-name  0 rv pv))

(defsynth pl [buf-name 0 mul 0.2 trig 1 rate 1] 
  (out [0 1] (* mul
                (env-gen (asr 0 1 9) :gate trig :action FREE)
                (play-buf 1 buf-name :loop 1 :rate rate))))

(defsynth granu [buf-name  0 mul 0.5 rate 1 spread 0.0 trig 1] 
  (out [0 1]
       (* mul
       (env-gen (asr 6 1 9) :gate trig :action FREE)
       (t-grains 
         2 
         (lf-pulse (mouse-y 8 440)) 
         buf-name 
         :rate (+ rate (* spread (lf-noise0))) 
         :center-pos (+ (mouse-x 0 20) (* 1 (+ 1 (lf-noise0)))) 
            
         :dur 0.3 
         :amp 0.6 )))) 
(defsynth fgranu [buf-name 0 mul 0.1]
  (out [0 1] 
       (svf (t-grains 
         2 
         (lf-pulse (mouse-y 8 440)) 
         buf-name 
         :rate 1 
         :center-pos (* 10 (+ 1 (lf-noise0))) 
            
         :dur 0.3 
         :amp 0.05 ) (+ 2200 (* 880 (lf-noise0 16))) 0.9 0 1)))

(defn fo [targ] (ctl targ :trig 0))

(rec b1)
(granu b1 :mul 0.2 :rate 1)
(granu b1 :mul 0.2 :rate 4/3)

;visualizer

(defonce  vb1 (buffer 512))
(defsynth vrec [] (record-buf (in 0) vb1))
(def vr1 (vrec [:tail 8]))

(defn setup []
  (q/frame-rate 30)
  (q/background 0)
  )

(defn draw []
  (q/blend-mode :blend)
  (q/fill 0 20  )
  (q/rect 0 0 500 500)
  (q/stroke (+ 128 (mod (q/frame-count) 128 ))
            (+ 128 (mod (q/frame-count) 129 ))
            (+ 128 (mod (q/frame-count) 120 )))
  (let [data (buffer-data vb1)]
    (doseq [n (range 512)] 
      (let [r (* 120 (nth data n))
            x (+ 250 (* (+ r 150)   (q/sin (/ n 81))))
            y (+ 250 (* (+ r 150)  (q/cos (/ n 81))))]
        (q/point x y))))
  
   (q/fill 0 80)
  (q/no-stroke)
  ;(q/ellipse 250 250 300 300) 
)

(q/defsketch visualize 
  :size [500 500]
  :setup setup
  :draw draw)
