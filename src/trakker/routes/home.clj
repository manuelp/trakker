(ns trakker.routes.home
  (:use compojure.core)
  (:require [trakker.views.layout :as layout]
            [trakker.util :as util]
            [trakker.models.db :as db]
            [clj-time.core :as t]))

(defn home-page [& [id error]]
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")
                 :error error
                 :id id}))

(defn about-page []
  (layout/render "about.html"))

(defn start-tracking [desc]
  (cond (empty? desc) (home-page nil "Description can't be empty!")
        :else (let [id (first (vals (db/log-time {:desc desc
                                                  :start (t/now)})))]
                (home-page id))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/" [desc] (start-tracking desc))
  (GET "/about" [] (about-page)))
