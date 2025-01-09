(ns cljs-dm-client.timeline.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
  ::timeline-page-load
  (fn [{:keys [db]} _]
    (if (-> db :selected-campaign nil?)
      {:navigate :campaign-select}                          ;; TODO: Make this an interceptor
      {:db         (assoc db :loading-status :loading)
       :http-xhrio {:method          :get
                    :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/gameday" )
                    :timeout         10000
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [::timeline-page-load-success]
                    :on-failure      [:standard-failure]}})))

(reg-event-fx
  ::timeline-page-load-success
  (fn [{:keys [db]} [_ response]]
    {:db (-> db
           (assoc :loading-status :success)
           (assoc-in [:page-data :game-days] (utils/tranform-response response)))
     :fx [[:dispatch [:fetch-notes "game_day"]]]}))
