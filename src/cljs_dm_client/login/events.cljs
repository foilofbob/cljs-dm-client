(ns cljs-dm-client.login.events
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]))

(reg-event-fx
 :login-page-load
 (fn [{:keys [db]} _]
   {:fx [[:dispatch [:page-ready]]]}))

;; TODO: Make proper handler
(defn handle-submit [login-state e]
  (.preventDefault e) ;; Prevent page reload
  (dispatch [::auth login-state]))

(reg-event-fx
 ::auth
 (fn [{:keys [db]} [_ login-state]]
   (let [{:keys [username password]} @login-state]
     {:db         (assoc db :action-status :working)
      :http-xhrio {:method          :post
                   :uri             "http://localhost:8090/login"
                   :params          {:Username username
                                     :Password password}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::auth-success]
                   :on-failure      [:action-failure]}})))

(reg-event-fx
 ::auth-success
 (fn [{:keys [db]} [_ response]]
   (let [auth {:authenticated? true
               :refreshing?    false
               :expires        (:expires response)
               :user           (:username response)
               :retry-queue    []}]
     {:db                (assoc db :auth auth)
      :update-in-session {:auth              auth
                          :selected-campaign nil
                          :campaign-setting  nil}
      :fx                [[:dispatch [:resume-session-if-present]]]})))

(reg-event-fx
 :refresh
 (fn [_ [_]]
   {:http-xhrio {:method          :post
                 :uri             "http://localhost:8090/refresh"
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::refresh-success]
                 :on-failure      [:standard-failure-fx]}}))

(reg-event-fx
 ::refresh-success
 (fn [{:keys [db]} [_ response]]
   (let [auth {:authenticated? true
               :refreshing?    false
               :expires        (:expires response)
               :user           (:username response)
               :retry-queue    []}]
     {:db                (assoc db :auth auth)
      :fx                (mapv #(vector :dispatch %) (some-> db :auth :retry-queue))
      :update-in-session {:auth auth}})))
