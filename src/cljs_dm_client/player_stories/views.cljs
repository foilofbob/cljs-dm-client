(ns cljs-dm-client.player-stories.views
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.components :refer [idx->accent-class
                                                 toggle-container]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal
                                            open-edit-note-modal
                                            standard-note-columns]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.player-stories.subs :as subs]))

(defn player-notes [player]
      (let [notes @(subscribe [:notes-for-ref "PLAYER_STORY" (:id player)])]
           [standard-note-columns {:notes           notes
                                   :container-class :notes-container
                                   :selected-obj    player
                                   :obj-type        "PLAYER_STORY"}]))

(defn player-stories-content []
      (let [players @(subscribe [::subs/players])]
           (into [:div.player-stories-page.panel-content]
                 (for [[idx player] (map-indexed vector players)]
                      [toggle-container {:header-class (idx->accent-class idx)
                                         :title        (:name player)}
                       [player-notes player]]))))

(defn player-stories []
      [:<>
       [note-modal]
       [loading-wrapper
        {:container [campaign-panel]
         :content   [player-stories-content]}]])
