(ns user)
 (def cv-ch)
(def semitone-v 0.04969)
(def v-map (vec(map #(* % semitone-v) (range 50))))



(defsynth my-sin [f 440 mul 0.2 out-ch 0] 
 (->> (sin-osc f) (* mul) (out out-ch)))  
(defsynth cv [os 0.1 ch 7] 
  (out ch (* os (dc:ar 1))))

(defsynth trg [dur 0.2 ch 6] 
  (out ch (* 3.0 (env-gen:ar (env-perc 0 dur) :action FREE ) (dc:ar 1)))) 

(defn cv-gate [p] (do (ctl cv-ch :os (v-map p))  (trg 0.2 6)))   
(cv-gate 32)

(trg 0.2 6 )
;(ctl cv-ch :os 0.3 )
(def foo (my-sin 440 0.2 6))
(ctl foo :f 880) 
