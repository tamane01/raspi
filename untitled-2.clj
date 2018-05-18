(require ['overtone.live :refer :all])

(defn cf [gate] (env-gen (env-asr 0.1 1 3) :gate gate :action FREE))

(pp-node-tree)
(def bus1 (audio-bus))
(defsynth modulator [freq 1 mul 1] (out bus1 (* mul (sin-osc freq))))
(defsynth modulator2 [freq 1 mul 1]  (out bus1 (* mul (saw (+ freq (* freq (sin-osc 0.5)))))))

(defsynth my-saw[freq 440 gate 1] (out [0 1](-> (saw freq)
                                             (svf (+ 440 (* 440 (in bus1))) 0.8)
                                             (* 0.2 (cf gate))

                                                )))
(def as-pre [:tail 5])
(modulator2 5 1 [:tail 5])
(def lfo0 (modulator as-pre 0.1  ))
(def lfo1 (modulator :tail 6))
(def lfo2 (modulator 9 0.3 :tail 6))
(def s1 (my-saw 220 ))
(def s1 (my-saw 330))
(def chord1 [0 3 7 11])
(do (map #(my-saw (midi->hz (+ 60 %)) :tail 7) chord1))
(ctl lfo1 :freq 1)

(stop
  )

(ctl 7
     :gate 0)
