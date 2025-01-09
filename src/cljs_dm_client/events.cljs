(ns cljs-dm-client.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [cljs-dm-client.db :as db]))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

(reg-event-fx
  :fetch-notes
  (fn [{:keys [db]} [_ reference-type]]
    (when-let [campaign-id (-> db :selected-campaign :id)]
      {:db         (assoc db :loading-status :loading)
       :http-xhrio {:method          :get
                    :uri             (str "http://localhost:8090/campaign/" campaign-id "/note/" reference-type)
                    :timeout         10000
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:standard-success :notes]
                    :on-failure      [:standard-failure]}})))

(reg-event-db
  :standard-success
  (fn [db [_ path response]]
    (-> db
      (assoc :loading-status :success)
      (assoc-in [:page-data path] (utils/tranform-response response)))))

(reg-event-db
  :standard-failure
  (fn [db [_ response]]
    (-> db
      (assoc :loading-status :failure)
      (assoc :page-error response))))
