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

(ns dda.pallet.dda-serverspec-crate.domain
  (:require
   [schema.core :as s]
   [dda.config.commons.map-utils :as map-utils]
   [dda.pallet.dda-serverspec-crate.infra :as infra]))

(def ServerTestDomainConfig
  {(s/optional-key :package) {s/Keyword {:installed? s/Bool}}
   (s/optional-key :netstat) {s/Keyword {:port s/Str}}
   (s/optional-key :file) [{:path s/Str
                            (s/optional-key :exist?) s/Bool}]
   (s/optional-key :netcat) [{:host s/Str
                              :port s/Num
                              (s/optional-key :reachable?) s/Bool}]})

(def InfraResult {infra/facility infra/ServerTestConfig})

(defn- domain-2-filefacts [file-domain-config]
  (apply merge
         (map
          #(let [path (:path %)] {(infra/path-to-keyword path) {:path path}})
          file-domain-config)))

(defn- domain-2-filetests [file-domain-config]
  (apply merge
         (map
          #(let [{:keys [path exist?] :or {exist? true}} %]
             {(infra/path-to-keyword path) {:exist? exist?}})
          file-domain-config)))

(defn- domain-2-netcatfacts [netcat-domain-config]
  (apply merge
         (map
          #(let [{:keys [host port]} %]
             {(keyword (infra/config-to-string host port 8))
              {:host host :port port :timeout 8}})
          netcat-domain-config)))

(defn- domain-2-netcattests [netcat-domain-config]
  (apply merge
         (map
          #(let [{:keys [host port reachable?] :or {reachable? true}} %]
             {(keyword (infra/config-to-string host port 8)) {:reachable? reachable?}})
          netcat-domain-config)))

(s/defn ^:allways-validate infra-configuration :- InfraResult
  [domain-config :- ServerTestDomainConfig]
  (let [{:keys [file package netstat netcat]} domain-config]
    {infra/facility
     (merge
      (if (contains? domain-config :package)
        {:package-fact nil
         :package-test package}
        {})
      (if (contains? domain-config :netstat)
        {:netstat-fact nil
         :netstat-test netstat}
        {})
      (if (contains? domain-config :file)
        {:file-fact (domain-2-filefacts file)
         :file-test (domain-2-filetests file)}
        {})
      (if (contains? domain-config :netcat)
        {:netcat-fact (domain-2-netcatfacts netcat)
         :netcat-test (domain-2-netcattests netcat)}
        {}))}))
