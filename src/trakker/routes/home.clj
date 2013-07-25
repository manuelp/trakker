(ns trakker.routes.home
  (:use compojure.core)
  (:require [trakker.views.layout :as layout]
            [trakker.util :as util]
            [trakker.models.db :as db]
            [clj-time.core :as t]
            [noir.response :as resp]))

(defn home-page [& [error]]
  (layout/render "home.html" {:error error}))

(defn tracking-page [id desc start]
  (layout/render "tracking.html" {:id id
                                  :desc desc
                                  :start start}))

(defn about-page []
  (layout/render "about.html"))

(defn start-tracking [desc]
  (cond (empty? desc) (home-page "Description can't be empty!")
        :else (let [start (t/now)
                    ; Extract fn for taking new ID
                    id (first (vals (db/log-time {:desc desc
                                                  :start start})))]
                (tracking-page id desc start))))

(defn stop-tracking [id]
  (do (db/stop-tracking id (t/now))
      (resp/redirect "/")))

(defn- calc-duration
  "Produces a new task map with an additional :duration in minutes
  calculated using :start and :end timestamps."
  [entry]
  (assoc entry
    :duration (t/in-minutes (t/interval (:start entry) (:end entry)))))

(defn report-day [dt]
  (let [tasks (map calc-duration (db/timelog-day dt))]
    (layout/render "report-day.html"
                   {:tasks tasks})))

(comment
  (db/timelog-day (t/now))

  (map calc-duration (db/timelog-day (t/now))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/" [desc] (start-tracking desc))
  (POST "/stop" [id] (stop-tracking id))
  (GET "/reports/today" [] (report-day (t/now)))
  (GET "/about" [] (about-page)))
