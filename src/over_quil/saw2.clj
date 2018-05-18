(ns over-quil.saw2
  (:use [overtone.live]))

(defonce tmp-ch 0)

(definst bouncing [freq 440 mul 0.2 dur 2.5]
  (let [
        env (env-gen (perc 0.01 dur 1 -1) :action FREE)
        pulse (lf-pulse (+ 0 (* 20 (- 1 env)))) 
        env-s  (env-gen (perc 0.01 0.05) pulse)
        fm-modulator (sin-osc (* freq 2))
        ]
    (-> (sin-osc (-> freq (+ (* 100 fm-modulator))) ) (* env env-s  mul)))) 
(def bouncing nil)

(definst chime [freq 440 mul 0.2 dur 3.0]
  (let [
        env (env-gen (perc 0.01 0.1 1 -5) :action FREE)
        ]
    (-> (sin-osc freq) (* mul env))))
(chime 440)
;(bouncing 3220 0.2 3)
;(bouncing 1660 0.2 5)
;(bouncing 2200 0.2 1)

(def m (metronome 120))
(defn chime-looper [] 
  (let [maxf 12000 
        minf 50
        max-dur 5 
        min-dur 0.2
        ;now (m (m)) 
        n (now)
        dur (-> (rand) (* max-dur) (+ min-dur)) 
        next-point (+ n (* 100 dur) ) ;(m (inc (m)))
        f (-> (rand) (* (- maxf minf)) (+ minf))
        vol (-> (rand 0.1) ) ]

   (at n (chime f vol))
   (apply-by next-point #'chime-looper )
))
(chime-looper )
(def chime-looper nil)
(defn pingpong-looper [m] 
  (let [maxf 6420
        minf 220
        max-dur 5 
        min-dur 1
        ;now (m (m)) 
        n (now)
        dur (-> (rand) (* max-dur) (+ min-dur)) 
        next-point (+ n (* 500 dur) ) ;(m (inc (m)))
        f (-> (rand) (* (- maxf minf)) (+ minf))
        vol (-> (rand 0.2) ) ]

   (at n (bouncing f vol dur)
   (apply-by next-point #'pingpong-looper [m])
)))
;(def pingpong-looper nil)
(def pp (pingpong-looper m ))

(defsynth my-sin [freq 440 mul 0.2 lfo-rate 0.01] 
  (out 0
        
         (* mul (sin-osc (-> (lf-noise1 0.5) (* 8) (+ freq))) 
             (sin-osc lfo-rate) 
                )
       ))

(definst my-saw [freq 440 mul 0.1 lfo-rate 0.1 trig 1] 
       (svf  
         (* mul
            (env-gen (asr 0 1 3) 1 1 0 1 FREE) 
            (+ 
              (saw (+ freq (* 1 (lf-noise0 0.5))))
              (saw (+ freq (* 1 (lf-noise0 0.5)))))
            (sin-osc lfo-rate))
         (-> (sin-osc 0.01) (* 300) (+ 400 )) 0.85
         )
       )

;(doseq [n [57 60 64 ]] (my-saw (midi->hz n)))

(def handles-1 (vec (reduce #(cons (my-saw (midi->hz %2)) %1 ) []  [20 57 60 64]))) 
;(def handles-2 (map #(my-saw (midi->hz %)) [57 60 64]))
;(doall  handles-2)
(kill handles-1)
(class handles-2)
(ctl handles-2 :amp 0)
;(def a (my-sin 440 0.1 0.2 )) 
;(my-sin 770 0.1 0.1)
;(my-sin 880 0.1 0.2)
;(my-sin 110 0.1 2)       
(stop)
(map #(my-sin %1 0.1 %2) [440 660 880] [0.2 0.5 1])

(map kill sins)
(def foo (my-sin))
(kill foo)

(fx-freeverb 0 0.5 100 50 )
