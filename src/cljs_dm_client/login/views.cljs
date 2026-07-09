(ns cljs-dm-client.login.views
  (:require
   [cljs-dm-client.login.events :as events]
   [cljs-dm-client.components.components :refer [logical-division]]
   [cljs-dm-client.layout.views :refer [loading-wrapper]]
   [re-frame.core :refer [dispatch
                          subscribe]]
   [reagent.core :as r]))

(defonce login-state (r/atom {:username "" :password ""}))

(defn login-form []
  (let [{:keys [username password]} @login-state]
    [:div.login-form
     [:form {:on-submit (partial events/handle-submit login-state)
             :auto-complete "on"}
      [:div
       [:label {:for :username}
        "Username:"]
       [:input {:id :username
                :type :text
                :name "username"
                :value username
                :on-change #(swap! login-state assoc :username (-> % .-target .-value))}]]
      [:div
       [:label {:for :password}
        "Password:"]
       [:input {:id :password
                :type :password
                :name "password"
                :value password
                :on-change #(swap! login-state assoc :password (-> % .-target .-value))}]]
      [:button.action-button {:type "submit"}
       "Log In"]]]))

(defn login []
  [loading-wrapper
   {:container [:div.login
                [logical-division {:text "Login"}]]
    :content   [login-form]}])
