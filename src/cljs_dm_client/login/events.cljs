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
   {:db (assoc db :auth {:authenticated? true
                         :expires        (:expires response)
                         :user           (:username response)})
    :fx [[:dispatch [:resume-session-if-present]]]}))
