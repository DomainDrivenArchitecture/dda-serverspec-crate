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

(ns dda.pallet.dda-serverspec-crate.app.instantiate-existing
  (:require
   [clojure.inspector :as inspector]
   [pallet.repl :as pr]
   [dda.pallet.commons.session-tools :as session-tools]
   [dda.pallet.commons.pallet-schema :as ps]
   [dda.pallet.commons.operation :as operation]
   [dda.pallet.commons.existing :as existing]
   [dda.pallet.dda-serverspec-crate.app :as app]))

(def provisioning-ip
  "192.168.56.105")

(def provisioning-user
  {:login "initial"
   :password "test1234"})

(def domain-config {:netstat '({:process-name "sshd" :port "11" :running? false}
                               {:process-name "sshd" :port "22"}
                               {:process-name "sshd" :port "22" :exp-proto "tcp6" :ip "::"})
                    :file '({:path "/root"}
                            {:path "/etc"}
                            {:path "/absent" :exist? false})
                    :netcat '({:host "www.google.com" :port 80}
                              {:host "www.google.c" :port 80 :reachable? false})
                    :package '({:name "test" :installed? false}
                               {:name "nano"})})
(defn provider []
  (existing/provider provisioning-ip "node-id" "dda-servertest-group"))

(defn provisioning-spec []
  (merge
   (app/servertest-group-spec (app/app-configuration domain-config))
   (existing/node-spec provisioning-user)))

(defn apply-install
 [& options]
 (let [{:keys [summarize-session]
        :or {summarize-session true}} options]
   (operation/do-apply-install
    (provider)
    (provisioning-spec)
    :summarize-session summarize-session)))

(defn apply-configure
  [& options]
  (let [{:keys [summarize-session]
         :or {summarize-session true}} options]
   (operation/do-apply-configure
    (provider)
    (provisioning-spec)
    :summarize-session summarize-session)))

(defn test
  [& options]
  (let [{:keys [summarize-session]
         :or {summarize-session true}} options]
   (operation/do-server-test
    (provider)
    (provisioning-spec)
    :summarize-session summarize-session)))
