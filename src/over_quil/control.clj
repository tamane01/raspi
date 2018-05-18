(recording-start "electrified0330.wav" )

(def rd1 (redirect [:tail 8] pre-fx))
(rec b1)
(pp-node-tree)

(def p1 (seq-player tmp s1))
(def drone1 (wf2 110 0.05))
(def drone2 (wf2 (* 2/3 220) 0.03))

(def b1 (buffer 48000))


(def g1 (granu b1 :mul 0.5 :rate 1))
(def g2 (granu b1 :mul 0.5 :rate 4/3))

(defn fadeout [] (ctl 0 :trig 0))

(fadeout) 
