(defonce tmp-ch 0)
(defsynth my-sin [freq 440 mul 0.2 lfo-rate 0.01] 
  (out 0
        
         (* mul (sin-osc (-> (lf-noise0 0.5) (* 8) (+ freq))) 
             (sin-osc lfo-rate) 
                )
       ))

(defsynth my-saw [freq 440 mul 0.2 lfo-rate 0.1] 
  (out 0
       (svf  
         (* mul (saw (+ freq (* 1 (lf-noise0 0.5))))  (sin-osc lfo-rate))
         (-> (sin-osc 0.01) (* 300) (+ 440 )) 0.7
         )
       )
  )


(doseq [n [57 60 64 ]] (my-saw (midi->hz n)))
(my-saw 220)
(def a (my-sin 440 0.1 0.2 )) 
(my-sin 770 0.1 0.1)
(my-sin 880 0.1 0.2)
(my-sin 110 0.2 2)       
(stop)
(kill a)
(map #(my-sin %1 0.1 %2) [440 660 880] [0.2 0.5 1])
(a)
hoge

