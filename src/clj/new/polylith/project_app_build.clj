(ns build
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def lib '{{group}}/{{artifact}})
(def version "{{version}}")
(def main '{{namespace}}.cli.main)

(defn uber "Build the uberjar." [opts]
  (-> opts
      (assoc :lib lib :version version :main main)
      (bb/clean)
      (bb/uber)))
