(defproject doccla/influxdb-client "1.0.0"
  :description "Simple clojure InfluxDB client for reading and writing.
  Supports InfluxDB 2.0."
  :url "https://github.com/winterscar/influxdb-client/"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[clj-http "3.9.1"]
                 [metosin/jsonista "0.2.2"]
                 [org.clojure/data.csv "1.0.1"]]
  :profiles {:dev {:dependencies [[clojure.java-time "0.3.2"]
                                  [org.clojure/clojure "1.10.0"]]}})
