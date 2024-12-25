(ns cljs-dm-client.navigation.views
  (:require
    [re-frame.core :refer [dispatch subscribe] :as re-frame]
    [cljs-dm-client.navigation.events :as events]))

(defn nav-button [{:keys [key content handler target link?]}]
  [:button {:class (if link? :nav-link :nav-button)
            :key key
            :on-click #(do (dispatch handler)
                           (dispatch [::events/navigate target]))}
   content])
