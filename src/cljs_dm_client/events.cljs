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
 :resume-session-if-present
 (fn [{:keys [db]} [_]]
   (let [session (utils/read-from-session)
         current-page (some-> session :current-page)
         authenticated? (or (some-> db :auth :authenticated?)
                            (some-> session :auth :authenticated?))
         target-page (cond
                       (not authenticated?)     :login
                       (= "login" current-page) :campaign-select
                       :else                    (or current-page :campaign-select))]
     {:fx [[:dispatch [::nav/navigate target-page]]]})))

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
 ::clear-page-error
 (fn [db [_]]
   (dissoc db :page-error)))

;; This will handle error responses with retries on 401s. On a 401 it will queue the original
;; event for retry and trigger a token refresh if one is not already in flight. For non-401s
;; it will call the supplied failure handler. This is especially useful for async-flow as those
;; will stop watching once the expected error events occur. Standard error handling is included
;; here so typically the additional failure-event is just used as a signal.
(reg-event-fx
 :failure-with-refresh-fx
 (fn [{:keys [db]} [_ original-event failure-event response]]
   (let [status (:status response)
         refreshing? (-> db :auth :refreshing?)]
     (cond
           ;; Not an auth error
       (not= status 401)
       {:db (-> db
                (assoc :loading-status :failure)
                (assoc :page-error response))
        :fx [[:dispatch failure-event]]
        :dispatch-later [{:ms 5000 :dispatch [::clear-page-error]}]}

           ;; Already refreshing, enqueue for retry
       refreshing?
       {:db (update-in db [:auth :retry-queue] conj original-event)}

           ;; Otherwise trigger
       :else
       {:db (-> db
                (update-in [:auth :retry-queue] conj original-event)
                (assoc-in [:auth :refreshing?] true))
        :fx [[:dispatch [:refresh]]]}))))

(reg-event-fx
 :standard-failure-fx
 utils/standard-failure-fx)

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
                   :on-failure      [:failure-with-refresh-fx
                                     [:fetch-notes reference-type]
                                     [:fetch-notes-failure]]}})))

(reg-event-db
 :fetch-notes-success
 (fn [db [_ response]]
   (utils/append-success-handler db :notes response)))

(reg-event-fx
 :fetch-notes-failure
 identity)

(reg-event-db
 :page-ready
 (fn [db [_]]
   (assoc db :loading-status :success)))

(reg-event-fx
 :action-failure
 (fn [{:keys [db]} [_ response]]
   (cond-> {:db (-> db
                    (assoc :action-status :failure)
                    (assoc :page-error response))
            :dispatch-later (cond-> [{:ms 5000 :dispatch [::clear-page-error]}]
                              (-> response :status #{401 403})
                              (conj {:ms 5000 :dispatch [:navigate :login]}))}

     (= 401 (:status response))
     (assoc :fx [[:dispatch [:refresh]]]))))

(reg-event-db
 :toggle-modal
 (fn [db [_ modal-key]]
   (update-in db [:page-data :modal modal-key :is-open] not)))

(reg-event-db
 :update-toggles
 "This will toggle a key, either disj or conj - like a true toggle"
 (fn [db [_ toggle-id]]
   (let [current-toggles (or (-> db :page-data :toggles) #{})]
     (update-in db [:page-data :toggles]
                #(if (contains? current-toggles toggle-id)
                   (disj current-toggles toggle-id)
                   (conj current-toggles toggle-id))))))

(reg-event-db
 :set-toggle
 "This will safely only conj"
 (fn [db [_ toggle-id]]
   (let [current-toggles (or (-> db :page-data :toggles) #{})]
     (update-in db [:page-data :toggles]
                #(if (contains? current-toggles toggle-id)
                   current-toggles
                   (conj current-toggles toggle-id))))))

(reg-event-db
 :clear-toggle
 "This will safely only disj"
 (fn [db [_ toggle-id]]
   (let [current-toggles (or (-> db :page-data :toggles) #{})]
     (update-in db [:page-data :toggles]
                #(if (contains? current-toggles toggle-id)
                   (disj current-toggles toggle-id)
                   current-toggles)))))
