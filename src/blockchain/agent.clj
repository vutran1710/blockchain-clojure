(ns blockchain.agent
  (:require [clj-http.client :as client]
            [blockchain.init :refer :all]
            [blockchain.helper :refer [fix-prc]]
            [blockchain.worker :refer [resolve-chain-conflict]]))


;; Behind-the-scene works
(defn- add-node [address]
  (when-not (or (.contains @nodes address)
                (= address "0:0:0:0:0:0:0:1")
                (= address (System/getenv "GATEWAY")))
    (swap! nodes conj address)))

(defn- remove-from-nodes [node]
  (->> (remove #(= node %) @nodes)
       (reset! nodes)))

(defn- update-node-list [remote-nodes]
  (run! add-node remote-nodes))

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

(defn- update-node-chain [{:keys [chain nodes]}]
  (resolve-chain-conflict chain)
  (update-node-list nodes))


;; Public services
(defn broadcast []
  "Submit the new chain to other nodes in the network."
  (run! submit-chain @nodes))

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
