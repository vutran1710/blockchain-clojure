(ns blockchain.worker
  (:require [blockchain.init :refer :all]
            [digest :refer [sha-256]]
            [clojure.java.io :refer [as-url]]))

(defn- add-tail [vect item]
  "Conj is consistent about where to add new element. Make our own function to do that."
  (vec (concat vect [item])))

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

(defn add-node [address]
  (when-not (or (.contains @nodes address)
                (= address "0:0:0:0:0:0:0:1")
                (= address (System/getenv "GATEWAY")))
    (swap! nodes conj address)))

(defn remove-from-nodes [node]
  (->> (remove #(= node %) @nodes)
       (reset! nodes)))

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
      (do (swap! chain add-tail block) chain))))

(defn resolve-chain-conflict [remote-chain]
  "Chain with greater length will replace the existing chain."
  (let [existing-length (count @chain)
        remote-length (count remote-chain)]
    (when (> remote-length existing-length)
      (do (reset! chain remote-chain)
          (println "Applied new chain...")))))

(defn update-node-list [remote-nodes]
  (run! add-node remote-nodes))
