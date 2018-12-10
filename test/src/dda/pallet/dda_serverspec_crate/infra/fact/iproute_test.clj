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

(ns dda.pallet.dda-serverspec-crate.infra.fact.iproute-test
  (:require
   [clojure.string :as string]
   [clojure.test :refer :all]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [dda.pallet.dda-serverspec-crate.infra.fact.iproute :as sut]))

; ------------------------  test data  ------------------------

(def gen-octet (gen/choose 0 255))
(def gen-ipv4 (gen/fmap #(string/join "." %) (gen/vector gen-octet 4)))
(def gen-ipv4-pair (gen/tuple gen-ipv4 gen-ipv4))

(def ipv4->mark (comp name sut/ip-to-keyword))
(defn pair->fact [[ip via]] {(sut/ip-to-keyword ip) {:via via}})
(defn pair->script [[ip via]]
  (str (ipv4->mark ip) "\n"
       ip " via " via " dev enp0s8 table 123 src 192.168.56.101 uid 0
    cache"))

(def gen-fact-and-script (gen/fmap (juxt pair->fact pair->script) gen-ipv4-pair))

; ------------------------  tests  ------------------------------

(def prop-ipv4-parsing
  (prop/for-all
   [[fact script] gen-fact-and-script]
   (= fact (sut/parse-iproute-response script))))

(deftest test-parse
  (testing
    "test parsing iproute output"
    (is (:result (tc/quick-check 10 prop-ipv4-parsing)))))

; ------------------------  test sample  ------------------------

(def script-output1
  "151_101_129_69
151.101.129.69 via 10.0.2.2 dev enp0s3 src 10.0.2.15 uid 0
  cache")

(def fact1
  {:151_101_129_69 {:via "10.0.2.2" :dev "enp0s3" :src "10.0.2.15"}})

(def script-output-ipv6
  "2a00_1450_4001_825__200e
  2a00:1450:4001:825::200e via fe80::ea4:2ff:fe8a:d801 dev ppp0  proto kernel  src 2a02:2698:424:3125::1  metric 1024  mtu 1492 advmss 1432 hoplimit 64")

(def fact-ipv6
  {:2a00_1450_4001_825__200e {:via "fe80::ea4:2ff:fe8a:d801" :dev "ppp0" :src "2a02:2698:424:3125::1"}})

(deftest test-parse
  (testing
    "test parsing iproute output"
    (is (= fact1 (sut/parse-iproute-response script-output1)))
    (is (= fact-ipv6 (sut/parse-iproute-response script-output-ipv6)))))
