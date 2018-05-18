(ns over-quil.sin 
 (:use [overtone.live]))
(defonce tmp-ch 0)
(defsynth my-saw [freq 440 mul 0.2] (out 0 (svf (* mul (saw freq)) (mouse-x 60 16000) (mouse-y 0 1.0))))
(doseq [n [60 64 68 73]] (my-saw  (midi->hz n)))

(my-saw)

