(ns user)
(definst sin-hord [freq 440]
 (let [modulator (sin-osc 100)
       modulator2 (sin-osc (* 10 modulator))
       env (env-gen (perc 0 0.8) :action FREE)
       f (+ freq (* 1800 env) (* 500 modulator))
       
       ]

   (-> (sin-osc f) (* 0.5 env))))


(def sh (sin-hord 110))
(demo (sin-osc))
