(ns trakker.models.format
  "Some functions to format various things, mainly for UI consumption."
  (:require [clj-time.format :as tf]
            [clj-time.core :as t]))

;; Timezone hours offset over UTC time, used to format date and times.
(def OFFSET 2)

(defn- format-w-timezone
  "Format the given DateTime in a string in the :date-hour-minute format."
  [d]
  (tf/unparse (tf/with-zone (tf/formatters :date-hour-minute)
                (t/time-zone-for-offset OFFSET))
              d))

(defn format-dates
  "Produces a new entry map with :start and :end values transformed to strings,
  taking into account the timezone offset."
  [{:keys [start end] :as e}]
  (assoc e
    :start (format-w-timezone start)
    :end (when end
           (format-w-timezone end))))

(defn format-duration
  "Produces a new entry map, with the :duration value in minutes associated to the :formatted-duration key formatted as 'Xh Ym'."
  [{:keys [duration] :as e}]
  (let [hours (int (/ duration 60))
        minutes (mod duration 60)]
    (assoc e :formatted-duration (str hours "h " minutes "m"))))
