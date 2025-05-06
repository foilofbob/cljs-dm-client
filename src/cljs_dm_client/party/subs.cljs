(ns cljs-dm-client.party.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::players
 (fn [db]
     (-> db :page-data :players (or []))))

(reg-sub
 ::items
 (fn [db]
     (-> db :page-data :items (or []))))

(reg-sub
 ::items-not-carried
 :<- [::items]
 (fn [items]
     (filter #(-> % (or 0) :carried-by-id (= 0)) items)))

(reg-sub
 ::items-carried-by-players
 :<- [::items]
 (fn [items]
     (filter #(-> % :carried-by #{"PLAYER"}) items)))

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

(defn- build-options [objects]
       (concat `({:value "" :label ""})
               (->> objects
                    (sort-by :name)
                    (map (fn [obj]
                             {:value (:id obj)
                              :label (:name obj)})))))

(reg-sub
 ::players-as-select-options
 :<- [::players]
 build-options)

(reg-sub
 ::containers-as-select-options
 :<- [::items-as-containers]
 build-options)
