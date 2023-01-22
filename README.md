# InfluxDB client for Clojure

This client library communicates with the [InfluxDB HTTP API][1] (v2) and
is very small. It is still lacking a few debugging features but has the
important things for managing, reading from and writing to databases.

[1]:https://docs.influxdata.com/influxdb/v2.6/api/ 


## Installation

Add the following dependency to your `project.clj` file:

    [fullspectrum/influxdb-client "1.0.0"]

[![Clojars Project](https://img.shields.io/clojars/v/fullspectrum/influxdb-client.svg)](https://clojars.org/fullspectrum/influxdb-client)


## Usage

### Connecting

Specify how the client reaches the InfluxDB API using a hash-map:

```clojure
{:url "http://localhost:8086"
 :token "string"
 :org "string" ;; optional for cloud
 }
```


The following code examples assumes you are in the `user` namespace and have
required the library and a connection representation (`conn`):

    user > (require '[influxdb.client :as client :refer [unwrap query write]])
    nil

    user > (def conn {:url "http://localhost:8086"})
    #'user/conn

If you don't already have an InfluxDB server running Docker can be used:

    docker run -p 8086:8086 -v influxdb:/var/lib/influxdb influxdb


### Read

This corresponds to `GET /query` endpoint. Pass a string flux query and an optional
parameters map.

    user > (unwrap (query conn "<some-flux-here>" {:bucket "bar"}))
    [{:foo 1} {:foo 2}] ...


### Write

If you already have the data you want to write in the [Line Protocol][2]:

    user> (:status (write conn "mydb" "mymeas,mytag=1 myfield=90"))
    204


If not you can use the `convert` namespace to generate Line Protocol syntax from
a hash-map:

    user> (require '[influxdb.convert :as convert])
    nil


The following hash-maps are all valid point representations:

```clojure
;; minimal data required by the Line Protocol
{:meas "cpu"
 :fields {:value 0.64}}

;; now also including a few tags
{:meas "cpu"
 :tags {:host "serverA" :region "us_west"}
 :fields {:value 0.64}}

;; now with multiple fields and different data types along with a timestamp
{:meas "cpu"
 :fields {:value 0.64 :verified true :count 4}
 :time 1434067467000000000}
```

Use `point->line` to construct a sigle line following the Line Protocol format:

    user> (convert/point->line {:meas "cpu" :fields {:value 0.64}})
    "cpu value=0.64"

[2]: https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_reference


The following is an extreeme [example from the InfluxDB website][3] to
demonstrate escaping:

    user> (convert/point->line
           {:meas "\"measurement with quo⚡️es and emoji\""
            :tags {"tag key with sp🚀ces" "tag,value,with\"commas\""}
            :fields {"field_k\\ey" "string field value, only \" need be esc🍭ped"}})
    "\"measurement\\ with\\ quo⚡️es\\ and\\ emoji\",tag\\ key\\ with\\ sp🚀ces=tag\\,value\\,with\"commas\" field_k\\ey=\"string field value, only \\\" need be esc🍭ped\""


[3]: https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_reference#examples-2


### Full circle

To first write data and then extract it from the database again:

    (:status (write conn "mydb" (convert/point->line {:meas "cpu" :fields {:value 0.64}})))
    (unwrap (query conn ::client/read "SELECT * FROM mydb..cpu"))
