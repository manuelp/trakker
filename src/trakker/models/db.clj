(ns trakker.models.db
  (:use korma.core
        [korma.db :only (defdb)])
  (:require [trakker.models.schema :as schema]
            [clj-time.core :as t]
            [clj-time.coerce :as coerce])
  (:import java.util.Date))

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

(defn- timestamp->date-time [t]
  (coerce/from-date t))

(defentity timesheets)

(defn- prepare-ts [{start :start end :end :as m}]
  (-> m
      (assoc :start (coerce/to-timestamp start))
      (assoc :end (if end
                    (coerce/to-timestamp end)))))

(defn- transform-ts
  [{start :start end :end :as m}]
  (-> m
      (assoc :start (timestamp->date-time start))
      (assoc :end (if end
                    (timestamp->date-time end)))))

(defn log-time [timelog]
  (insert timesheets
          (values (prepare-ts timelog))))


(defn stop-tracking [id dt]
  (update timesheets
          (set-fields {:end (coerce/to-timestamp dt)})
          (where {:id id})))


(defn calc-duration
  "Produces a new task map with an additional :duration in minutes
  calculated using :start and :end timestamps."
  [entry]
  (assoc entry
    :duration (t/in-minutes (t/interval (:start entry) (:end entry)))))

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
    (reduce conj [] (map #(assoc {}
                          :desc (key %)
                          :duration (val %))
                         sum))))

(comment
  (update timesheets
          (set-fields {:end nil})
          (where {:id 2}))

  (stop-tracking 2 (t/now))

  (map :id (select timesheets))

  (let [grouped (group-by :desc (timelog-day (t/today)))]
    (reduce #(assoc %1 (key %2) (reduce + (map :duration (map calc-duration (val %2)))))
            {}
            grouped))

  (timelog-day (t/today))

  (timelog-day-aggregated (t/now))

  (select timesheets)

  (map rm-log (filter #(> % 2) (map :id (select timesheets))))

  ((comp first vals)
   (log-time {:desc "lsdffg"
              :start (t/now)})))

(defn rm-log [id]
  (delete timesheets
          (where {:id id})))

(defn timelog-day
  "Produces the seq of timesheet entries for the day d."
  [d]
  (let [query (-> (select* timesheets)
                  (where (and (= (sqlfn dayofmonth :start) (t/day d))
                              (= (sqlfn month :start) (t/month d))
                              (= (sqlfn year :start) (t/year d)))))]
    (map transform-ts (exec query))))
