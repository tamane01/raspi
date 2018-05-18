(ns user
  (:require [overtone.live :refer :all]
            [overtone.studio.scope :refar :all]))

(defsynth my-clk [freq 1660 res 0.8]
         (let [ fm-mod (* 600 (sin-osc freq (* 5/3 freq)))
                freq (+ freq fm-mod)]
  (out [0 1] (-> (dc 1.0)
                 (* (env-gen (env-perc 0 0.02)) 0.2)
                 (svf freq res))
       )))

(my-clk 1660 0.95)
(defsynth my-drone1 [freq 220 gate 1]
  (let [my-lfo (+ 200 (* 100 (sin-osc 1)))]
  (out [0 1] (-> (saw freq)
                 (svf my-lfo 0.6)
                 (* 0.3 (env-gen (env-asr 0.5 1 3) :gate gate :action FREE))
                 (pan2 my-lfo)
                 ))))
(defsynth my-burst2 [freq 440  res 0.9 gate 1 fm-ratio 5/3 fm-mag 0.5]
  (let [my-lfo (+ 10 (* 10 (lf-noise1 5)))
        my-lfo2 (sin-osc:kr 0.5)
        fm-mod (* fm-mag  freq (sin-osc (* freq fm-ratio)))]
    (-> (pulse  my-lfo)
        (svf (+ freq  (* 600 fm-mod)) res 0 1 0 0 1)
        (* 0.1 (env-gen (env-asr 0.5 1 3) :gate gate :action FREE))
        (pan2 my-lfo2)
        (as-> x (out [0 1] x))
        )))

(defsynth my-burst [freq 440  res 0.8 gate 1 vol 0.05]
  (let [my-lfo (+ 10 (* 10 (lf-noise1 5)))
        my-lfo2 (sin-osc:kr 0.5)]
    (-> (pulse  my-lfo)
        (svf freq res 0 1 0 0 1)
        (* vol (env-gen (env-asr 0.5 1 3) :gate gate :action FREE))
        (pan2 my-lfo2)
        (as-> x (out [0 1] x))
        )))

(my-burst 7040)
(my-burst 3520)
(my-burst  1760)
(my-burst 440)
(my-burst)

(defn fadeout [] (ctl 0 :gate 0))
(fadeout)
(ctl 0 :res 0.95)
(stop)
(defsynth clicker [gate 1]  (let [my-lfo (* 3 (+ 1 (lf-noise1 5))) ]
            (out [0 1] (-> (pulse my-lfo)
                     (svf (* 1660 my-lfo) 0.8 0 1)
                     (* 0.1 (env-gen (env-asr 0.5 1 3) :gate gate :action FREE))
                     (pan2 (lf-noise1))
                     (free-verb)
                     ))))

(clicker)
(my-drone1)
(my-drone1 110)
(my-drone1 330)
(my-drone1 440)
(my-drone1 880)

(doseq [f [80 84 87 89]] (my-burst (midi->hz f)))

(do (map #(my-burst (midi->hz %)) [90 95 1000]))
