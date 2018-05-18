(require ['overtone.live :refer :all]
;         ['overtone.osc :refer :all]
         )
;serial OSC port
(def SERVER-PORT 12001)
(def CLIENT-PORT 12002)

(def server (osc-server SERVER-PORT))
(def client (osc-client "localhost" CLIENT-PORT))
(def device-client (atom nil))
(def thread-alive (atom :alive))
(def arc-value (atom [0 0 0 0]))
;debug
;(osc-listen server  (fn[msg] (print msg)) :debug)
;(osc-rm-listener server :debug)


(defn set-device-port-callback [msg]
  (let [port (last (:args msg))]
   (if @device-client
       (osc-close @device-client)
       nil)
   (reset! device-client (osc-client "localhost" port)
   ;(osc-send @device-client "/sys/port" SERVER-PORT)
)))


(defn in-range2 [x max* min*] 
  (cond 
    (< x max*) max*
    (> x min*) min*
    :else x)) 

(defn arc-delta-callback [c msg] 
  (let [vs (:args msg)
        target-ring (first vs)
        delta (/ (last vs) 10.0)
        new-v (+ (float (nth @arc-value target-ring)) delta)
        l (assoc  (vec @arc-value) target-ring new-v)
        l (map #(in-range2 % 0 64) l)
        ]
       (reset! arc-value l)
       (println "arc: " @arc-value)
    ))
(arc-delta-callback @device-client  {:args [0 1]})

;handler
(osc-handle server "/serialosc/device" (fn[msg] (set-device-port-callback msg))) 
(osc-handle server "/monome/enc/delta" (fn[msg] (arc-delta-callback @device-client msg))) 

;init
(osc-send client "/serialosc/list" "localhost" SERVER-PORT)
(Thread/sleep 100)
(osc-send @device-client "/sys/port" 12001)

(defn update-rings [c] (let [samples @arc-value ;(control-bus-get arc-rings)
                             z 15 
                             clear-rings (fn [c] (doseq  [x (range 4)] (osc-send c "/monome/ring/all" x 0))) ] 
                    (clear-rings c)
                    (ctl 0 :cutoff (+ 0.5 (/ (nth samples 0) 16.0)))     
                    (ctl 0 :res (/ (nth samples 1) 64.0))     
                    (ctl 0 :noise-level (/ (nth samples 2) 16.0))     
                    (ctl 0 :vol (/ (nth samples 3) 256.0))     
                    (Thread/sleep 2) 
                    (doseq [x (range 4)] 
                      
                       ; (osc-send c "/monome/ring/all" x 0)
                        (osc-send c "/monome/ring/set" x (int (nth samples x))  z))))
(update-rings @device-client)
(def monome-updater (future
                      (loop [] (#'update-rings @device-client)
                               (Thread/sleep 33)
                               (if @thread-alive (recur)))))
;(stop)
;(reset! arc-value [0 0 0 0])
;(reset! thread-alive nil)
