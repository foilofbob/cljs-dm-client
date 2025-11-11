(ns cljs-dm-client.characters.npcs.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [cljs-dm-client.characters.events :as events]))

(reg-event-fx
 :npcs-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-npcs-success ::events/fetch-items-success :fetch-notes-success]
  [::fetch-npcs-failure ::events/fetch-items-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
     {:dispatch-n [[::fetch-npcs]
                   [::events/fetch-items]
                   [:fetch-notes "character"]]}))

(reg-event-fx
 ::fetch-npcs
 (fn [{:keys [db]} _]
     {:http-xhrio {:method          :get
                   :uri             (str "http://localhost:8090/campaign/" (-> db :selected-campaign :id) "/npcs")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::fetch-npcs-success]
                   :on-failure      [::fetch-npcs-failure]}}))

(reg-event-db
 ::fetch-npcs-success
 (fn [db [_ response]]
     (utils/standard-success-handler db :npcs response)))

(reg-event-fx
 ::fetch-npcs-failure
 utils/standard-failure-handler)
