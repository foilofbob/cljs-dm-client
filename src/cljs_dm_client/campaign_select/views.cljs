(ns cljs-dm-client.campaign-select.views
  (:require
    [re-frame.core :refer [dispatch subscribe] :as re-frame]
    [cljs-dm-client.campaign-select.events :as events]
    [cljs-dm-client.campaign-select.subs :as subs]
    [cljs-dm-client.navigation.views :refer [nav-button]]))

(defn campaign-select []
      (let [campaigns @(subscribe [::subs/campaigns])]
           [:div.select-campaign
            [:h1.title "Campaigns"]

            [:div.campaign-list
             (if (seq campaigns)
               (for [campaign campaigns]
                    [nav-button {:key (:id campaign)
                                 :content (:name campaign)
                                 :handler [::events/select-campaign campaign]
                                 :target :timeline}])
               [:p "No campaigns!"])]

            ;; TODO: Create button
            ]))
