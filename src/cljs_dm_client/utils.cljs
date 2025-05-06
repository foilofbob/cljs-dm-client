(ns cljs-dm-client.utils
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cognitect.transit :as t]))

(defn page-loader [async-flow]
      (fn [{:keys [db]} _]
          (if (-> db :selected-campaign nil?)
            {:navigate :campaign-select}
            {:db         (assoc db :loading-status :loading)
             :async-flow async-flow})))

(defn write-to-session
      "Expecting to write a map containing active-panel and selected-campaign"
      [session-data]
      (try
        (js/sessionStorage.setItem "campaign-manager-session" (js/JSON.stringify (clj->js session-data)))
        (catch :default e
          (print "Exception writing session data: " e))))

(defn read-from-session []
      (try
        (or (some-> (.getItem js/sessionStorage "campaign-manager-session")
                    (js/JSON.parse)
                    (js->clj :keywordize-keys true))
            {})
        (catch :default e
          (print "Exception reading from session data: " e)
          {})))

(defn update-in-session [update-map]
      (let [current-session (read-from-session)
            updated-session (if (seq current-session)
                              (merge current-session update-map)
                              update-map)]
           (write-to-session updated-session)))

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
