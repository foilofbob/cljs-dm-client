(ns cljs-dm-client.utils
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]))

(defn tranform-response
  "Takes a response body and updates keys to be kebab-case-keyword"
  [response]
  (cske/transform-keys csk/->kebab-case-keyword response))