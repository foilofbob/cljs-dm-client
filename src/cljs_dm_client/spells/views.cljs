(ns cljs-dm-client.spells.views
  (:require
   [clojure.math :as math]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.spells.events :as events]
   [cljs-dm-client.spells.subs :as subs]))

(defn spells-content []
  (let [spell-search-text @(subscribe [::subs/spell-search-text])
        spells @(subscribe [::subs/spells])]
    [:<>
     [:div.panel-content
      [:div
       [:table.spells
        [:thead
         [:tr
          [:th "Name"]
          [:th "Level"]
          [:th "Cast Time"]
          [:th "Duration"]
          [:th "School"]
          [:th "Range"]
          [:th "Components"]
          [:th "Description"]
          [:th "Higher Casting"]]]
        [:tbody
         (for [{:keys [id name level casting-time duration school range components description higher-casting]} spells]
           [:tr {:key (str "spell-" id)}
            [:td name]
            [:td level]
            [:td casting-time]
            [:td duration]
            [:td school]
            [:td range]
            [:td components]
            [:td description]
            [:td higher-casting]])]]]]
     [:div.right-panel
      [:input {:id          :spell-search-text
               :class       :spell-search
               :value       spell-search-text
               :placeholder "Search..."
               :max-length  30
               :on-change   #(dispatch [::events/spell-search-text (-> % .-target .-value)])}]]]))

(defn spells []
  [loading-wrapper
   {:container [campaign-panel]
    :content   [spells-content]}])
