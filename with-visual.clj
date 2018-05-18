 (ns over-quil.with-visual  
  (:require [quil.core :as q]
          ;  [quil.middleware :as m]
            ))
(defn setup []
 (q/frame-rate 30)
 ;(q/background 0)
 ) 

(defn draw[] 
 (let [r (buffer-get b1 1)] 
      (q/back-ground 0)
  (q/ellipse 100 100 r r )
 ))

(defsketch with-visual 
  :size [200 200]
  :draw draw
  :setup setup
)
