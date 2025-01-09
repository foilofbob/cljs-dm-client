(ns cljs-dm-client.utils
  (:require
   [camel-snake-kebab.core :as csk]))

(defn tranform-response
  "Takes a response body and updates keys to be kebab-case-keyword"
  [response]
  (map
    (fn [c]
      (update-keys c
        #(if (keyword? %)
           (csk/->kebab-case-keyword %)
           %)))
    response))