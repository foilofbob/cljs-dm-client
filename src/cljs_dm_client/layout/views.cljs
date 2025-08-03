(ns cljs-dm-client.layout.views
  (:require
   [cljs-dm-client.campaign-select.events :as campaign-events]
   [cljs-dm-client.navigation.views :refer [nav-button]]
   [re-frame.core :refer [dispatch subscribe] :as re-frame]))

(def page-mappings
  [{:id      :timeline
    :name    "Timeline / Events"}
   {:id      :party
    :name    "Party / Inventory"}
   {:id      :player-stories
    :name    "Player Stories"}
   {:id      :locations
    :name    "Locations"}
   {:id      :xp-tracker
    :name    "XP Tracker"}])

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
     [:div.header-align-left]
     [:h1.current-panel
      (->> page-mappings
           (filter #(= current-panel (:id %)))
           first
           :name)]
     [:div.header-align-right]]))

(defn left-nav []
  (let [current-panel @(subscribe [:active-panel-key])]
    (into [:div.left-nav]
          (for [{id :id name :name handler :handler} page-mappings]
            [nav-button {:key     id
                         :active? (= current-panel id)
                         :content name
                         :handler handler
                         :target  id}]))))

(defn campaign-panel [& panel-content]
  [:div.campaign-manager {:key :campaign-panel}
   [header-content]
   (into [:div.campaign-body-content
          [left-nav]]
         panel-content)])

(def loading-spinner
  [:div.loader {:key :loading-spinner}])

(defn page-error []
  (let [error-str @(subscribe [:page-error])]
    [:div.error {:key :page-error} error-str]))

(defn loading-wrapper [{:keys [loading-handler container content right-panel-content]}]
  (let [loading-status @(subscribe [:loading-status])]
    (into container
          [(case loading-status
             :success content
             :failure page-error
             loading-spinner)])))
