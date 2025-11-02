(ns cljs-dm-client.party.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :party-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-players-success ::fetch-items-success :fetch-notes-success]
  [::fetch-players-failure ::fetch-items-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
   {:dispatch-n [[::fetch-players]
                 [::fetch-items]
                 [:fetch-notes "player"]]}))

(reg-event-fx
 ::fetch-players
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/players")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-players-success]
                 :on-failure      [::fetch-players-failure]}}))

(reg-event-db
 ::fetch-players-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :players response)))

(reg-event-fx
 ::fetch-players-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::fetch-items
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/items")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-items-success]
                 :on-failure      [::fetch-items-failure]}}))

(reg-event-db
 ::fetch-items-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :items response)))

(reg-event-fx
 ::fetch-items-failure
 utils/standard-failure-handler)

(reg-event-fx
 ::open-edit-item-modal
 (fn [{:keys [db]} [_ item]]
   (let [edit-item (or item
                       {:campaign-id (-> db :selected-campaign :id)
                        :name ""
                        :description ""
                        :link ""
                        :rariity ""
                        :cost ""
                        :requirements ""
                        :is-container false
                        :carried-by nil
                        :carried-by-id nil})]
     {:fx [[:dispatch [:set-edit-object :edit-item edit-item]]
           [:dispatch [:toggle-modal :item-modal]]]})))

(reg-event-fx
 ::edit-item
 (fn [{:keys [db]} [_ item]]
   (let [new-item? (nil? (:id item))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-item? :post :put)
                   :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/item")
                                      (not new-item?)
                                      (str "/" (:id item)))
                   :params          (update-in (rename-keys
                                                (cske/transform-keys csk/->PascalCase item)
                                                {:Id :ID :CampaignId :CampaignID :CarriedById :CarriedByID})
                                               [:CarriedByID]
                                               #(some-> % js/parseInt))
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-item-success new-item?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-item-success
 (fn [{:keys [db]} [_ new-item? response]]
   (let [updated-item (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-item?
            (update-in [:page-data :items] concat [updated-item])

            (not new-item?)
            (update-in [:page-data :items]
                       #(map (fn [item]
                               (if (= (:id item) (:id updated-item))
                                 updated-item
                                 item))
                             %)))
      :fx [[:dispatch [:toggle-modal :item-modal]]]})))

(reg-event-fx
 ::delete-item
 (fn [{:keys [db]} [_ item-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/item/" item-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::delete-item-success item-id]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-item-success
 (fn [{:keys [db]} [_ item-id response]]
   {:db (-> db
            (assoc :action-status :success)
            (update-in [:page-data :items] #(remove (fn [item] (= item-id (:id item))) %)))
    :fx [[:dispatch [:toggle-modal :item-modal]]]}))

(reg-event-fx
 ::open-edit-player-modal
 (fn [{:keys [db]} [_ player]]
   (let [edit-player (or player
                         {:campaign-id (-> db :selected-campaign :id)
                          :name ""
                          :race ""
                          :class ""
                          :armor-class 0
                          :hit-points 0
                          :passive-perception 0
                          :strength 10
                          :dexterity 10
                          :constitution 10
                          :intelligence 10
                          :wisdom 10
                          :charisma 10
                          :movement 30
                          :languages ""
                          :proficiencies ""
                          :acrobatics-proficiency-bonus 0
                          :animal-handling-proficiency-bonus 0
                          :arcana-proficiency-bonus 0
                          :athletics-proficiency-bonus 0
                          :deception-proficiency-bonus 0
                          :history-proficiency-bonus 0
                          :insight-proficiency-bonus 0
                          :intimidation-proficiency-bonus 0
                          :investigation-proficiency-bonus 0
                          :medicine-proficiency-bonus 0
                          :nature-proficiency-bonus 0
                          :perception-proficiency-bonus 0
                          :performance-proficiency-bonus 0
                          :persuasion-proficiency-bonus 0
                          :religion-proficiency-bonus 0
                          :sleight-of-hand-proficiency-bonus 0
                          :stealth-proficiency-bonus 0
                          :survival-proficiency-bonus 0})]
     {:fx [[:dispatch [:set-edit-object :edit-player edit-player]]
           [:dispatch [:toggle-modal :player-modal]]]})))

(reg-event-fx
 ::edit-player
 (fn [{:keys [db]} [_ player]]
   (let [new-player? (nil? (:id player))]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          (if new-player? :post :put)
                   :uri             (cond-> (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/character")
                                      (not new-player?)
                                      (str "/" (:id player)))
                   :params          (rename-keys
                                     (cske/transform-keys csk/->PascalCase player)
                                     {:Id :ID :CampaignId :CampaignID})
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::edit-player-success new-player?]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::edit-player-success
 (fn [{:keys [db]} [_ new-player? response]]
   (let [updated-player (utils/tranform-response response)]
     {:db (cond-> (assoc db :action-status :success)

            new-player?
            (update-in [:page-data :players] concat [updated-player])

            (not new-player?)
            (update-in [:page-data :players]
                       #(map (fn [player]
                               (if (= (:id player) (:id updated-player))
                                 updated-player
                                 player))
                             %)))
      :fx [[:dispatch [:toggle-modal :player-modal]]]})))

(reg-event-fx
 ::delete-player
 (fn [{:keys [db]} [_ player-id]]
   {:db         (assoc db :action-status :working)
    :http-xhrio {:method          :delete
                 :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/character/" player-id)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::delete-player-success player-id]
                 :on-failure      [:action-failure]}}))

(reg-event-fx
 ::delete-player-success
 (fn [{:keys [db]} [_ player-id response]]
   {:db (-> db
            (assoc :action-status :success)
            (update-in [:page-data :players] #(remove (fn [player] (= player-id (:id player))) %)))
    :fx [[:dispatch [:toggle-modal :player-modal]]]}))
