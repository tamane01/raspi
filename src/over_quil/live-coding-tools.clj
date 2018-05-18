(require ['overtone.live :refer :all])

(defmacro fader [gate] `(~'* (env-gen (env-asr 0.1 1 3) :gate ~gate :action FREE)))
(defn fade-out ([node-no] (ctl node-no :gate 0)
               ([] (fade-out 0))) )

(defmacro out' [src out-ch gate] `(out ~out-ch (-> ~src (fader ~'gate))))
(macroexpand-1 '(out' 1 1 1))
(pp-node-tree)

(defsynth foo [freq 440 mod-osc-ratio 2 gate 1] (let [modulation-osc (sin-osc (* 2 freq))
                                                      ]
                                                  _

