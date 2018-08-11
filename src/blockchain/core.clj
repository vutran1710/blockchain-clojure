(ns blockchain.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.walk :refer [keywordize-keys]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as resp]
            [blockchain.init :refer [chain nodes]]
            [blockchain.worker :as worker]
            [blockchain.agent :as agent]
            [blockchain.helper :refer [cli-inquire]]))

(defn generate-response [response]
  (if (integer? (:status response))
    (let [body (:body response)
          status (:status response)]
      {:body body :status status})
    (resp/response response)))

(defroutes app-routes
  (GET "/" request (do (agent/get-address request)
                       (resp/response @chain)))
  (POST "/" {chain :body}
        (-> (keywordize-keys chain)
            (worker/resolve-chain-conflict))
        (generate-response {:status 200}))
  (GET "/nodes" [] (resp/response @nodes))
  (GET "/mine" []
       (let [new-block (worker/forge-new-block)]
         (-> (worker/append-to-chain new-block)
             (agent/broadcast))
         (generate-response {:body new-block :status 201})))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body)
      (wrap-json-response)))

(defn init []
  (try
    ;; Need a boot node preinstalled
    (-> (cli-inquire "Connect to address -> ")
        (agent/fetch-remote-chain))
    (catch Exception e
      (println "Start boot.."))))
