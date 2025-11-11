(ns cljs-dm-client.characters.party.views
  (:require
   [clojure.string :refer [lower-case]]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-dm-client.components.notes :refer [build-notes
                                            note-modal]]
   [cljs-dm-client.layout.views :refer [campaign-panel
                                        loading-wrapper]]
   [cljs-dm-client.characters.views :refer [character-modal
                                            character-content
                                            item-component
                                            item-modal]]
   [cljs-dm-client.characters.subs :as subs]))

(defn party-content []
      (let [players      @(subscribe [::subs/players])
            player-items @(subscribe [::subs/items-carried-by-players])]
           (character-content players player-items)))

(defn party []
  [:<>
   [note-modal]
   [item-modal]
   [character-modal]
   [loading-wrapper
    {:container [campaign-panel]
     :content   [party-content]}]])

