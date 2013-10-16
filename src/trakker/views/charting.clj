(ns trakker.views.charting
  (:require [incanter.core :as i]
            [incanter.charts :as charts]
            [incanter.svg :as svg :refer [save-svg]]))

(defn- pie-chart
  "Produces a pie chart using the given dataset:

  * `title`: The title of the chart.
  * `labels`: a seq with a label for every value (see below).
  * `values`: a seq of values."
  [title labels values]
  (charts/pie-chart labels values
                    :title title))

(defn save-svg-image
  "Renders an SVG of a pie chart and save it to a file in the root directory of the project."
  [title labels values filename]
  (save-svg (pie-chart title labels values) filename))
