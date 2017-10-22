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
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [dda.pallet.dda-serverspec-crate.infra.fact.netstat :as netstat-fact]
    [dda.pallet.dda-serverspec-crate.infra.core.test :as server-test]))

(def NetstatTestConfig {s/Keyword {:port s/Str :ip s/Str :exp-proto s/Str
                                   :running? s/Bool}})

(s/defn fact-check :- server-test/FactCheckResult
  [result :- server-test/FactCheckResult
   spec :- NetstatTestConfig
   considered-map]
  (if (<= (count spec) 0)
    result
    (let [elem (first spec)
          {:keys [port ip exp-proto running?]} (val elem)
          present-elem  (get-in considered-map [(key elem)])
          {:keys [local-port local-ip proto]} present-elem
          present-running (and
                               (some? present-elem)
                               running?)
          passed? (or
                    (and (= running? false) (= (some? present-elem) false))
                    (and (= running? (some? present-elem))
                       (and
                            (= port local-port)
                            (= ip local-ip)
                            (= exp-proto proto))))
          expected-settings (if present-running
                              (str ", expected settings: port " port ", ip " ip ", protocol " exp-proto)
                              (str ", expected: not running on port " port ", ip " ip ", protocol " exp-proto))
          actual-settings (if present-running
                            (str ", actual settings: port " local-port ", ip " local-ip ", protocol " proto)
                            "")]
      (recur
          {:test-passed (and (:test-passed result) passed?)
           :test-message (str (:test-message result) "test netstat: " (name (key elem))
                              expected-settings
                              ", was running?: " (some? present-elem)
                              actual-settings
                              ", passed?: " passed? "\\n")
           :no-passed (if passed? (inc (:no-passed result)) (:no-passed result))
           :no-failed (if (not passed?) (inc (:no-failed result)) (:no-failed result))}
          (rest spec)
          considered-map))))

(s/defn result-to-map
  [input :- (seq netstat-fact/NetstatResult)]
  (apply merge (map (fn [e] {(keyword (str (:process-name e) "_" (:proto e) "_" (:local-ip e) ":" (:local-port e))) e}) input)))

(s/defn test-netstat-internal :- server-test/TestResult
  [test-config :- NetstatTestConfig
   input :- (seq netstat-fact/NetstatResult)]
  (let [considered-map (result-to-map input)
        fact-result (fact-check server-test/fact-check-seed test-config considered-map)]
    (server-test/fact-result-to-test-result input fact-result)))

(s/defn test-netstat :- server-test/TestActionResult
  [test-config :- NetstatTestConfig]
  (server-test/test-it
    netstat-fact/fact-id-netstat
    #(test-netstat-internal test-config %)))
