(ns user
  (:use [overtone.live]))

(definst my-perc[freq 440 amp 0.2 fm-freq 440] 
  (->
   (sin-osc 
     (-> (sin-osc fm-freq)  (* 400) (+  freq (* 5 (lf-noise1 1))))) 
   (* amp  (env-gen (perc 3 3) 1 1 0 1 FREE))
  )
)


(def m (metronome 120))
(defn looper [nome sound ] 
  (let [beat (nome)]
        (at (nome beat) (sound 
                          (rand-nth @my-seq)
                          0.2 
                          (rand-nth @my-seq))) 
;         (at (nome (+ (rand) beat)) (my-perc2 
;                          (choice (vec @my-seq ))
;                          0.2 
;                          (choice (vec @my-seq)))) 
                         
        (apply-by (nome (inc beat)) #'looper [nome sound] )))
(def my-seq (atom (vec (map (partial * 440) [1 2 3 2/3 3/5 4/5 4/3 1/2]))))

(reset! my-seq [220 440 880 1660])

(reset! my-seq (vec (map (partial * 440) [1 2 3 2/3 3/5 4/5 4/3 1/2 2/3 4/3])))
;(defn looper [] nil)
(looper m my-perc)
(stop)
(m :bpm 30)    

