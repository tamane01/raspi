(ns user
  (:require [overtone.live  :refer :all ]
            [overtone.studio.scope :refer :all]))
(scope)
;init groups
(def pre (group "pre"))
(def main (group "main"))
(def post (group "posr"))

;init buses
(defonce lfo-bus (audio-bus))
(defonce main-out [0 1])

;helper fn
(defn fader  ([gate attack release] (env-gen (env-asr attack 1 release) :gate gate :action FREE))
             ([gate] (fader gate 0.1 3)))


(def my-chord [0 4 9 12 38])
(def open-chord (cycle [0 12]))

;synth def
(defsynth saw-voice [freq 440 vol 0.1 gate 1 res 0.8 cutoff 440 lfo-mag 440  noise-level 0.1]
          (out [0 1 fx-bus] (->   (+ (saw (+ (* 0.5 (lf-noise0 1)) freq)) ( saw (+ (* 0.5 (lf-noise0 1)) freq)) (* noise-level (pink-noise)))
                            (svf (+ cutoff (* lfo-mag (in lfo-bus) res ))) 
                            (* vol (fader gate))
                            (pan2 (sin-osc:kr 0.5)))))
   

(defsynth lfo1 [freq 9
                vol 1
                fmf 0.1
                ] 
        
                (out lfo-bus (-> (sin-osc (+ freq (* vol (sin-osc fmf)))))))
(ctl pre :freq 1 :vol  10)
(ctl 0 :vol 0.666666)
(ctl post :gate 0)
(do (map #(saw-voice [:tail post] (midi->hz (+ 53 %))) my-chord))
(do (ctl post :release 0.5 :gate 0) (map #(saw-voice [:tail post] (midi->hz (+ 46 %))) my-chord))
(defn transpose [tp] (ctl post :release 0.5 :gate 0) (map #(saw-voice [:tail post] (midi->hz (+ tp %1  %2))) (rotate 2 my-chord) open-chord))
(transpose 44)
(class pre)
(ctl pre :freq 0.01)
(ctl pre :vol 0.1)
(ctl pre :fmf 0.3)
(ctl post :cutoff 440 :lfo-mag 440 :vol 0.1)
(ctl post :noise-level 0.3)
(ctl post :res 0.95)
(ctl post :vol 0.05)
;control
(def l1 (lfo1 [:tail pre]))
(def v1 (saw-voice [:tail post ] (midi->hz 41)))
