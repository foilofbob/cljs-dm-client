(ns cljs-dm-client.xp-tracker.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::xp
 (fn [db]
     (-> db :page-data :experiences (or []))))

(reg-sub
 ::planned-xp
 :<- [::xp]
 (fn [xps]
     (filter #(-> % :finalized not) xps)))

(reg-sub
 ::finalized-xp
 :<- [::xp]
 (fn [xps]
     (->> xps
          (filter #(-> % :finalized true?))
          (sort-by :id)
          reverse)))

(reg-sub
 ::player-count
 (fn [db]
     (or (some-> db :page-data :players count) 0)))

(reg-sub
 ::adjusted-planned-xp-total
 :<- [::planned-xp]
 :<- [::player-count]
 (fn [[planned-xps player-count]]
     (let [summed-xp (or (some->> planned-xps (map :xp) (apply +)) 0)]
          (-> summed-xp (/ player-count) Math/floor))))
