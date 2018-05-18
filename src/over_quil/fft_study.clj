(use 'overtone.live)
(defsynth fft-freeze [gate 0]
          (let [fft-buf (local-buf 512)]
           (out [0 1] (-> (fft fft-buf (sound-in 0))
                          (pv-mag-freeze gate)
