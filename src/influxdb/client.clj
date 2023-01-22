(ns influxdb.client
  "When interacting with the InfluxDB HTTP API a \"connection\" needs to be
  provided. A connection is a hash-map describing how to connect to InfluxDB and
  could look like the following:
    {:url \"http://localhost:8086\"
     :token \"foo-token\"
     :org \"some-org\"}
  Only the :url is mandatory."
  (:require [clj-http.client :as http-client]
            [clojure.string :as str]
            [jsonista.core :as json]
            [clojure.data.csv :as csv]))

(def api-path "/api/v2")

(defn read
  "Takes a connection, a flux query q to execute,
   and an optional map of parameters p to inject into the query."
  ([conn q]
   (read conn q {}))
  ([conn q p]
   (let [body (json/write-value-as-string {:query q
                                           :params p})]
     (http-client/post (str (:url conn) api-path "/query")
                       {:headers {:authorization (str "Token " (:token conn))
                                  :content-type "application/json"}
                        :query-params {:org (:org conn)}
                        :body body}))))

(defn write
  "Writes data provided in line protocol to the bucket."
  [conn bucket data]
  (http-client/post
   (str (:url conn) api-path "/write")
   {:headers {:authorization (str "Token " (:token conn))}
    :body data
    :query-params {:org (:org conn)
                   :bucket bucket}}))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword)    ;; Drop if you want string keys instead
            repeat)
	   (rest csv-data)))

(defn unwrap
  "Takes a http response from the API endpoint and converts it to a Clojure data
  structure for convenience."
  [response]
  (-> response
      :body
      csv/read-csv
      csv-data->maps))
