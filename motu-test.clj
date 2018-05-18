(ns user)
(defsynth my-sin [f 440 mul 0.2 out-ch 0] 
 (->> (sin-osc f) (* mul) (out out-ch)))  
(defsynth cv [os 0.1 ch 7] 
  (out ch (* os (dc:ar 1))))

(defsynth trg [dur 0.2 ch 6] 
  (out ch (* 3.0 (env-gen:ar (env-perc 0 dur) :action FREE ) (dc:ar 1)))) 



(def semitone-v 0.04969)
(def v-map (vec(map #(* % semitone-v) (range 50))))
(def bar (cv 0.595))
(trg 0.2 6 )
(ctl bar :os 0.7 )
(def foo (my-sin 440 0.2 6))
(ctl foo :f 880) 
