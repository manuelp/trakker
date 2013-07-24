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

(defentity timesheets
  (prepare (fn [{start :start end :end :as v}]
               (-> v
                   (assoc :start (coerce/to-timestamp start))
                   (assoc :end (if end
                                   (coerce/to-timestamp end))))))
  (transform (fn [{start :start end :end :as v}]
               (-> v
                   (assoc :start (timestamp->date-time start))
                   (assoc :end (if end
                                 (timestamp->date-time end)))))))

(defn log-time [timelog]
  (insert timesheets
          (values timelog)))

(defn rm-log [id]
  (delete timesheets
          (where {:id id})))

(defn timelog-day
  "Produces the seq of timesheet entries for the day d."
  [d]
  (-> (select* timesheets)
      (where (and (= (sqlfn dayofmonth :start) (t/day d))
                  (= (sqlfn month :start) (t/month d))
                  (= (sqlfn year :start) (t/year d))))
      ;as-sql
      exec))