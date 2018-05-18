(require ['overtone.live :refer :all]
         ['overtone.studio.scope :refer :all])

(defsynth my-burst [] (out [0 1] 
                           (let [lfo (lf-noise0 3)]
                           (-> (impulse:ar (* 10 (+ 1 lfo)))
                                     ;(decay 0.001)
                                     (svf 3520 0.9)        
                             (* 0.2)
                                   ))))
(my-burst)

