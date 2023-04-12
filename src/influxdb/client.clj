(ns influxdb.client
  "Thin wrapper around the InfluxDB java client"
  (:require [clojure.walk :refer [keywordize-keys]])
  (:import (com.influxdb.client InfluxDBClient InfluxDBClientFactory)))

(defn- ->data
  [flux-tables]
  (let [->records (fn [table]   (.getRecords table))
        ->values  (fn [records] (map #(.getValues %) records))
        ->map     (fn [records] (map #(keywordize-keys (into {} %)) records))]
    (->> flux-tables
         (map ->records)
         (map ->values)
         (map ->map))))

(defn make-query-client [url token org bucket]
  (let [token (.toCharArray token)
        ^InfluxDBClient influx-client (InfluxDBClientFactory/create url token org bucket)]
    (.getQueryApi influx-client)))

(defn query [client q]
  (->data (.query client q)))