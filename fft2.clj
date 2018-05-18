(use 'overtone.live)
(defsynth foo [gate 0 vol 0.5]
          (let [b1 (local-buf 512)]
            (out [0 1] (-> (fft b1 (sound-in 1))
                           (pv-mag-freeze gate)
                           (ifft)
                           (* vol)
                         ))))
(def fx-bus (audio-bus))
(defsynth bar  [gate 0 vol 0.1] 
          (let [b1 (local-buf 512)
                b2 (local-buf 512)
                atm (-> (fft b1 (sound-in 1)) (pv-mag-freeze gate)) ]
            (out [0 1] (-> (fft b2 (in fx-bus)) (pv-mul b1) (ifft) (* vol))))) 
            
(def f1 (foo))
(def f2 (bar))
(ctl f2 :vol 0.8)
(ctl f2 :gate 0)
(ctl f2 :gate 1)
(kill f2)
(defsynth bells [freq 880 fm-ratio 5/3 fm-mag 0.5] (let [burst (pulse (+ 1 (* 5 (lf-noise0 2)) 0.1 ))
                         env (decay2 burst 0.01 0.05) 
                         f (+ freq (* freq  2 (ti-rand 0 1 burst)))
                         pan (sin-osc 0.5)                                
                         fm-mod (sin-osc (* fm-ratio f))]
                                      (out [0 1] (free-verb (pan2 pan (* 0.1 burst (sin-osc (+ f (* fm-mag f fm-mod)))))))))

(bells)
(ctl 0 :freq 16600 :fm-ratio 7/3 :fm-mag 0.2)
(bells :freq 660 :fm-ratio 5/7 :fm-mag 0.5)
(scope fx-bus)
