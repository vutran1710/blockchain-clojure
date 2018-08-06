(ns blockchain.init
  (:require [digest :refer [sha-256]]
            [clojure.java.io :refer [as-url]]))

(defn now [] (quot (System/currentTimeMillis) 1000))

(def genesis-block {:index 1
                    :time (now)
                    :proof "vutr.io"
                    :previous-hash "khoai"})

(def chain (atom [genesis-block]))
(def current-transactions (atom []))
(def nodes (atom []))
