(ns cljs-dm-client.layout.views
  (:require
    [cljs-dm-client.campaign-select.events :as campaign-events]
    [cljs-dm-client.navigation.views :refer [nav-button]]
    [re-frame.core :refer [dispatch subscribe] :as re-frame]))

(def page-mappings
  [{:id      :timeline
    :name    "Timeline / Events"
    :handler identity}
   {:id      :party
    :name    "Party Members"
    :handler identity}
   {:id      :locations
    :name    "Locations"
    :handler identity}])

(defn header-content []
      (let [campaign @(subscribe [:selected-campaign])
            current-panel @(subscribe [:active-panel-key])]
           [:div.header-content
            [:div.campaign
             [:h2.subtitle (:name campaign)]
             [nav-button {:content "Back to Campaign Select"
                          :handler [::campaign-events/select-campaign nil]
                          :target  :campaign-select
                          :link?   true}]]
            [:h1.current-panel
             (->> page-mappings
                  (filter #(= current-panel (:id %)))
                  first
                  :name)]]))

(defn left-nav []
      (into [:div.left-nav]
            (for [{id :id name :name handler :handler} page-mappings]
                 [nav-button {:key     id
                              :content name
                              :handler handler
                              :target  id}])))

(defn campaign-panel [& panel-content]
      [:div.campaign-manager
       [header-content]
       [:div.campaign-body-content
        [left-nav]
        [:div panel-content]]])
