(ns blockchain.worker
  (:require [blockchain.init :refer :all]
            [digest :refer [sha-256]]
            [blockchain.helper :refer [now]]))


(defn- hashing-block [block]
  (sha-256 (str block)))

(defn- validate-proof [proof last-hashed]
  (let [guess-hash (sha-256 (str proof last-hashed))]
    (= (subs guess-hash 0 4) "0000")))

(defn- find-proof [block]
  "Find the next nonce"
  (let [hashed (hashing-block block)]
    (loop [proof 0]
      (if-not (validate-proof proof hashed)
        (recur (inc proof)) proof))))

(defn- validate-adjacent-blocks [block prv-block]
  (and (=
        (:index block)
        (inc (:index prv-block)))
       (validate-proof
        (:proof block)
        (hashing-block prv-block))))

(defn- validate-chain [new-chain]
  (loop [id 0 idx 1]
    (if (< idx (count new-chain))
      (if (validate-adjacent-blocks (new-chain idx) (new-chain id))
        (recur (inc id) (inc idx)) false)
      true)))

(defn forge-new-block []
  (let [last-block (last @chain)]
    {:index (inc (count @chain))
     :time (now)
     :proof (find-proof last-block)}))

(defn append-to-chain [block]
  "Append new block to the existing chain. Return the chain after modification."
  (let [proof (:proof block)
        last-hashed (hashing-block (last @chain))]
    (when (validate-proof proof last-hashed)
      (do (swap! chain conj block) chain))))

(defn resolve-chain-conflict [remote-chain]
  "Chain with greater length will replace the existing chain."
  (let [existing-length (count @chain)
        remote-length (count remote-chain)]
    (when (> remote-length existing-length)
      (when (validate-chain remote-chain)
        (do (reset! chain remote-chain)
            (println "Applied new chain..."))))))
