(use 'overtone.live)

(defsynth click [freq 880 res 0.9 vol 0.5]
          (let [pluck (line 1 0 0.01)]
            (out [0 1] (* (svf pluck freq res) vol)))) 

(defsynth click2 [freq 1660 res 0.9 vol 0.3 dur 0.5 lf-f 5]
          (let [lfo (pulse lf-f)
                env (env-gen (env-perc 0.1 dur) :action FREE) ] 
            (out [0 1] (-> lfo (svf freq res) (* vol env) (free-verb)))))

(defsynth bleep [freq 440 vol 0.2] (out [0 1] (-> (sin-osc freq) (* vol (env-gen (env-adsr-ng 0.00 0 0.0 0.2) :action FREE))))) 
(click 1660)
(bleep 1660)
(def m (metronome 129))

(defn looper [m] 
  (let [beat (m)
        next-beat (+ 4  beat)]
    (at (m beat) (click 100))
    (at (m (+ 1.5 beat)) (click 1660))
    (at (m (+ 2.5 beat)) (click 1000 :res 0.3 ))
    (at (+ 2.0 beat) (bleep 6440))
    (at (+ 3.5 beat) (click2))
    (apply-by (m next-beat) #'looper [m])))

(looper m)

