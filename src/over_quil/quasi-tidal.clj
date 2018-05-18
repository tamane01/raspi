(ns user
 (:use [overtone.live]))

;def instulments
(definst click [freq 880 amp 0.8]
  (-> (sin-osc (+ freq (* 100 (sin-osc (* 0.3 freq)) ))) 
      (*  amp (env-gen (perc 0.01 0.1) 1 1 0 1 FREE))
      )
  )

(definst my-saw2 [freq 440 mul 0.2 lfo-rate 3] 
       (svf  
         (* mul  (env-gen (perc 0.01 1.5) 1 1 0 1 FREE)
             (+ 
              (saw (+ freq (* 1 (lf-noise0 0.5))))
              (saw (+ freq (* 1 (lf-noise0 0.5)))))
            (sin-osc lfo-rate))
         (-> (sin-osc 6) (* 500) (+ 800 )) 0.8
         )
       )

(definst my-saw3 [freq 440 mul 0.2 lfo-rate 3] 
       (-> (saw freq) 
           (svf (+ freq (* 500 (env-gen (perc 0.02 0.3 )))) 0.7  )
           (* mul (env-gen (perc 0.01 1 -1) 1 1 0 1 FREE))))

(defsynth clk [dur 0.2 ch 0 filter-f 1660 filter-res 0.7] 
  (out ch (-> (dc:ar 1)
              (* 0.5 (env-gen:ar (env-perc 0 dur) :action FREE ) )
              (svf filter-f filter-res)
              ))) 
(clk :filter-f 3220)

(defsynth bounce [] 
                      (out 0 (-> (saw 440 )
                                 (svf 1660 0.8)
                                 
                          )))
;j(bounce)    
(def cc (sample (freesound-path 43678)))
(def hh (sample (freesound-path 175549)))
(def sd (sample (freesound-path 26903)))  
(def kick (sample (freesound-path 25649))) 
;(def kick (trg 0.2 6))
;(definst kick [freq 120 dur 0.9 width 0.5]
;    (let [freq-env (* freq (env-gen (perc 0 (* 0.99 dur))))
;                  env (env-gen (perc 0.01 dur) 1 1 0 1 FREE)
;                          sqr (* (env-gen (perc 0 0.01)) (pulse (* 2 freq) width))
;                                  src (sin-osc freq-env)
;                                          drum (+ sqr (* env src))]
;          (compander drum drum 0.2 1 0.1 0.01 0.01)))

;<- def instulments

;def sequencer
(defn time-mapper [sq start-time dur]
  (let [ n (count sq) 
         span 
                 (/ dur n)
        ] 
    (map-indexed #(if (vector? %2) 
                    (time-mapper %2  (+ start-time  (* %1  span )) span)
                    (hash-map :p  %2 :t  (+ start-time (* %1 span)))
                    ) sq)))

(defn render-note [sq inst ]
  "vector sqの要素によってディスパッチし、スケジューラーに登録"
  (doseq [n sq]   
                (at (n :t) (cond 
                                   (nil? (n :p)) nil
                                   (= :r (n :p))     (inst (midi->hz (+ 20 (int (* 40 (rand))))))
                                   (number? (n :p))  (inst (midi->hz (n :p)))
                                   (= :ck (n :p))     (clk) 
                                   (= :cc (n :p))     (cc)  
                                   (= :bd (n :p))     (kick)
                                   (= :sd (n :p))     (sd)  
                                   (= :hh (n :p))     (hh)  
;                                   (= :syn (n :p))   (doseq [n [57 60 64 ]] (my-saw2 (midi->hz n)))
                                   ;  (list? (n :p)) (list-analize-helper (n :p)  inst )
                                   ))))

(defn list-analize-helper [note inst]
  (case (first note)
    :r (inst (-> rand (* (/ (- (get 3 note) (second note)) (second note)) (+ (second note)) )(int) ))
    ))   

