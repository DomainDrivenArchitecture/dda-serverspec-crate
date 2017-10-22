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
    [pallet.build-actions :as build-actions]
    [pallet.actions :as actions]
    [dda.pallet.dda-serverspec-crate.infra.test.netstat :as sut]))

(def test-config-1 {:not-running {:port "22" :ip "0.0.0.0" :exp-proto "tcp" :running? true}
                    (keyword "apache2_tcp6_:::80") {:port "80" :ip "::" :exp-proto "tcp6" :running? true}
                    :sshd_tcp_0.0.0.0:22 {:port "22" :ip "0.0.0.0" :exp-proto "tcp" :running? true}})
(def test-config-2 {:not-running {:port "22" :ip "0.0.0.0" :exp-proto "tcp" :running? true}
                    (keyword "apache2_tcp6_:::81") {:port "81" :ip "::" :exp-proto "tcp6" :running? true}
                    :sshd_tcp_0.0.0.0:22 {:port "22" :ip "0.0.0.0" :exp-proto "tcp" :running? true}})

(def input
  '({:foreign-address ":::*",
     :local-ip "::",
     :local-port "80",
     :recv-q "0",
     :inode "44161",
     :state "LISTEN",
     :process-name "apache2",
     :proto "tcp6",
     :pid "4135",
     :send-q "0",
     :user "0"}
    {:foreign-address "0.0.0.0:*",
     :local-ip "0.0.0.0",
     :local-port "22",
     :recv-q "0",
     :inode "10289",
     :state "LISTEN",
     :process-name "sshd",
     :proto "tcp",
     :pid "974",
     :send-q "0",
     :user "0"}))

(deftest test-netstat-internal
 (testing
   "test test-netstat-internal"
   (is (= 0
          (:no-failed (sut/test-netstat-internal {} input))))
   (is (= 1
          (:no-failed (sut/test-netstat-internal test-config-1 input))))
   (is (= 2
          (:no-failed (sut/test-netstat-internal test-config-2 input))))))

(deftest retest-issues
 (testing
   "test issues"
   (is (= 1
          (:no-failed (sut/test-netstat-internal
                       {:sshd {:port 22}}
                       ({:foreign-adress "0.0.0.0:*", :local-adress "0.0.0.0:22",
                         :recv-q "0", :inode "15261", :state "LISTEN",
                         :proto "tcp", :pid "1200", :process-name "sshd",
                         :send-q "0", :user "0"}
                        {:foreign-adress ":::*", :local-adress ":::22", :recv-q "0",
                         :inode "15263", :state "LISTEN", :proto "tcp6", :pid "1200",
                         :process-name "sshd", :send-q "0", :user "0"}
                        {:proto "udp", :recv-q "0", :send-q "0",
                         :local-adress "0.0.0.0:68", :foreign-adress "0.0.0.0:*",
                         :state "0", :user "13122", :inode "919", :pid "dhclient"})))))))
