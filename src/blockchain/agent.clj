(ns blockchain.agent
  (:require [clj-http.client :as client]
            [blockchain.init :refer :all]
            [blockchain.worker :refer [resolve-chain-conflict remove-from-nodes]]))


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

(defn fetch-remote-chain [address]
  "TODO: replace with real ip address"
  (-> (client/get address {:async? false :as :auto})
      (:body)
      (vec)
      (resolve-chain-conflict))
  (when-not (.contains @nodes address)
    (swap! nodes conj address)))

(defn get-address [{:keys [remote-addr]}]
  (when-not (.contains @nodes remote-addr)
    (swap! nodes conj remote-addr)))
