(ns trakker.models.format
  (:require [clj-time.format :as tf]
            [clj-time.core :as t]))

;; Timezone hours offset over UTC time, used to format date and times.
(def OFFSET 2)

(defn- format-w-timezone [d]
  (tf/unparse (tf/with-zone (tf/formatters :date-hour-minute)
                (t/time-zone-for-offset OFFSET))
              d))

(defn format-dates [{:keys [start end] :as e}]
  (assoc e
    :start (format-w-timezone start)
    :end (when end
           (format-w-timezone end))))

(defn format-duration [{:keys [duration] :as e}]
  (let [hours (int (/ duration 60))
        minutes (mod duration 60)]
    (assoc e :duration (str hours "h " minutes "m"))))