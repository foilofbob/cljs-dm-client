(ns cljs-dm-client.logout.views
  (:require
   [cljs-dm-client.logout.events :as events]
   [cljs-dm-client.logout.subs :as subs]
   [re-frame.core :refer [dispatch subscribe]]))

(defn logout-link []
  (let [user @(subscribe [::subs/user])]
    [:div.logout-container
     [:button.action-link {:on-click #(dispatch [::events/logout])}
      (str "Logout " user)]]))
