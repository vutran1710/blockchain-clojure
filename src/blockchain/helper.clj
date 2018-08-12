(ns blockchain.helper
  (:require [clojure.string :refer [starts-with?]]))

(defn now []
  (quot (System/currentTimeMillis) 1000))

(defn fix-prc [addr]
  "Fix address without http protocol"
  (if (starts-with? addr "http")
    addr
    (str "http://" addr)))
