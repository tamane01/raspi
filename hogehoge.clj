(use 'overtone.live)
(defn fader [gate] (env-gen (env-asr 0.1 1 3) :gate gate :action FREE))

(def pitch-class [[1 2 4 5 6 8] [1 1/2 1/3 1/4 1/5 1/6]])  
(defsynth my-sin [freq 440 mul 0.2 gate 1] (out [0 1] (-> (sin-osc freq) 
                                                   (* mul (fader gate)))))

(second pitch-class)
(do (map #(my-sin (* 440 %1) (* 0.2  %2) (first pitch-class) (second pitch-class))))
(def s1 (my-sin 440))
(stop)
(ctl 0 :gate 0)

  (foo)
