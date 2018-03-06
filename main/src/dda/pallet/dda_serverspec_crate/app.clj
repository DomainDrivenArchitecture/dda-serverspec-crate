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

(ns dda.pallet.dda-serverspec-crate.app
  (:require
   [schema.core :as s]
   [dda.cm.group :as group]
   ; TODO: validate as soon as pallet-commons issue is fixed
   ;[dda.pallet.commons.session :as session]
   [dda.pallet.commons.existing :as existing]
   [dda.pallet.core.dda-crate :as dda-crate]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.dda-serverspec-crate.infra :as infra]
   [dda.pallet.dda-serverspec-crate.domain :as domain]
   [dda.pallet.commons.external-config :as ext-config]
   [dda.pallet.dda-serverspec-crate.app.summary :as summary]))

(def with-serverspec infra/with-serverspec)

(def ServerSpecDomainConfig domain/ServerTestDomainConfig)

(def InfraResult domain/InfraResult)

(def ServertestAppConfig
  {:group-specific-config
   {s/Keyword InfraResult}})

(s/defn ^:always-validate
  load-targets :- existing/Targets
  [file-name :- s/Str]
  (existing/load-targets file-name))

(s/defn ^:always-validate
  load-domain :- ServerSpecDomainConfig
  [file-name :- s/Str]
  (ext-config/parse-config file-name))

(s/defn ^:always-validate
  create-app-configuration :- ServertestAppConfig
  [config :- infra/ServerTestConfig
   group-key :- s/Keyword]
  {:group-specific-config
     {group-key config}})

(s/defn ^:always-validate
  app-configuration :- ServertestAppConfig
  [domain-config :- ServerSpecDomainConfig
   & options]
  (let [{:keys [group-key]
         :or  {group-key :dda-servertest-group}} options]
    {:group-specific-config
       {group-key (domain/infra-configuration domain-config)}}))

(s/defn ^:always-validate
  servertest-group-spec
  [app-config :- ServertestAppConfig]
  (group/group-spec
    app-config [(config-crate/with-config app-config)
                with-serverspec]))

(s/defn ^:always-validate
  existing-provisioning-spec-resolved
  "Creates an integrated group spec from a domain config and a provisioning user."
  [domain-config :- ServerSpecDomainConfig
   targets-config :- existing/TargetsResolved]
  (let [{:keys [existing provisioning-user]} targets-config]
    (merge
     (servertest-group-spec (app-configuration domain-config))
     (existing/node-spec provisioning-user))))

(s/defn ^:always-validate
  existing-provisioning-spec
  "Creates an integrated group spec from a domain config and a provisioning user."
  [domain-config :- ServerSpecDomainConfig
   targets-config :- existing/Targets]
  (existing-provisioning-spec-resolved domain-config (existing/resolve-targets targets-config)))

(s/defn ^:always-validate
  existing-provider-resolved
  [targets-config :- existing/TargetsResolved]
  (let [{:keys [existing provisioning-user]} targets-config]
    (existing/provider {:dda-servertest-group existing})))

(s/defn ^:always-validate
  existing-provider
  [targets-config :- existing/Targets]
  (existing-provider-resolved (existing/resolve-targets targets-config)))

; TODO: validate as soon as pallet-commons issue is fixed
;[session :- session/SessionSpec
(s/defn ^:always-validate
  summarize-test-session
  [session
   & options]
  (apply summary/summarize-test-session (cons session options)))

; TODO: validate as soon as pallet-commons issue is fixed
;[session :- session/SessionSpec]
(s/defn ^:always-validate
  session-passed? :- s/Bool
  [session]
  (apply summary/session-passed? '(session)))
