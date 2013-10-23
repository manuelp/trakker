(ns trakker.routes.home
  (:use compojure.core)
  (:require [trakker.views.layout :as layout]
            [trakker.util :as util]
            [trakker.models.db :as db]
            [trakker.models.format :as fmt]
            [trakker.views.charting :as charts]
            [clj-time.core :as t]
            [noir.response :as resp]))

;; Definition of the tabs to visualize (title, link). Tabs will be displayed in the insertion order declared here.
(def tabs (array-map :home {:title "Home" :url "/"}
                     :today {:title "Today" :url "/reports/today"}
                     :today-aggregated {:title "Today (aggregated)" :url "/reports/today-aggregated"}
                     :search {:title "Search" :url "/search"}
                     :about {:title "About" :url "/about"}))

(defn gen-tabs
  "Transform a map {title link} to {title [link active?]},
  adding a boolean to tell to the view template how to render
  the corresponding tabs."
  [tabs active-tab]
  (letfn [(active? [entry] (= (key entry) active-tab))]
    (reduce #(conj %1 (assoc (val %2) :active (active? %2)))
            []
            tabs)))

(defn home-page [& [error]]
  (layout/render "home.html" {:error error
                              :tabs (gen-tabs tabs :home)}))

(defn tracking-page [id]
  (let [entry ((comp fmt/format-dates db/calc-duration) (db/get-entry id))]
    (layout/render "tracking.html" entry)))

(defn about-page []
  (layout/render "about.html" {:tabs (gen-tabs tabs :about)}))

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

(defn cancel-tracking [id redirect-url]
  (do (db/rm-log id)
      (resp/redirect redirect-url)))

(defn- post-process
  "Calculate duration in minutes, and format some things (duration and dates)."
  [event]
  ((comp fmt/add-formatted-duration
         fmt/format-dates
         db/calc-duration)
   event))

(defn- total-duration [tasks]
  (reduce + (map :duration tasks)))

(defn report-day [dt]
  (let [tasks (map post-process
                   (db/timelog-day dt))
        total (fmt/format-duration (total-duration tasks))]
    (layout/render "report-day.html" {:tasks tasks
                                      :total total
                                      :tabs (gen-tabs tabs :today)})))

(defn report-day-aggregated [dt]
  (let [tasks (map fmt/add-formatted-duration
                   (db/timelog-day-aggregated dt))
        total (fmt/format-duration (total-duration tasks))]
    (charts/save-svg-image "Today"
                           (map :desc tasks)
                           (map :duration tasks)
                           "resources/public/img/today.svg")
    (layout/render "report-day-aggregated.html"
                   {:tasks tasks
                    :total total
                    :tabs (gen-tabs tabs :today-aggregated)})))

(defn search-page []
  (layout/render "search.html" {:tabs (gen-tabs tabs :search)}))

(defn search-results [pattern]
  (let [tasks (map post-process
                   (db/tasks-by-desc-pattern pattern))]
    (layout/render "report-day.html" {:tasks tasks
                                      :tabs (gen-tabs tabs :search)})))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/" [desc] (start-tracking desc))
  (GET "/tracking/:id" [id] (tracking-page id))
  (POST "/stop" [id] (stop-tracking id))
  (GET "/cancel/:id" [id] (cancel-tracking id "/"))
  (GET "/delete/:id" [id] (cancel-tracking id "/reports/today"))

  (GET "/search" [] (search-page))
  (POST "/search" [pattern] (search-results pattern))

  (GET "/reports/today" [] (report-day (t/now)))
  (GET "/reports/today-aggregated" [] (report-day-aggregated (t/now)))
  (GET "/about" [] (about-page)))
