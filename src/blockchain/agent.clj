(ns blockchain.agent
  (:require [clj-http.client :as client]
            [blockchain.init :refer :all]
            [blockchain.worker :refer [resolve-chain-conflict remove-from-nodes update-node-list add-node]]))

(defn- submit-chain [addr]
  (try
    (client/post addr {:form-params @chain
                       :content-type :json
                       :async? true}
                 ;; success callback: alert of success
                 (fn [] println "new chain submitted!")
                 ;; failure callback: remove add from nodelist
                 (fn [e] (remove-from-nodes addr)))
    (catch Exception e (remove-from-nodes addr))))

(defn broadcast [chain]
  "Submit new chain to other nodes in the network."
  (run! submit-chain @nodes))

(defn- update-node-chain [{:keys [chain nodes]}]
  (resolve-chain-conflict chain)
  (update-node-list nodes))

(defn fetch-remote-chain [address]
  (-> (client/get address {:async? false :as :auto})
      (:body)
      (update-node-chain))
  (add-node address))

(defn get-address [{:keys [remote-addr]}]
  (add-node remote-addr))
