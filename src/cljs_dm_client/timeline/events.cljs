(ns cljs-dm-client.timeline.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :timeline-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-game-days-success :fetch-notes-success]
  [::fetch-game-days-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
     {:dispatch-n [[::fetch-game-days]
                   [:fetch-notes "game_day"]]}))

(reg-event-fx
 ::fetch-game-days
 (fn [{:keys [db]} _]
     {:http-xhrio {:method          :get
                   :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/gameday")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::fetch-game-days-success]
                   :on-failure      [::fetch-game-days-failure]}}))

(reg-event-db
 ::fetch-game-days-success
 (fn [db [_ response]]
     (utils/standard-success-handler db :game-days response)))

(reg-event-fx
 ::fetch-game-days-failure
 utils/standard-failure-handler)

(reg-event-fx
  ::post-game-day
  (fn [{:keys [db]} [_]]
      {:db         (assoc db :action-status :working)
       :http-xhrio {:method          :post
                    :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/gameday")
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [::post-game-day-success]
                    :on-failure      [:action-failure]}}))

(reg-event-fx
  ::post-game-day-success
  (fn [{:keys [db]} [_ response]]
      (let [new-game-day (utils/tranform-response response)]
           {:db (-> db
                    (assoc :action-status :success)
                    (update-in [:page-data :game-days] #(conj % new-game-day)))})))
