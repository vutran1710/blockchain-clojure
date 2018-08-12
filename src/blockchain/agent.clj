(ns blockchain.agent
  (:require [clj-http.client :as client]
            [clojure.string :refer [starts-with?]]
            [blockchain.init :refer :all]
            [blockchain.worker :refer
             [resolve-chain-conflict
              remove-from-nodes
              update-node-list
              add-node]]))


(defn- fix-prc [addr]
  (if (starts-with? addr "http")
    addr
    (str "http://" addr)))


(defn- submit-chain [addr]
  (when (string? addr)
    (try
      (client/post (fix-prc addr) {:form-params @chain
                                   :content-type :json
                                   :async? true}
                   (fn [msg] (println "Chain submitted to" addr))
                   (fn [e] (do (println "Failed to submit to: >> " addr)
                               (println (.getMessage e))
                               (remove-from-nodes addr))))
      (catch Exception e (remove-from-nodes addr)))))

(defn broadcast []
  "Submit the new chain to other nodes in the network."
  (run! submit-chain @nodes))

(defn- update-node-chain [{:keys [chain nodes]}]
  (resolve-chain-conflict chain)
  (update-node-list nodes))

(defn fetch-remote-chain [address]
  (println "Fetching from >>" address)
  (try
    (client/get (fix-prc address) {:async? true :as :auto}
                (fn [data] (-> (:body data)
                               (update-node-chain))
                  (add-node address))
                (fn [e] (println "Cannot fetch from >>" address)
                  (println (.getMessage e))
                  (remove-from-nodes address)))
    (catch Exception e
      (do (println (.getMessage e))
          (remove-from-nodes address)))))

(defn get-address [{:keys [remote-addr]}]
  (println "Request from >>" remote-addr)
  (add-node remote-addr))
