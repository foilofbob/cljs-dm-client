(ns cljs-dm-client.navigation.views
  (:require
   [re-frame.core :refer [dispatch subscribe] :as re-frame]
   [cljs-dm-client.navigation.events :as events]))

;; Expectation is that the passed in handler accepts a callback
(defn nav-button [{:keys [key content handler target link?]}]
  [:button {:class (if link? :action-link :action-button)
            :key key
            :on-click #(do (if handler
                         (dispatch (conj handler [::events/navigate target]))
                         (dispatch [::events/navigate target])))}
   content])
