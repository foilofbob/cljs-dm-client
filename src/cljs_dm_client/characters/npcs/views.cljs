(ns cljs-dm-client.characters.npcs.views
  (:require
   [clojure.string :refer [lower-case]]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.characters.views :refer [character-content
                                            character-modal
                                            item-component
                                            item-modal]]
   [cljs-dm-client.characters.subs :as subs]))

(defn npcs-content []
      (let [players      @(subscribe [::subs/npcs])
            player-items @(subscribe [::subs/items-carried-by-npcs])]
           (character-content players player-items)))

(defn npcs []
  [:<>
   [note-modal]
   [item-modal]
   [character-modal]
   [loading-wrapper
    {:container [campaign-panel]
     :content   [npcs-content]}]])

