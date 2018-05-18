(use 'overtone.live)
(defsynth foo [freq 440 vol 0.2 gate 1]
          (let [lf (+ freq (* 100 (sin-osc:kr 10)))]
            (out [0 1] (-> (sin-osc lf)
                           (* (env-gen (env-asr 1 1 3) :gate gate :action FREE) vol)))))

(defsynth bar [freq 440 vol 0.2 gate 1 lf2-rate 1]
          (let [
                lfo2 (+ 200 (* 400 (lf-noise0 lf2-rate)))
                lfo1 (+ lfo2 (* 200 (sin-osc (* freq 1.2))))
                ]
            (out [0 1] (-> (sin-osc lfo1)
                           (* vol (env-gen (env-asr 0.1 1 3) :gate gate :action FREE))))))

(bar)
(ctl 0 :lf2-rate 50)


(def f1 (foo))
(def f2 (foo 660))
(ctl 0 :gate 0)
