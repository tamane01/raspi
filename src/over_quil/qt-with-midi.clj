(ns user
 (:use [overtone.live]))

;def instulments
(definst my-saw3 [freq 440 mul 0.5 lfo-rate 3] 
       (-> (saw freq) 
           (svf(+ (+ freq 100) (* 900 (env-gen (perc 0.02 1 )))) 0.85)
           (* mul (env-gen (perc 0.01 1 -1) 1 1 0 1 FREE))))
           
(def cc (sample (freesound-path 43678)))
(def hh (sample (freesound-path 175549)))
(def sd (sample (freesound-path 406)))  
(def kick (sample (freesound-path 25649))) 
(demo (* (sd)  (env-gen (perc 0.01 0.1) 1101 FREE))) 
(demo (kick))
;MIDIサポート

(def midi-inst (first (midi-connected-receivers)))
(defn play-midi
  [note] (midi-note midi-inst note 80 80 0))

(play-midi 60)


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
  (doseq [n sq] (if (number? (n :p ))  
                  ;(apply-at (n :t) #(play-midi (n :p)))
                  (apply-at (n :t) #(cv-gate (n :p)))
                  (at (n :t) (cond 
                                   (nil? (n :p)) nil
                                   (= :r (n :p))     (inst (midi->hz (+ 20 (int (* 40 (rand))))))
                                   (number? (n :p))   (inst (midi->hz (n :p)))
                                                    
                                   (= :cc (n :p))     (cc)  
                                   (= :bd (n :p))     (kick)  
                                   (= :sd (n :p))     (sd)  
                                   (= :hh (n :p))     (hh)  
                                   ;(= :syn (n :p))   (doseq [n [57 60 64 ]] (my-saw2 (midi->hz n)))
                                   ;  (list? (n :p)) (list-analize-helper (n :p)  inst )
                                   )))))

(defn list-analize-helper [note inst]
  (case (first note)
    :r (inst (-> rand (* (/ (- (get 3 note) (second note)) (second note)) (+ (second note)) )(int) ))
    ))   

(def m (metronome 30))
"midi looper"
(defn looper1 ([nome seq-atom inst]
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
                     (render-note inst)))
    (apply-by next-beat #'looper1 [nome seq-atom inst])))
  ([nome seq-atom] (looper1 [nome seq-atom :midi])))


;scale 
(def ionian-seed [1 3 5 6 8 10 12] )
(def penta [1 3 5 8 10])
(defn scale-oct [n scale-seed] (map #(+ (* 12 n) %) scale-seed))
(defn scale-maker [n scale-seed] 
  "1オクターブ分のシーケンス(vector)からnオクターブのスケールを作成"
  (let [scale-o (fn [n seed]
                 (map #(+ (* 12 n) %) seed))]
                      (vec (flatten (map #(scale-o % scale-seed) (range  n))))))

(def penta-scale (scale-maker 4 penta))
(scale-maker 4 ionian-seed)
(defn snapper [n scale] (first (filter #(<= n %) scale)))               
(snapper 5 ionian-seed)

penta-scale

;controler function
(def m2 (fn [& sq] (modify my-seq2 sq)))  

(def s1 (atom [nil]))
(def s2 (atom [nil]))
(def s3 (atom [nil]))


(looper1 m s1 :midi )
(looper1 m s2 :midi )
(looper1 m s3 :midi )
;control
(reset! s1 [[33 45  nil  33]  [45 33 nil  45 ] [nil 24] [31 43]])
(reset! s2 [[:bd :hh ] [:bd [:hh :hh :hh :hh] ] [:bd :hh ] [:bd :bd :hh :hh]    ]  )
(reset! s2 [:bd   [:hh :hh :hh :hh]  :bd :sd  [:bd :bd :hh :hh]    ]  )
(reset! s3 [nil :sd nil :sd [:sd :sd :sd :sd] nil :sd :sd nil :sd nil :sd :sd nil :sd :sd])
(reset! s1 [nil])
(reset! s2 [nil])
(reset! s3 [nil])

(reset! s1 (shuffle @s1))
(reset! s2 (shuffle @s2))
(reset! s3 (shuffle @s3))
(reset! s1 (shuffle (into [nil nil](map #(snapper % penta-scale)  (range 17 35 2)))))
(reset! s1 penta-scale)

(reset! my-seq4 (into [nil nil ]  @my-seq4))
(reset! my-seq4 (take 8  @my-seq4))
(reset! my-seq4 (map #(- % 24)  @my-seq4))

(reset! s5 [nil [nil :syn]]  )
