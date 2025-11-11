(ns cljs-dm-client.characters.party.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [cljs-dm-client.characters.events :as events]))

(reg-event-fx
 :party-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-players-success ::events/fetch-items-success :fetch-notes-success]
  [::fetch-players-failure ::events/fetch-items-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
   {:dispatch-n [[::fetch-players]
                 [::events/fetch-items]
                 [:fetch-notes "character"]]}))

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
