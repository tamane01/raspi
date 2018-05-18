(ns over-quil.fm  
  (:require [quil.core :as q])
           ; [quil.middleware :as m])
  (:use [overtone.live]))

(def main-group (group "main group"))
(def pre-group (group "pre group" :head main-group))
(def post-group (group "post group" :after main-group ))

(def pre-fx (audio-bus))
(def main-bus [0 1])

(pp-node-tree)


(definst fm [base-freq 440 mul 0.5
              mod1-ratio 2/3 mod1-mul 50 
              mod2-ratio 4/5 mod2-mul 20
              mod3-ratio 6/7 mod3-mul 0.2 ] 
   (
                 * mul
                 (env-gen (env-perc 0.01 9 :curv -1) :action FREE)
                 (sin-osc (+ base-freq (* (mouse-y 0 100)  (sin-osc (* base-freq mod1-ratio))))))) 


(defsynth wf2 [freq 440 mul 0.1 trig 1]
  (out main-bus (svf (* mul
     (env-gen (asr 6 1 6) :gate trig :action FREE)
      (fold2 (* (+ 2 (* 1 (lf-tri:kr 0.1))) (sin-osc freq)) 1.0)
       ) (mouse-x 110 1660) (mouse-y 0.2 0.95 ))))


(defsynth rec [buf-name 0 rv 0.5 pv 0.5 input-ch 0]
 (record-buf (in input-ch) buf-name  0 rv pv))
(defsynth rec-aux  [buf-name 0 rv 0.5 pv 0.5]
 (record-buf (sound-in 0) buf-name  0 rv pv))


(defsynth pl [buf-name 0 mul 0.2 trig 1 rate 1] 
  (out [0 1] (* mul
                (env-gen (asr 0 1 9) :gate trig :action FREE)
                (play-buf 1 buf-name :loop 1 :rate rate))))


(defsynth granu [buf-name  0 mul 0.5 rate 1 spread 0.0  wobble 0 trig 1] 
  (out [0 1]
       (* mul
       (env-gen (asr 6 1 9) :gate trig :action FREE)
       (t-grains 
         2 
         (lf-pulse (mouse-y 8 440)) 
         buf-name 
         :rate (+ rate (* wobble (lf-noise0))) 
         :center-pos (+ (mouse-x 0 20) (* 1 (+ spread (lf-noise0)))) 
            
         :dur 0.3 
         :amp 0.6 )))) 

(defsynth redirect [input-ch 0  mul 0.3 trig 1]
  (out main-bus
       (* mul 
          (env-gen (asr 6 1 6) :gate trig :action FREE)
          (in input-ch))))



(def rd1 (redirect [:tail 8] pre-fx))

(defsynth aux-in [input-ch 4 mul 1]
  (out main-bus (* mul (sound-in input-ch))))

(def rd-from-aux (aux-in 4))
;(defsynth filt [] (out main-bus (svf (in pre-fx) (+ 880 (* 660 (lf-noise0 8))))))
;(filt)


(def s0 [1 4/3 5/3 7/3 3/5 4/5 6/5 1/2 2])  
(def s1 (conj (map #(* 2 %) s0)))  
(def tmp (metronome 60))

(defn seq-player [m sq]
 (let [beat (m)] 
   (at beat (fm  (* 220 (choose sq))
                :mod1-ratio (choose sq)))
   (apply-by (m (+ (rand 2)  beat)) #'seq-player [m sq]))) 
                           
(def p1 (seq-player tmp s1))
(def drone1 (wf2 110 0.05))
(def drone2 (wf2 (* 2/3 220) 0.03))

(def b1 (buffer 48000))
(def b2 (buffer 960000))

;(def r1(rec [:tail 7] b1 :input-ch pre-fx))
;(def r2(rec-aux [:tail 7] b2 ))

(defsynth pl [buf-name 0 mul 0.2 trig 1 rate 1]
      (out [0 1] (* mul
      (env-gen (asr 0 1 9) :gate trig :action FREE)
      (play-buf 1 buf-name :loop 1 :rate rate))))

(def p1 (pl b2))
(def g1 (granu b1 [:tail 8] :mul 0.3 :rate 1))
(def g2 (granu b1 [:tail 8] :mul 0.3 :rate 4/3))

(defn fadeout [] (ctl 0 :trig 0))
(fadeout) 
(def b4 (buffer 500))
(def r4 (rec [:tail 7] b4 :input-ch 0))

(defn setup []
 (q/frame-rate 30)
 (q/background 0)
 (q/stroke 255) 
  )

(defn draw []
  (let [data (buffer-data b4)]
   (doseq [x (range 500)] (q/point x
                                   (+ 250 (* 200 (nth data x)))))
   ))

(q/defsketch foo
  :size [500 500]
  :setup setup
  :draw draw
)
;(fx-bitcrusher)
