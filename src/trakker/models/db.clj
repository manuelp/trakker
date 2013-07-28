(ns trakker.models.db
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [trakker.models.schema :as schema]
            [clj-time.core :as t]
            [clj-time.coerce :as coerce]
            [clj-time.local :as tl]
            [clj-time.format :as tf])
  (:import java.util.Date))

(def OFFSET 2)

(defdb db schema/db-spec)

(defentity users
  (pk :id)
  (entity-fields :desc :start :end))

(defn create-user [user]
  (insert users
          (values user)))

(defn update-user [id first-name last-name email]
  (update users
  (set-fields {:first_name first-name
               :last_name last-name
               :email email})
  (where {:id id})))

(defn get-user [id]
  (first (select users
                 (where {:id id})
                 (limit 1))))


(defentity timesheets)

(defn- prepare-ts [{start :start end :end :as m}]
  (-> m
      (assoc :start (coerce/to-timestamp start))
      (assoc :end (if end
                    (coerce/to-timestamp end)))))

(defn- timestamp->local-date-time [t]
  (-> t
      coerce/from-sql-date
      coerce/to-date
      tl/to-local-date-time))

(defn- transform-ts
  [{start :start end :end :as m}]
  (-> m
      (assoc :start (timestamp->local-date-time start))
      (assoc :end (if end
                    (timestamp->local-date-time end)))))

(defn log-time [timelog]
  (insert timesheets
          (values (prepare-ts timelog))))

(defn stop-tracking [id dt]
  (update timesheets
          (set-fields {:end (coerce/to-timestamp dt)})
          (where {:id id})))

(defn timelog-day
  "Produces the seq of timesheet entries for the day d."
  [d]
  (let [query (-> (select* timesheets)
                  (where (and (= (sqlfn dayofmonth :start) (t/day d))
                              (= (sqlfn month :start) (t/month d))
                              (= (sqlfn year :start) (t/year d)))))]
    (map transform-ts (exec query))))

(defn calc-duration
  "Produces a new task map with an additional :duration in minutes
  calculated using :start and :end timestamps."
  [{:keys [start end] :as m}]
  (assoc m
    :duration (t/in-minutes (t/interval start end))))

(defn sum-durations
  "Produces the sum of the durations in minutes of all the tasks in the seq."
  [coll]
  (->> coll
       (map calc-duration)
       (map :duration)
       (reduce +)))

(defn timelog-day-aggregated
  "Produces a map (task description -> duration in minutes), aggregating
  all the time logged in the specified date by description."
  [dt]
  (let [grouped (group-by :desc (timelog-day dt))
        sum (reduce #(assoc %1 (key %2) (sum-durations (val %2)))
                    {}
                    grouped)]
    (reduce conj [] (map #(assoc {} :desc (key %) :duration (val %))
                         sum))))

(defn- format-w-timezone [d]
  (tf/unparse (tf/with-zone (tf/formatters :date-hour-minute)
                (t/time-zone-for-offset OFFSET))
              d))

(defn format-dates [{:keys [start end] :as e}]
  (assoc e
    :start (format-w-timezone start)
    :end (when end
           (format-w-timezone end))))

(comment
  (update timesheets
          (set-fields {:end nil})
          (where {:id 2}))

  (stop-tracking 2 (t/now))

  (select timesheets)

  (tl/to-local-date-time (:start (first (select timesheets (where {:id 3})))))


  (import '[java.util.Date])
  (import 'java.sql.Timestamp)

  (-> (tl/local-now)
      coerce/to-sql-date
      coerce/from-sql-date
      coerce/to-date
      tl/to-local-date-time
      )

  (format-dates (first (timelog-day (tl/local-now))))

  (map #(assoc %
          :start (tl/to-local-date-time (:start %)))
       (timelog-day (t/today)))

  (map #(-> % :start tl/to-local-date-time)
       (timelog-day (t/today)))

  (log-time {:start (tl/local-now)
             :desc "Or ora, le 13:24"})


  (require '[clj-time.local :as tl])

  (tl/to-local-date-time (coerce/to-timestamp (t/now)))

  (timelog-day-aggregated (t/now))


  (map rm-log (filter #(> % 2) (map :id (select timesheets))))

  ((comp first vals)
   (log-time {:desc "lsdffg"
              :start (t/now)})))

(defn rm-log [id]
  (delete timesheets
          (where {:id id})))
