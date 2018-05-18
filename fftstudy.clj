(use 'overtone.live)

(def fft-buf1 (buffer 512))
(def fft-buf2 (buffer 512))
(def b1 (audio-bus))

(defsynth cosc1 [freq 440] (out b1 (* 0.5 (+ (sin-osc freq) (saw (* freq 5/3))))))
(defsynth external-in [ch 0 vol 0.5] (out b1 (* vol (sound-in ch))))
(defsynth fft-study1 [freq 440] (let [f1 (-> (fft fft-buf1 (in b1))
                                             (pv-local-max 0.5 ))]
                          (out [0 1] (-> (fft fft-buf2 (white-noise:ar)) 
                                         (pv-mag-mul f1)

                                         (ifft)
                                         (* 0.2)
                                      )))) 
(defsynth fft-study2 [freq 440 vol 0.6 freeze 0] (out [0 1] (-> (fft fft-buf1 (sound-in 0))
                                                       (pv-bin-scramble 0.5)
                                                       (pv-mag-freeze freeze)
                                                       (ifft)
                                                       (* vol)
                                                     )))
(defsynth dry [vol 0.6 ] (out [0 1] (* vol (in b1))))
(dry)
(scope b1)

(def s1 (external-in))

(def m1 (fft-study1))
(def m2 (fft-study2))
(ctl m2 :freeze 0.2)
(volume 0.4)
(def c2 (cosc1 [:head 6]))
(ctl 0 :freq 880)
(pp-node-tree)
(def db1 (audio-bus))
(defsynth my-delay [delaytime 0.2 feedback 0.6 vol 0.5]
          (let [iex-in (sound-in 0)
                lbus1 (local-in)
                fb-out (local-out (-> (+ (sound-in 0) (* feedback (in-feedback lbus1)))
                         (delay-c  1 delaytime)))
            (out [0 1] (* vol (local-in lbus1)))

                     ;   (* vol)
                   )))  
(volume 0.2)
(my-delay)

