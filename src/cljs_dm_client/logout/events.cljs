(ns cljs-dm-client.logout.events
  (:require
   [ajax.core :as ajax]
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-fx
 ::logout
 (fn [{:keys [db]} [_]]
   {:http-xhrio {:method          :post
                 :uri             "http://localhost:8090/logout"
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::logout-response-handler true]
                 :on-failure      [::logout-response-handler false]}}))

(reg-event-fx
 ::logout-response-handler
 (fn [{:keys [db]} [_ success? response]]
   (let [auth {:authenticated? false
               :refreshing?    false
               :expires        nil
               :user           nil
               :retry-queue    []}]
     {:db                (assoc db :auth auth)
      :navigate          :login
      :update-in-session {:auth              auth
                          :selected-campaign nil
                          :campaign-setting  nil}})))
