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

(ns dda.pallet.dda-serverspec-crate.infra.test.netstat
  (:require
    [clojure.string :as st]
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.fact.netstat :as netstat-fact]
    [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]))

(def NetstatTestConfig {s/Keyword {:running? s/Bool
                                   :port s/Str
                                   (s/optional-key :ip) s/Str
                                   (s/optional-key :exp-proto) s/Str}})

(s/defn
  strict-service-key :- s/Keyword
  [e :- netstat-fact/NetstatResult]
  (keyword
    (str
      (:fact-process-name e) "_"
      (st/replace (:fact-ip e) #":" "_") "_"
      (:fact-port e))))

(s/defn
  loose-service-key :- s/Keyword
  [e :- netstat-fact/NetstatResult]
  (keyword
    (str
      (:fact-process-name e) "_"
      (:fact-port e))))

(s/defn fact-check :- server-test/TestResult
  [result :- server-test/TestResult
   spec :- NetstatTestConfig
   considered-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          {:keys [port ip exp-proto running?]} (val elem)
          fact-elem  (get-in considered-map [(key elem)])
          {:keys [fact-port fact-ip fact-proto]} fact-elem
          present-running (and
                               (some? fact-elem)
                               running?)
          test-ip (contains? (val elem) :ip)
          test-exp-proto (contains? (val elem) :exp-proto)
          detail-check   (when (some? fact-elem)
                          (and
                            (= port fact-port)
                            (if test-ip (= ip fact-ip) true)
                            (if test-exp-proto (= exp-proto fact-proto) true)))
          passed?   (or
                      (and (= running? false) (if (not detail-check) true false))
                      (and (= running? (some? fact-elem)) detail-check))
          expected-settings (str ", expected:: running?: " running? ", port: " port
                                 (if test-ip (str ", ip: " ip) "")
                                 (if test-exp-proto (str ", protocol: " exp-proto) ""))
          actual-settings (str " - found facts:: running?: " (some? fact-elem)
                               ", port: " fact-port ", ip: " fact-ip ", protocol " fact-proto)]
        (recur
          {:test-passed (and (:test-passed result) passed?)
           :test-message (str (:test-message result) "test netstat: " (name (key elem))
                              expected-settings
                              actual-settings
                              " - passed?: " passed? "\n")
           :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
           :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
          (rest spec)
          considered-map))))

(s/defn result-to-map
  [input :- netstat-fact/NetstatResults]
  (apply merge
          (into
            (map (fn [e] {(loose-service-key e) e}) input)
            (map (fn [e] {(strict-service-key e) e}) input))))

(s/defn test-netstat-internal :- server-test/TestResultHuman
  [test-config :- NetstatTestConfig
   input :- netstat-fact/NetstatResults]
  (let [considered-map (result-to-map input)
        fact-result (fact-check server-test/fact-check-seed test-config considered-map)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-netstat :- server-test/TestActionResult
  [test-config :- NetstatTestConfig]
  (server-test/test-it
    netstat-fact/fact-id-netstat
    #(test-netstat-internal test-config %)))
