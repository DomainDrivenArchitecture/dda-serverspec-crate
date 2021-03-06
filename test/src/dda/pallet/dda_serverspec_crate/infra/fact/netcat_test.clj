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
(ns dda.pallet.dda-serverspec-crate.infra.fact.netcat-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [dda.pallet.dda-serverspec-crate.infra.fact.netcat :as sut]))

; ------------------------  test data  ------------------------
(def netcat-google "www.google.com_80_1\n0")

(def netcat-yahoo "www.yahoo.org_80_1\n1")

(def netcat-bing "www.bing.org_80_1\n2")

(def netcat-invalid "www.google.c_80_8
nc: getaddrinfo: Name or service not known
1")

(def script-results
  (str
    netcat-bing
    sut/output-separator
    netcat-google
    sut/output-separator
    netcat-invalid
    sut/output-separator
    netcat-yahoo
    sut/output-separator))

; ------------------------  tests  ------------------------------
(deftest should-parse-synthetic-results
  (testing "parse single outputs"
    (is (:reachable? (val (first (sut/parse-result netcat-google)))))
    (is (not (:reachable? (val (first (sut/parse-result netcat-yahoo))))))
    (is (= "www.bing.org_80_1" (name (key (first (sut/parse-result netcat-bing)))))))
  (testing
   "test parsing netcat output"
    (is (= 4
           (count (keys (sut/parse-netcat script-results)))))))

(defdatatest should-parse-real-script-results [input expected]
  (is (= expected
         (sut/parse-netcat input))))
