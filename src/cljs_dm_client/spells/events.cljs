(ns cljs-dm-client.spells.events
  (:require
   [ajax.core :as ajax]
   [cljs-dm-client.utils :as utils]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 :spells-page-load
 (utils/page-loader
  [::fetch-spells]
  [::fetch-spells-success]
  [::fetch-spells-failure]))

(reg-event-fx
 ::fetch-spells
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str "http://localhost:8090/spells")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-spells-success]
                 :on-failure      [::fetch-spells-failure]}}))

(reg-event-db
 ::fetch-spells-success
 (fn [db [_ response]]
   (utils/standard-success-handler db :spells response)))

(reg-event-fx
 ::fetch-spells-failure
 utils/standard-failure-handler)

(reg-event-db
 ::spell-search-text
 (fn [db [_ input]]
   (assoc-in db [:page-data :spell-search-text] input)))
