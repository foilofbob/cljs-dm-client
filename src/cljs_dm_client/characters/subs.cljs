(ns cljs-dm-client.characters.subs
  (:require
   [cljs-dm-client.components.forms :refer [build-options-from-list]]
   [re-frame.core :refer [reg-sub]]
   [cljs-dm-client.utils :as utils]
   [clojure.set :refer [rename-keys]]))

(reg-sub
 ::items
 (fn [db]
   (sort-by :name (-> db :page-data :items (or [])))))

(reg-sub
 ::items-not-carried
 :<- [::items]
 (fn [items]
   (filter #(-> % (or 0) :carried-by-id (= 0)) items)))

(reg-sub
 ::items-carried-by-items
 :<- [::items]
 (fn [items]
   (filter #(-> % :carried-by #{"ITEM"}) items)))

(reg-sub
 ::items-as-containers
 :<- [::items]
 (fn [items]
   (filter #(:is-container %) items)))

(reg-sub
 ::containers-as-select-options
 :<- [::items-as-containers]
 build-options-from-list)

(reg-sub
 ::current-party-level
 :<- [:selected-campaign]
 (fn [campaign]
   (utils/level-by-xp (:current-player-xp campaign))))

(reg-sub
 ::spellbooks
 (fn [db]
   (-> db :page-data :spellbooks (or []))))

(reg-sub
 ::spells
 (fn [db]
   (-> db :page-data :spells (or []))))

(reg-sub
 ::complete-spellbooks
 :<- [::spellbooks]
 :<- [::spells]
 (fn [[spellbooks all-spells]]
   (map
    (fn [spellbook]
      (let [spell-ids (->> spellbook :spell-book-entries (mapv :spell-id) set)
            spells (->> spellbook
                        :spell-book-entries
                        (map (fn [entry]
                               (-> (filter #(= (:id %) (:spell-id entry)) all-spells)
                                   first
                                   (assoc :spellbook-entry-id (:id entry))
                                   (assoc :spellbook-id (:id spellbook))))))]
        (assoc spellbook
               :spells
               (rename-keys
                (group-by :level spells)
                {"Cantrip" :cantrip
                 "1st" :first
                 "2nd" :second
                 "3rd" :third
                 "4th" :fourth
                 "5th" :fifth
                 "6th" :sixth
                 "7th" :seventh
                 "8th" :eighth
                 "9th" :ninth}))))
    spellbooks)))

;;;;;;;;;;;;; PLAYERS ;;;;;;;;;;;;;;;

(reg-sub
 ::players
 (fn [db]
   (sort-by :name (-> db :page-data :players (or [])))))

(reg-sub
 ::items-carried-by-players
 :<- [::items]
 (fn [items]
   (filter #(-> % :carried-by #{"PLAYER"}) items)))

(reg-sub
 ::players-as-select-options
 :<- [::players]
 build-options-from-list)

;;;;;;;;;;;;; NPCS ;;;;;;;;;;;;;;;

(reg-sub
 ::npcs
 (fn [db]
   (sort-by :name (-> db :page-data :npcs (or [])))))

(reg-sub
 ::items-carried-by-npcs
 :<- [::items]
 (fn [items]
   (filter #(-> % :carried-by #{"NPC"}) items)))

(reg-sub
 ::npcs-as-select-options
 :<- [::npcs]
 build-options-from-list)