(def m (metronome 30))
(defn looper2 [nome seq-atom]
  (let [beat (nome)
        sq (vec @seq-atom)
        just-beat (nome beat)
        next-beat (nome (inc beat))
        beat-len (- next-beat just-beat)
        div (count sq)
        foo (/ beat-len div)  
        ]
        (when-not (= 0 (count sq))
                     (-> (time-mapper sq just-beat beat-len)
                     (flatten)
                     (render-note my-saw3)))
    (apply-at just-beat (transpose (rand-nth (range 40 50))))
    (apply-by next-beat #'looper2 [nome seq-atom])))

(defn looper3 [nome seq-atom]
  (let [beat (nome)
        sq (vec @seq-atom)
        just-beat (nome beat)
        next-beat (nome (+ beat 2))
        beat-len (- next-beat just-beat)
        div (count sq)
        foo (/ beat-len div)  
        ]
    (when-not (= 0 (count sq))
                     (-> (time-mapper sq just-beat beat-len)
                     (flatten)
                     (render-note my-saw3)))
    
            (apply-by next-beat #'looper3 [nome seq-atom])))


(defn looper4 [nome f]
  (let [beat (nome)
        just-beat (nome beat)
        next-beat (nome (+ beat 2))
        ]
            (apply-at  just-beat (do f)) 
            (apply-by next-beat #'looper3 [nome f])))

(looper4 m  
 #(reset! my-seq3 (shuffle @my-seq3)))

;scale 
(def ionian-seed [1 3 5 6 8 10 12] )
(def penta [1 3 5 8 10])
(defn scale-oct [n scale-seed] (map #(+ (* 12 n) %) scale-seed))
(defn scale-maker [n scale-seed] 
  "1オクターブ分のシーケンス(vector)からnオクターブのスケールを作成"
  (let [scale-o (fn [n seed]
                 (map #(+ (* 12 n) %) seed))]
                      (vec (flatten (map #(scale-o % scale-seed) (range  n))))))

(scale-maker 4 ionian-seed)
(defn snapper [n scale] (first (filter #(<= n %) scale)))               
(snapper 5 ionian-seed)
;リスト内包表現
(for [x (range 0 120 12) y penta ] (+ x y))
;reduce
(reduce #(into %1 (map (fn [a] (+ a %2)) penta )) [] (range 0 120 12)) 
(def penta-scale  (scale-maker 10  penta))

;controler function
(defn modify
 "modyfy musical sequencea (vector)
 (modify sequence-id vector-or-option)   
 option
  :q empty the sequence
  :s shuffle the sequence
 " 

   ([nm] (print @nm))
   ([nm sq] (cond 
              (vector? sq) (reset! nm sq)
              (keyword? sq) (cond (= sq :shuffle) (->> @nm (shuffle) (vec) (reset! nm))
                                  (= sq :q) (reset! nm [nil])
                                 )))) 
(def m2 (fn [& sq] (modify my-seq2 sq)))  

(def my-seq2 (atom [nil]))
(def my-seq3 (atom [nil]))
(def my-seq4 (atom [nil]))

(def s5 (atom [nil]))

(looper3 m my-seq2)
(looper2 m my-seq3)
(looper2 m my-seq4)
(looper3 m s5)
(transpose 50)
(defsynth my-lfo1 [f 1] (out 5  (-> (sin-osc f)
                                (* 0.5)
                                (+ 0.5)
                                   ))) 
;(defsynth foo [] (out 0 (sin-osc 440)))
;(foo


(scope 5)
(ctl lh1 :f 1.5)

(clk)
(def lh1 (my-lfo1));control
(modify s1 [:h])

(reset! my-seq2 [:ck :ck :bd ])
(reset! my-seq2 [nil])
(reset! my-seq2 [:bd :ck [:ck :ck :sd [:hh :hh :hh]]])
(reset! my-seq2 [:hh :hh :bd 90 :hh :hh :bd [120 :hh]    ])
(reset! my-seq2 [:hh :hh :hh :hh [:hh :hh :hh]   [:hh :hh :hh] :hh    ])
(reset! my-seq2 [:bd :hh [nil :hh :hh :hh] [:sd :hh] ])
(reset! my-seq2 [:bd :hh :sd :hh :bd [:hh :hh] :sd :hh])
(reset! my-seq2 [:bd [:hh :hh] [:sd :bd] [:hh nil :hh :hh]])

(reset! my-seq2 [:bd :hh :sd [:hh :bd] :hh [:hh :hh] :sd :hh])
(reset! my-seq2 
        [:bd [:hh :hh] [:sd :bd] [:hh nil :hh :hh] :bd [:hh :hh] [:sd :bd] [:hh nil :hh :hh]])

(reset! my-seq2 [:bd :hh :hh nil  [nil :hh] :hh [:hh [:hh :hh :hh]]])
(reset! my-seq2 [:hh :hh  :hh nil  [nil :hh] :hh [:hh [:hh :bd]]])
(reset! my-seq2 [:hh :hh  :hh nil  [nil :bd] :hh [:hh [:hh :hh]]])
(reset! my-seq2 [:bd :hh  [:hh :hh 90 :hh] 120  [nil :hh] :sd nil nil :cc])


(reset! my-seq3 [nil])
(reset! s5 [:bd :sd :bd :sd] )
(reset! my-seq3 [:bd [:sd]]  )
(reset! my-seq3 [nil [nil :syn]]  )
(reset! s5 [:bd [:sd :syn]]  )
(reset! my-seq3 [:bd [:sd :sd :sd]]  )
(reset! my-seq3 [[:bd :syn [:bd :bd :bd] ] [:sd :sd :sd]]  )
(reset! my-seq3 [[:bd :bd :bd] [:syn  [:sd nil [110 110 110 110]]] ])
(reset! my-seq3 [[[:bd :bd] nil ] :syn :sd nil ])
(m :bpm 35)
(reset! my-seq4 (vec (range 100 108 1)))
(reset! my-seq4 [nil])

(reset! my-seq2 (shuffle @my-seq2))
(reset! my-seq3 (shuffle @my-seq3))
(reset! my-seq4 (shuffle @my-seq4))
(reset! my-seq4 (map #(snapper % penta-scale)  @my-seq4))
(reset! my-seq4 (map #(snapper % penta-scale)  (range  40 80 3)))
(reset! my-seq4 (map #(snapper % penta-scale)  (range  80 110 3)))
(reset! my-seq4 (into [nil nil ]  @my-seq4))
(reset! my-seq4 (take 8  @my-seq4))
(reset! my-seq4 (map #(- % 24)  @my-seq4))

(reset! s5 [nil [nil :syn]]  )

