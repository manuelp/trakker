(ns trakker.routes.home
  (:use compojure.core)
  (:require [trakker.views.layout :as layout]
            [trakker.util :as util]
            [trakker.models.db :as db]
            [trakker.models.format :as fmt]
            [clj-time.core :as t]
            [noir.response :as resp]))

(defn home-page [& [error]]
  (layout/render "home.html" {:error error}))

(defn tracking-page [id]
  (let [entry ((comp fmt/format-dates db/calc-duration) (db/get-entry id))]
    (layout/render "tracking.html" entry)))

(defn about-page []
  (layout/render "about.html"))

(defn start-tracking [desc]
  (cond (empty? desc) (home-page "Description can't be empty!")
        :else (let [start (t/now)
                    ; Extract fn for taking new ID
                    id (first (vals (db/log-time {:desc desc
                                                  :start start})))]
                (resp/redirect (str "/tracking/" id)))))

(defn stop-tracking [id]
  (do (db/stop-tracking id (t/now))
      (resp/redirect "/")))

(defn cancel-tracking [id redirect]
  (do (db/rm-log id)
      (resp/redirect redirect)))

(defn report-day [dt]
  (let [tasks (map (comp fmt/format-dates db/calc-duration)
                   (db/timelog-day dt))]
    (layout/render "report-day.html" {:tasks tasks})))

(comment
  (db/timelog-day (t/now))

  (map db/calc-duration (db/timelog-day (t/now))))

(defn report-day-aggregated [dt]
  (layout/render "report-day-aggregated.html"
                 {:tasks (map fmt/format-duration (db/timelog-day-aggregated dt))}))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/" [desc] (start-tracking desc))
  (GET "/tracking/:id" [id] (tracking-page id))
  (POST "/stop" [id] (stop-tracking id))
  (GET "/cancel/:id" [id] (cancel-tracking id "/"))
  (GET "/delete/:id" [id] (cancel-tracking id "/reports/today"))
  (GET "/reports/today" [] (report-day (t/now)))
  (GET "/reports/today-aggregated" [] (report-day-aggregated (t/now)))
  (GET "/about" [] (about-page)))
