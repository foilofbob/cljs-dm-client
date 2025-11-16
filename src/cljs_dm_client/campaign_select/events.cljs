(ns cljs-dm-client.campaign-select.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :campaign-select-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-campaigns-success ::fetch-campaign-settings-success]
  [::fetch-campaigns-failure ::fetch-campaign-settings-failure]
  false))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
   {:dispatch-n [[::fetch-campaigns]
                 [::fetch-campaign-settings]]}))

(reg-event-fx
 ::fetch-campaigns
 (fn [{:keys [db]} _]
   {:db         (assoc db :loading-status :loading)
    :http-xhrio {:method          :get
                 :uri             "http://localhost:8090/campaign"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-campaigns-success]
                 :on-failure      [::fetch-campaigns-failure]}}))

(reg-event-db
 ::fetch-campaigns-success
 (fn [db [_ response]]
   (assoc-in db [:page-data :campaigns] (utils/tranform-response response))))

(reg-event-fx
 ::fetch-campaigns-failure
 (fn [db [_ response]]
   utils/standard-failure-handler))

(reg-event-fx
 ::fetch-campaign-settings
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             "http://localhost:8090/campaign-settings"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-campaign-settings-success]
                 :on-failure      [::fetch-campaign-settings-failure]}}))

(reg-event-db
 ::fetch-campaign-settings-success
 (fn [db [_ response]]
   (assoc-in db [:page-data :campaign-settings] (utils/tranform-response response))))

(reg-event-fx
 ::fetch-campaign-settings-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::select-campaign
 (fn [{:keys [db]} [_ campaign nav-call]]
   (if (nil? campaign)
     {:db (-> db
              (assoc :action-status :success)
              (assoc :selected-campaign nil)
              (assoc :campaign-setting nil))
      :fx [[:dispatch nav-call]]
      :update-in-session {:selected-campaign nil
                          :campaign-setting nil}}
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          :get
                   :uri             (str "http://localhost:8090/campaign/" (:id campaign))
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::select-campaign-success nav-call]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::select-campaign-success
 (fn [{:keys [db]} [_ nav-call response]]
   (let [parsed-response  (utils/tranform-response response)
         campaign         (:campaign parsed-response)
         campaign-setting (select-keys parsed-response [:months
                                                        :week-days
                                                        :calendar-cycles
                                                        :calendar-cycle-offsets
                                                        :calendar-events])]
     {:db (-> db
              (assoc :action-status :success)
              (assoc :selected-campaign campaign)
              (assoc :campaign-setting campaign-setting))
      :fx [[:dispatch nav-call]]
      :update-in-session {:selected-campaign campaign
                          :campaign-setting campaign-setting}})))

(reg-event-fx
 ::edit-campaign
 (fn [{:keys [db]} [_ campaign]]
   (let [new-campaign? (nil? (:id campaign))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-campaign? :post :put)
                   :uri             (cond-> "http://localhost:8090/campaign"
                                      (not new-campaign?)
                                      (str "/" (:id campaign)))
                   :params          (update-in (rename-keys
                                                (cske/transform-keys csk/->PascalCase campaign)
                                                {:Id :ID :CampaignSettingId :CampaignSettingID :CurrentPlayerXp :CurrentPlayerXP})
                                               [:CampaignSettingID]
                                               #(some-> % js/parseInt))
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-campaign-success new-campaign?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-campaign-success
 (fn [{:keys [db]} [_ new-campaign? response]]
   (let [updated-campaign (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-campaign?
            (update-in [:page-data :campaigns] concat [updated-campaign])

            (not new-campaign?)
            (update-in [:page-data :campaigns]
                       #(map (fn [campaign]
                               (if (= (:id campaign) (:id updated-campaign))
                                 updated-campaign
                                 campaign))
                             %)))
      :fx [[:dispatch [:toggle-modal :campaign-modal]]]})))
