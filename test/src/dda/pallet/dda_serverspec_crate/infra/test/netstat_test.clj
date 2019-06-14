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

(ns dda.pallet.dda-serverspec-crate.infra.test.netstat-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [dda.pallet.dda-serverspec-crate.infra.test.netstat :as sut]))

(def input
  '({:fact-foreign-adress ":::*",
     :fact-ip "::",
     :fact-port "80",
     :fact-recv-q "0",
     :fact-inode "44161",
     :fact-state "LISTEN",
     :fact-process-name "apache2",
     :fact-proto "tcp6",
     :fact-pid "4135",
     :fact-send-q "0",
     :fact-user "0"}
    {:fact-foreign-adress "0.0.0.0:*",
     :fact-ip "0.0.0.0",
     :fact-port "22",
     :fact-recv-q "0",
     :fact-inode "10289",
     :fact-state "LISTEN",
     :fact-process-name "sshd",
     :fact-proto "tcp",
     :fact-pid "974",
     :fact-send-q "0",
     :fact-user "0"}
    {:fact-foreign-adress ":::*",
     :fact-ip "::",
     :fact-port "22",
     :fact-recv-q "0",
     :fact-inode "10289",
     :fact-state "LISTEN",
     :fact-process-name "sshd",
     :fact-proto "tcp6",
     :fact-pid "974",
     :fact-send-q "0",
     :fact-user "0"}))

(defdatatest should-test-netstat [input expected]
  (is (= expected
         (sut/test-netstat-internal (:test-config input) (:fact-result input)))))
