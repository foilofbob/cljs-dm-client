(ns cljs-dm-client.campaign-select.events
  (:require
   [ajax.core :as ajax]
   ;[camel-snake-kebab.core :as csk]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
  ::campaign-select-page-load
  (fn [{:keys [db]} _]
    {:db         (assoc db :loading-status :loading)
     :http-xhrio {:method          :get
                  :uri             "http://localhost:8090/campaign"
                  :timeout         10000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::campaign-select-page-load-success]
                  :on-failure      [::campaign-select-page-load-failure]}}))

(reg-event-fx
  ::campaign-select-page-load-success
  (fn [{:keys [db]} [_ response]]
    {:db (-> db
           (assoc :loading-status :success)
           (assoc-in [:page-data :campaigns] (utils/tranform-response response)))}))

(reg-event-fx
  ::campaign-select-page-load-failure
  (fn [{:keys [db]} [_ response]]
    {:db (-> db
           (assoc :loading-status :failure)
           (assoc :page-data response))}))

(reg-event-db
  ::select-campaign
  (fn [db [_ campaign]]
    (assoc db :selected-campaign campaign)))
