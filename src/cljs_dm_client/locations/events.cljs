(ns cljs-dm-client.locations.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :locations-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-locations-success ::fetch-sublocations-success ::fetch-points-of-interest-success :fetch-notes-success]
  [::fetch-locations-failure ::fetch-sublocations-failure ::fetch-points-of-interest-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
   {:dispatch-n [[::fetch-locations]
                 [::fetch-sublocations]
                 [::fetch-points-of-interest]
                 [:fetch-notes "location"]
                 [:fetch-notes "point_of_interest"]]}))

;; Locations
(reg-event-fx
 ::fetch-locations
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/locations")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-locations-success]
                 :on-failure      [::fetch-locations-failure]}}))

(reg-event-fx
 ::fetch-locations-success
 (fn [{:keys [db]} [_ response]]
   {:db (utils/standard-success-handler db :locations response)
    :fx [[:dispatch [::resume-last-viewed]]]}))

(reg-event-fx
 ::fetch-locations-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::open-edit-location-modal
 (fn [{:keys [db]} [_ location]]
   (let [edit-location (or location
                           {:campaign-id (-> db :selected-campaign :id)
                            :name ""})]
     {:fx [[:dispatch [:set-edit-object :edit-location edit-location]]
           [:dispatch [:toggle-modal :location-modal]]]})))

(reg-event-fx
 ::edit-location
 (fn [{:keys [db]} [_ location]]
   (let [new-location? (nil? (:id location))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-location? :post :put)
                   :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/location")
                                      (not new-location?)
                                      (str "/" (:id location)))
                   :params          (rename-keys
                                     (cske/transform-keys csk/->PascalCase location)
                                     {:Id :ID :CampaignId :CampaignID})
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-location-success new-location?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-location-success
 (fn [{:keys [db]} [_ new-location? response]]
   (let [updated-location (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-location?
            (update-in [:page-data :locations] concat [updated-location])

            (not new-location?)
            (update-in [:page-data :locations]
                       #(map (fn [location]
                               (if (= (:id location) (:id updated-location))
                                 updated-location
                                 location))
                             %)))
      :fx [[:dispatch [:toggle-modal :location-modal]]]})))

(reg-event-fx
 ::delete-location
 (fn [{:keys [db]} [_ location-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/location/" location-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::delete-location-success location-id]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-location-success
 (fn [{:keys [db]} [_ location-id response]]
   {:db (-> db
            (assoc :action-status :success)
            (update-in [:page-data :locations] #(remove (fn [location] (= location-id (:id location))) %)))
    :fx [[:dispatch [:toggle-modal :location-modal]]]}))

;; Sublocations
(reg-event-fx
 ::fetch-sublocations
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/sublocations")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-sublocations-success]
                 :on-failure      [::fetch-sublocations-failure]}}))

(reg-event-db
 ::fetch-sublocations-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :sublocations response)))

(reg-event-fx
 ::fetch-sublocations-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::open-edit-sublocation-modal
 (fn [{:keys [db]} [_ sublocation]]
   (let [edit-sublocation (or sublocation
                              {:campaign-id (-> db :selected-campaign :id)
                               :location-id (-> db :page-data :selected-location-id)
                               :name ""
                               :description ""})]
     {:fx [[:dispatch [:set-edit-object :edit-sublocation edit-sublocation]]
           [:dispatch [:toggle-modal :sublocation-modal]]]})))

(reg-event-fx
 ::edit-sublocation
 (fn [{:keys [db]} [_ sublocation]]
   (let [new-sublocation? (nil? (:id sublocation))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-sublocation? :post :put)
                   :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/sublocation")
                                      (not new-sublocation?)
                                      (str "/" (:id sublocation)))
                   :params          (rename-keys
                                     (cske/transform-keys csk/->PascalCase sublocation)
                                     {:Id :ID :CampaignId :CampaignID})
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-sublocation-success new-sublocation?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-sublocation-success
 (fn [{:keys [db]} [_ new-sublocation? response]]
   (let [updated-sublocation (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-sublocation?
            (update-in [:page-data :sublocations] concat [updated-sublocation])

            (not new-sublocation?)
            (update-in [:page-data :sublocations]
                       #(map (fn [sublocation]
                               (if (= (:id sublocation) (:id updated-sublocation))
                                 updated-sublocation
                                 sublocation))
                             %)))
      :fx [[:dispatch [:toggle-modal :sublocation-modal]]]})))

(reg-event-fx
 ::delete-sublocation
 (fn [{:keys [db]} [_ sublocation-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/sublocation/" sublocation-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::delete-sublocation-success sublocation-id]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-sublocation-success
 (fn [{:keys [db]} [_ sublocation-id response]]
   {:db (-> db
            (assoc :action-status :success)
            (update-in [:page-data :sublocations] #(remove (fn [sublocation] (= sublocation-id (:id sublocation))) %)))
    :fx [[:dispatch [:toggle-modal :sublocation-modal]]]}))

;; Points of Interest
(reg-event-fx
 ::fetch-points-of-interest
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/points-of-interest")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-points-of-interest-success]
                 :on-failure      [::fetch-points-of-interest-failure]}}))

(reg-event-db
 ::fetch-points-of-interest-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :points-of-interest response)))

(reg-event-fx
 ::fetch-points-of-interest-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::open-edit-poi-modal
 (fn [{:keys [db]} [_ sublocation-id poi]]
   (let [edit-poi (or poi
                      {:campaign-id (-> db :selected-campaign :id)
                       :sublocation-id sublocation-id
                       :name ""})]
     {:fx [[:dispatch [:set-edit-object :edit-poi edit-poi]]
           [:dispatch [:toggle-modal :poi-modal]]]})))

(reg-event-fx
 ::edit-poi
 (fn [{:keys [db]} [_ poi]]
   (let [new-poi? (nil? (:id poi))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-poi? :post :put)
                   :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/point-of-interest")
                                      (not new-poi?)
                                      (str "/" (:id poi)))
                   :params          (rename-keys
                                     (cske/transform-keys csk/->PascalCase poi)
                                     {:Id :ID :CampaignId :CampaignID :LocationId :LocationID})
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-poi-success new-poi?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-poi-success
 (fn [{:keys [db]} [_ new-poi? response]]
   (let [updated-poi (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-poi?
            (update-in [:page-data :points-of-interest] concat [updated-poi])

            (not new-poi?)
            (update-in [:page-data :points-of-interest]
                       #(map (fn [poi]
                               (if (= (:id poi) (:id updated-poi))
                                 updated-poi
                                 poi))
                             %)))
      :fx [[:dispatch [:toggle-modal :poi-modal]]]})))

(reg-event-fx
 ::delete-poi
 (fn [{:keys [db]} [_ poi-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/point-of-interest/" poi-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::delete-poi-success poi-id]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-poi-success
 (fn [{:keys [db]} [_ poi-id response]]
   {:db (-> db
            (assoc :action-status :success)
            (update-in [:page-data :points-of-interest] #(remove (fn [poi] (= poi-id (:id poi))) %)))
    :fx [[:dispatch [:toggle-modal :poi-modal]]]}))

(reg-event-fx
 ::select-location
 (fn [{:keys [db]} [_ location-id]]
   {:db (assoc-in db [:page-data :selected-location-id] location-id)
    :update-in-session {:last-viewed-location-id location-id}}))

(reg-event-fx
 ::resume-last-viewed
 (fn [{:keys [db]} [_]]
   (when-let [last-viewed-location-id (some-> (utils/read-from-session) :last-viewed-location-id)]
     {:fx [[:dispatch [::select-location last-viewed-location-id]]]})))
