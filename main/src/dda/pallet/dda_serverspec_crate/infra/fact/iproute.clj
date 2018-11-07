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
    [iproute.route :as route]
    [schema.core :as s]
    [dda.config.commons.schema :as schema]
    [dda.pallet.dda-serverspec-crate.infra.core.fact :as fact]))

(def fact-id-iproute ::iproute)

(def output-separator "----- iproute output separator -----")

(def IprouteFactConfig {s/Keyword {:ip (s/pred schema/ipv4?)}})

(def IprouteFactResult {:ip (s/pred schema/ipv4?)
                      :via (s/pred schema/ipv4?)})
(def IprouteFactResults {s/Keyword IprouteFactResult})

(s/defn ip-to-keyword :- s/Keyword
  [ip :- s/Str] (keyword (string/replace ip #"\." "_")))

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
  (let [result-lines (string/split single-script-result #"\n" 2)
        result-key (first result-lines)
        result-text (nth result-lines 1)]
    ;; iproute always tries to find multiple rules
    {(keyword result-key) (-> (route/parse result-text)
                              first
                              (select-keys [:via]))}))

(s/defn parse-iproute-script-responses :- IprouteFactResults
  "returns a IprouteFactResults from the result gateway probes"
  [raw-script-results :- s/Str]
  (apply merge
         (map parse-iproute-response
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
