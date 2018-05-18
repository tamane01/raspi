(ns over-quil.xy-scope
  (:require [quil.core :as q])
  (:use [overtone.live]))

(defonce xy-buffer (
