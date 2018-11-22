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
   [dda.pallet.core.app :as core-app]
   [dda.pallet.dda-serverspec-crate.app :as app]))

(defn install
  [& options]
  (let [{:keys [domain targets summarize-session]
         :or {domain "integration/resources/command.edn"
              targets "integration/resources/remote-pwd-target.edn"
              summarize-session true}} options]
    (core-app/existing-install app/crate-app
                          {:domain domain
                           :targets targets})))

(defn configure
 [& options]
 (let [{:keys [domain targets summarize-session]
        :or {domain "integration/resources/command.edn"
             targets "integration/resources/remote-pwd-target.edn"
             summarize-session true}} options]
  (core-app/existing-configure app/crate-app
                          {:domain domain
                           :targets targets})))

(defn serverspec
  [& options]
  (let [{:keys [domain targets verbosity]
         :or {domain "integration/resources/command.edn"
              targets "integration/resources/remote-pwd-target.edn"
              verbosity 1}} options]
    (core-app/existing-serverspec app/crate-app
                             {:domain domain
                              :targets targets
                              :verbosity verbosity})))
