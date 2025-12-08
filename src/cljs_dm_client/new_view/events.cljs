(ns cljs-dm-client.new-view.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :new-view-page-load
 (utils/page-loader
  [::page-load-dispatcher]
  [::fetch-locations-success :fetch-notes-success]
  [::fetch-locations-failure :fetch-notes-failure]))

(reg-event-fx
 ::page-load-dispatcher
 (fn [_ _]
     {:dispatch-n [[::fetch-locations]
                   [:fetch-notes "location"]]}))

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
     {:db (utils/standard-success-handler db :locations response)}))

(reg-event-fx
 ::fetch-locations-failure
 utils/standard-failure-handler)