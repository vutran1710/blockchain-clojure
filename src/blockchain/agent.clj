(ns blockchain.agent
  (:require [clj-http.client :as client]
            [blockchain.init :refer :all]
            [blockchain.worker :refer [resolve-chain-conflict]]))

(defn broadcast [command]
  "Radio Center"
  (println "Broadcasting newly-mined block..."))

(defn fetch-remote-chain [remote-port]
  (let [address (str "http://localhost:" remote-port)]
    (-> (client/get address {:async? false :as :auto})
        (:body)
        (vec)
        (resolve-chain-conflict))
    (swap! nodes conj address)))

(defn get-address [{:keys [remote-addr]}]
  (println remote-addr))
