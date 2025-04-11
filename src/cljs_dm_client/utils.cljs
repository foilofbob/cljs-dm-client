(ns cljs-dm-client.utils
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]))

(defn page-loader [async-flow]
      (fn [{:keys [db]} _]
          (if (-> db :selected-campaign nil?)
            {:navigate :campaign-select}
            {:db         (assoc db :loading-status :loading)
             :async-flow async-flow})))

(defn tranform-response
  "Takes a response body and updates keys to be kebab-case-keyword"
  [response]
  (cske/transform-keys csk/->kebab-case-keyword response))

(defn standard-success-handler [db path response]
      (assoc-in db [:page-data path] (tranform-response response)))

(defn standard-failure-handler [db [_ response]]
      (-> db
          (assoc :loading-status :failure)
          (assoc :page-error response)))
