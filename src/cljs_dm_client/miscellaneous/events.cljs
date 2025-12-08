(ns cljs-dm-client.miscellaneous.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :miscellaneous-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-categories-success :fetch-notes-success]
  [::fetch-categories-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
   {:dispatch-n [[::fetch-categories]
                 [:fetch-notes "category"]]}))

(reg-event-fx
 ::fetch-categories
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/categories")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-categories-success]
                 :on-failure      [::fetch-categories-failure]}}))

(reg-event-fx
 ::fetch-categories-success
 (fn [{:keys [db]} [_ response]]
   {:db (utils/standard-success-handler db :categories response)
    :fx [[:dispatch [::resume-last-viewed]]]}))

(reg-event-fx
 ::fetch-categories-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::select-category
 (fn [{:keys [db]} [_ category-id]]
   {:db (assoc-in db [:page-data :selected-category-id] category-id)
    :update-in-session {:last-viewed-category-id category-id}}))

(reg-event-fx
 ::resume-last-viewed
 (fn [{:keys [db]} [_]]
   (when-let [last-viewed-category-id (some-> (utils/read-from-session) :last-viewed-category-id)]
     {:fx [[:dispatch [::select-category last-viewed-category-id]]]})))

(reg-event-fx
 ::open-edit-category-modal
 (fn [{:keys [db]} [_ category]]
   (let [edit-category (or category
                           {:campaign-id (-> db :selected-campaign :id)
                            :name ""})]
     {:fx [[:dispatch [:set-edit-object :edit-category edit-category]]
           [:dispatch [:toggle-modal :category-modal]]]})))

(reg-event-fx
 ::edit-category
 (fn [{:keys [db]} [_ category]]
   (let [new-category? (nil? (:id category))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-category? :post :put)
                   :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/category")
                                      (not new-category?)
                                      (str "/" (:id category)))
                   :params          (rename-keys
                                     (cske/transform-keys csk/->PascalCase category)
                                     {:Id :ID :CampaignId :CampaignID})
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-category-success new-category?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-category-success
 (fn [{:keys [db]} [_ new-category? response]]
   (let [updated-category (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-category?
            (update-in [:page-data :categorys] concat [updated-category])

            (not new-category?)
            (update-in [:page-data :categories]
                       #(map (fn [category]
                               (if (= (:id category) (:id updated-category))
                                 updated-category
                                 category))
                             %)))
      :fx [[:dispatch [:toggle-modal :category-modal]]]})))

(reg-event-fx
 ::delete-category
 (fn [{:keys [db]} [_ category-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/category/" category-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::delete-category-success category-id]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-category-success
 (fn [{:keys [db]} [_ category-id response]]
   {:db (-> db
            (assoc :action-status :success)
            (update-in [:page-data :categories] #(remove (fn [category] (= category-id (:id category))) %)))
    :fx [[:dispatch [:toggle-modal :category-modal]]]}))
