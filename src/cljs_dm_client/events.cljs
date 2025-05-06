(ns cljs-dm-client.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.navigation.events :as nav]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
   [cljs-dm-client.db :refer [default-db]]))

(reg-event-db
 ::initialize-db
 (fn [db [_]]
     (merge default-db (utils/read-from-session))))

(reg-event-fx
 ::resume-session-if-present
 (fn [{:keys [db]} [_]]
     (let [current-page (some-> (utils/read-from-session) :current-page)]
          (print (str "Session page: " current-page))
          {:fx [[:dispatch [::nav/navigate (or current-page :campaign-select)]]]})))

(reg-fx
 :update-in-session
 (fn [data]
   (utils/update-in-session data)))

(reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

(reg-event-db
 :standard-async-success
 (fn [db [_ path response]]
     (assoc-in db [:page-data path] (utils/tranform-response response))))

(reg-event-db
 :standard-success
 (fn [db [_ path response]]
     (-> db
         (assoc :loading-status :success)
         (assoc-in [:page-data path] (utils/tranform-response response)))))

(reg-event-db
 :standard-failure
 utils/standard-failure-handler)

(reg-event-fx
  :fetch-notes
  (fn [{:keys [db]} [_ reference-type]]
    (when-let [campaign-id (-> db :selected-campaign :id)]
      {:db         (assoc db :loading-status :loading)
       :http-xhrio {:method          :get
                    :uri             (str "http://localhost:8090/campaign/" campaign-id "/note/" reference-type)
                    :timeout         10000
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:fetch-notes-success]
                    :on-failure      [:fetch-notes-failure]}})))

(reg-event-db
 :fetch-notes-success
 (fn [db [_ response]]
     (utils/standard-success-handler db :notes response)))

(reg-event-fx
 :fetch-notes-failure
 utils/standard-failure-handler)

(reg-event-db
 :page-ready
 (fn [db [_]]
     (assoc db :loading-status :success)))

(reg-event-db
  :action-failure
  (fn [db [_ response]]
      (-> db
          (assoc :action-status :failure)
          (assoc :page-error response))))

(reg-event-db
  :toggle-modal
  (fn [db [_ modal-key]]
    (update-in db [:page-data :modal modal-key :is-open] not)))
