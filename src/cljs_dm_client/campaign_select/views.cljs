(ns cljs-dm-client.campaign-select.views
  (:require
   [cljs-dm-client.campaign-select.events :as events]
   [cljs-dm-client.campaign-select.subs :as subs]
   [cljs-dm-client.components.components :refer [logical-division]]
   [cljs-dm-client.layout.views :refer [loading-wrapper]]
   [cljs-dm-client.navigation.views :refer [nav-button]]
   [re-frame.core :refer [subscribe]]))

(defn campaign-select []
  [loading-wrapper
   {:container [:div.select-campaign
                [logical-division {:text "Campaigns"}]]
    :content   (let [campaigns @(subscribe [::subs/campaigns])]
                    [:div.campaign-list
                     (if (seq campaigns)
                       (for [campaign campaigns]
                            [nav-button {:key     (:id campaign)
                                         :content (:name campaign)
                                         :handler [::events/select-campaign campaign]
                                         :target  :timeline}])
                       [:p "No campaigns!"])])}])
