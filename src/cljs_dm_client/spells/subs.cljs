(ns cljs-dm-client.spells.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [clojure.string :as string]))

(reg-sub
 ::spells
 (fn [db]
   (let [search-text (-> db :page-data :spell-search-text)]
     (cond->> (-> db :page-data :spells)
       (seq search-text)
       (filter #(string/includes?
                 (-> % :name string/lower-case)
                 (string/lower-case search-text)))))))

(reg-sub
 ::spell-search-text
 (fn [db]
   (-> db :page-data :spell-search-text (or ""))))
