(ns cljs-dm-client.campaign-select.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :campaign-select-page-load
 (fn [{:keys [db]} _]
     {:db         (assoc db :loading-status :loading)
      :http-xhrio {:method          :get
                   :uri             "http://localhost:8090/campaign"
                   :timeout         10000
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::campaign-select-page-load-success]
                   :on-failure      [::campaign-select-page-load-failure]}}))

(reg-event-db
 ::campaign-select-page-load-success
 (fn [db [_ response]]
     (-> db
         (assoc :loading-status :success)
         (assoc-in [:page-data :campaigns] (utils/tranform-response response)))))

(reg-event-fx
 ::campaign-select-page-load-failure
 (fn [db [_ response]]
     (-> db
         (assoc :loading-status :failure)
         (assoc :page-data response))))

(reg-event-fx
 ::select-campaign
 (fn [{:keys [db]} [_ campaign nav-call]]
     (if (nil? campaign)
       {:db (-> db
                (assoc :action-status :success)
                (assoc :selected-campaign nil))
        :fx [[:dispatch nav-call]]}
       {:db         (assoc db :action-status :working)
        :http-xhrio {:method          :get
                     :uri             (str "http://localhost:8090/campaign/" (:id campaign))
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [::select-campaign-success nav-call]
                     :on-failure      [:action-failure]}})))

(reg-event-fx
 ::select-campaign-success
 (fn [{:keys [db]} [_ nav-call response]]
     (let [parsed-response (utils/tranform-response response)]
          {:db (-> db
                   (assoc :action-status :success)
                   (assoc :selected-campaign (:campaign parsed-response))
                   (assoc :campaign-setting (select-keys parsed-response [:months
                                                                          :week-days
                                                                          :calendar-cycles
                                                                          :calendar-cycle-offsets
                                                                          :calendar-events])))
           :fx [[:dispatch nav-call]]})))
