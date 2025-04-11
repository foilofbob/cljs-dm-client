(ns cljs-dm-client.party.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :party-page-load
 (utils/page-loader
  {:first-dispatch [::page-load-dispatcher]
   :rules [{:when :seen-all-of? :events [::fetch-players-success ::fetch-items-success :fetch-notes-success] :dispatch [:page-ready]}
           {:when :seen-any-of? :events [::fetch-players-failure ::fetch-items-failure :fetch-notes-failure] :halt? true}]}))

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

;; PUT - update item

;; DELETE - delete item

;; PUT - update player

;; DELETE - player
