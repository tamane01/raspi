(use 'overtone.live)

(volume 0.4)
;helpers
(defn fader  ([gate attack release] (env-gen (env-asr attack 1 release) :gate gate :action FREE))
             ([gate] (fader gate 0.1 3)))

;synthdef
(defsynth bells [freq 880 fm-ratio 5/3 fm-mag 0.5 vol 0.2 gate 1 release 0.1] 
          (let [burst (impulse:ar (* 16 (+ 0.5 (lf-noise0 20))  ))
                         env (decay:ar burst  release) 
                         f (+ freq (* freq  2 (ti-rand 0 1 burst)))
                         pan (sin-osc 0.5)                                
                         fm-mod (sin-osc (* fm-ratio f))
                ]
                (out [0 1] (-> (sin-osc (+ f (* fm-mag f fm-mod)))
                               (* vol env (fader gate) )
                               (free-verb )
                               (pan2 pan )
                             ))))

(defsynth click [freq 880 res 0.9 vol 0.5]
          (let [pluck (line 1 0 0.01)]
            (out [0 1] (* (svf pluck freq res) vol)))) 

(defsynth click2 [freq 1660 res 0.9 vol 0.3 dur 0.5 lf-f 5]
          (let [lfo (pulse lf-f)
                env (env-gen (env-perc 0.1 dur) :action FREE) ] 
            (out [0 1] (-> lfo 
                           (svf freq res)
                           (* vol env)
                         ))))

(defsynth bleep [freq 440 vol 0.05]
          (out [0 1] (-> (sin-osc freq)
                         (* vol (env-gen (env-adsr-ng 0.00 0 0.3 0.0) :action FREE))))) 

;pad
(defsynth saw-voice [freq 440 vol 0.02 gate 1 res 0.9 cutoff 3 lfo-mag 3  noise-level 0.1
                     lfo-freq 9 lfo-vol 1 fmf 0.1  pan-rate 0.01]
          (let [lfo1 (+ lfo-freq (* lfo-mag lfo-freq (sin-osc fmf)))] 
            (out [0 1 ] (->   (+ (saw (+ (* 0.5 (lf-noise0 1)) freq))
                                 ( saw (+ (* 0.5 (lf-noise0 1)) (* 1.0025  freq))) 
                              (* noise-level (pink-noise)))
                              (svf (+ (* freq cutoff) lfo1 ) res) 
                              (* vol (env-gen (env-asr 30 1 30) :gate gate :action FREE) )
                              (pan2 (sin-osc:kr (* freq pan-rate)))
                          ))))

(defsynth drone1 [freq 110 vol 0.2 lfo-f 0.01 th 0.9 gate 1]
          (let [lfo1 (sin-osc lfo-f )]
           (out [0 1] (-> (sin-osc freq) (fold2 (* (- 1.2 lfo1) th) ) (* vol (fader gate))))))  

(defsynth my-sin [freq 440 vol 0.05 gate 1 pan-lfo 0.05] 
          (out [0 1] (-> (sin-osc freq)
                         (* vol (env-gen (env-asr 10 1 10) :gate  gate :action FREE))
                         (pan2 (sin-osc:kr pan-lfo)))))

(definst fm [base-freq 440 mul 0.2
              mod1-ratio 2/3 mod1-mul 50 
              mod2-ratio 4/5 mod2-mul 20
              mod3-ratio 6/7 mod3-mul 0.2 ] 
   (
                 * mul
                 (env-gen (env-perc  0 9 :curv -4) :action FREE)
                 (sin-osc (+ base-freq (* (mouse-y 0 100)  (sin-osc (* base-freq mod1-ratio))))))) 
(fx)
(defn seq-player [m sq]
 (let [beat (m)] 
   (at beat (fm  (* 220 (choose sq))
                :mod1-ratio (choose sq)))
   (apply-by (m (+ (rand 2)  beat)) #'seq-player [m sq]))) 

(def s0 [1 4/3 5/3 7/3 3/5 4/5 6/5 1/2 2])  
(def s1 (conj (map #(* 2 %) s0)))  
                          
(def p1 (seq-player m s1))

(defonce sins (group "sins"))
(defonce drones (group "drones"))
(defonce pads (group "pads"))
(defonce bellsg (group))

(def s1 (my-sin [:head sins] 1660 0.03))
(def s2 (my-sin [:head sins] 440 0.03))
(def r3 (my-sin [:head sins] 880 0.03))
(ctl sins :pan-lfo 0.1)
(ctl sins :gate 0)
(def d1 (drone1  [:tail drones] (midi->hz 45)  0.2 0.01  0.9 ))
(def d2 (drone1  [:tail drones] (midi->hz 52)  0.2 0.01  0.9 ))
(ctl d1 :vol 0.4 :th 0.8)
(kill d1)
(ctl pad1-group :vol 0.02 )
(ctl pads  :pan-rate 2/300)
(ctl 0 :noise-level 0.9)
(ctl 0 :gate 0)
(ctl 0 :fmf 0.5)
(ctl 0 :lfo-freq 1)
(ctl 0 :res 0.95)
(ctl 0 :cutoff 2 :lfo-mag 3)
(doseq [x [57 69 74 76 78]] (saw-voice [:tail pads] (midi->hz (- x 12))))
(ctl pads :gate 0)
(ctl drones :gate 0)

;sequencer
(defonce m (metronome 76))

(defn looper [m] 
  (let [beat (m)
        next-beat (+ 8  beat)]
    (at (m beat) (click 880 :res 0))
    (at (m (+ 1.5 beat)) (click 1760))
    (at (m (+ 2.5 beat)) (click 1000 :res 0.3 ))
    (at (m (+ 3.0 beat)) (bleep (* 440 24)))
    (at (m (+ 3.5 beat)) (click2))
    (apply-by (m next-beat) #'looper [m])))

(defn looper2 [m]
  (let [beat (m)
        next-beat (+ 2 beat)
        co (rand-nth (range 1 5))]
    (at (m beat) (ctl 0 :cutoff co))
    (apply-by (m next-beat) #'looper2 [m])))

(looper m)
(looper2 m)
(def looper nil)
(def looper2 nil)
(do (map #(saw-voice [:tail post] (midi->hz (+ 53 %))) my-chord))

(bells (midi->hz 69))
(bells (midi->hz 81))
(bells (midi->hz 76))
(bells (midi->hz 93))
(ctl 0 :release 0.08 )
(do (map #(bells (midi->hz %)) (chord :e5 :M7))) 
(map #(bells (+ 440 (* 1000 (rand) % ))) (range 5)) 

(range 10)
(ctl bellsg
     :gate 0)
(my-sin [:tail sins] 880)
(my-sin [:tail sins] (midi->hz (note :E6)))
(my-sin [:tail sins] 440)
(my-sin [:tail sins] 1760)
