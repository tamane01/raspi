(ns user
  (:use [overtone.live]))
(definst acid [freq 440 amp 0.5 f 330 q 0.8]
  (let [env (env-gen (env-perc 0 0.3) 1 1 0 1 FREE)
        filter-sweep (-> (env-gen (env-perc 0.0 0.3)) 
                         (* 200) (+ f))]
    (-> (saw freq)
        (svf filter-sweep q)
        (* env amp))))

(acid 110 0.5 (rcf))
(defn rcf []
  (float (rand-nth (range 220 1660 220))))
(def m (metronome 240))
(defn looper [nome]
  (let [beat (nome) ]
    (at (nome beat) (acid 60 0.6 ))
    (at (nome (+ beat 0.5)) (acid 60 0.6 880 0.85))
    (apply-by (nome (inc beat))  #'looper [nome])))
(looper m) 
(def looper nil)
(m :bpm 140)

