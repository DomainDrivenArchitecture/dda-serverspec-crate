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


(ns dda.pallet.dda-serverspec-crate.infra.fact.netstat-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [dda.pallet.dda-serverspec-crate.infra.fact.netstat :as sut]))

(deftest test-split-line
 (testing
   "test splitting fixed size line"
     (is (= 11
            (count (sut/split-netstat-line (str "tcp        0      0 0.0.0.0:22              0.0.0.0:*"
                                                "               LISTEN      0          9807        1001/sshd")))))
     (is (= 11
            (count (sut/split-netstat-line (str "udp        0      0 0.0.0.0:68              0.0.0.0:*"
                                                "                           0          13182       916/dhclient")))))
     (is (= 11
            (count (sut/split-netstat-line (str "tcp6       0      0 :::22                   :::* "
                                                "                   LISTEN      0          15306       1198/sshd")))))
     (is (= 11
            (count (sut/split-netstat-line (str "111111222222233333334444444444444444444:4444555555555555555555555555"
                                                "666666666666777777777778888888888889999/00000000000")))))))

(defdatatest should-parse [input expected]
  (is (= expected
         (sut/parse-netstat input))))
