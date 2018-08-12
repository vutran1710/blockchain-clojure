(ns blockchain.init
  (:require [blockchain.helper :refer [now]]))


(defonce ^:private genesis-block {:index 1
                                  :time (now)
                                  :proof "vutr.io"})

(def chain (atom [genesis-block]))
(def nodes (atom []))
