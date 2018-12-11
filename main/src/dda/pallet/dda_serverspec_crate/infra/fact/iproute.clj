; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.


(ns dda.pallet.dda-serverspec-crate.infra.fact.iproute
  (:require
    [clojure.edn :as edn]
    [clojure.string :as string]
    [clojure.tools.logging :as logging]
    [pallet.actions :as actions]
    [iproute.route :as route]
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.core.fact :as fact])
  (:import [java.net InetAddress]))

(def fact-id-iproute ::iproute)

(s/defn ip? [s :- s/Str] (not (empty? (route/parses s :start :ip))))

(def output-separator "----- iproute output separator -----")

(def IprouteFactConfig {s/Keyword {:ip (s/pred ip?) :version s/Int}})

(def IprouteFactResult {:ip (s/pred ip?)
                        :version s/Int
                        (s/optional-key :via) (s/pred ip?)
                        (s/optional-key :src) (s/pred ip?)
                        (s/optional-key :dev) s/Str})
(def IprouteFactResults {s/Keyword IprouteFactResult})

(s/defn ip-to-keyword :- s/Keyword
  [ip :- s/Str]
  (-> ip
      (string/replace #"\." "_")
      (string/replace #"\:" "_")
      keyword))

(defn- ips-by-version [version ^InetAddress a]
  (let [bytesize (case version
                   4 4
                   6 16)]
    (-> a .getAddress count (= bytesize))))

(s/defn hostname-to-ips :- [s/Str]
  [hostname :- s/Str
   version :- s/Int]
  (->> (InetAddress/getAllByName hostname)
       (filter (partial ips-by-version version))
       (map (fn [^InetAddress a] (.getHostAddress a)))))

(s/defn build-iproute-script
  "builds the script to retrieve ip gateways"
  [[iproute-config-key {:keys [ip]}]] ;- MapEntry of IprouteFactConfig
  (str
   "echo '" (name iproute-config-key) "';"
   "ip route get " ip " 2>&1;"
   "echo '" output-separator "'"))

(s/defn parse-iproute-response :- IprouteFactResult
  "returns a IprouteFactResult from the result of a gateway probe"
  [single-script-result :- s/Str]
  (let [[result-key result-text] (string/split single-script-result #"\n" 2)]
    ;; iproute always tries to find multiple rules
    ;; result-text is multiline, e.g.: "151.101.129.69 via 10.0.2.2 dev enp0s3 src 10.0.2.15 uid 0 \n    cache"
    {(keyword result-key) (-> (route/parse result-text)
                              first
                              (select-keys [:via :src :dev]))}))

(s/defn parse-iproute-script-responses :- IprouteFactResults
  "returns a IprouteFactResults from the result gateway probes"
  [raw-script-results :- s/Str]
  (apply merge
         (map (comp parse-iproute-response string/trim)
              (string/split raw-script-results (re-pattern output-separator)))))

(s/defn collect-iproute-fact
  "Collects the file facts."
  [fact-configs :- IprouteFactConfig]
  (fact/collect-fact
   fact-id-iproute
   (str
    (string/join
     "; " (map #(build-iproute-script %) fact-configs))
    "; echo -n ''")
   :transform-fn parse-iproute-script-responses))

(s/defn install
  []
  (actions/package "iproute2"))
