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
   [clojure.string :as st]
   [schema.core :as s]
   [dda.config.commons.map-utils :as map-utils]
   [dda.config.commons.schema :as schema]
   [dda.pallet.dda-serverspec-crate.infra :as infra]))

(def fact-id-file infra/fact-id-file)

(def ServerTestDomainConfig
  {(s/optional-key :package) [{:name s/Str
                               (s/optional-key :installed?) s/Bool}]
   (s/optional-key :netstat) [{:process-name s/Str
                               :port s/Str
                               (s/optional-key :running?) s/Bool
                               (s/optional-key :ip) s/Str
                               (s/optional-key :exp-proto) s/Str}]
   (s/optional-key :file) [{:path s/Str
                            (s/optional-key :exist?) s/Bool
                            (s/optional-key :mod) s/Str
                            (s/optional-key :user) s/Str
                            (s/optional-key :group) s/Str
                            (s/optional-key :link-to) s/Str}]
   (s/optional-key :netcat) [{:host s/Str
                              :port s/Num
                              (s/optional-key :reachable?) s/Bool}]
   (s/optional-key :certificate-file) [{:file s/Str               ;incl path as e.g. /path/file.crt
                                        :expiration-days s/Num}]  ;min days certificate must be valid
   (s/optional-key :http) [{:url s/Str                            ;full url e.g. http://google.com
                            :expiration-days s/Num}]              ;minimum days the certificate must be valid
   (s/optional-key :iproute) [(letfn [(either-ip-hostname [m] (or (contains? m :ip) (contains? m :hostname)))]
                                (s/conditional
                                 #(contains? % :ip) {:ip (s/pred schema/ipv4?) :via (s/pred schema/ipv4?)}
                                 #(contains? % :hostname) {:hostname s/Str :via (s/pred schema/ipv4?)}
                                 :else (s/pred either-ip-hostname)))]
   (s/optional-key :command) [{:cmd s/Str
                               :exit-code s/Num
                               (s/optional-key :stdout) s/Str}]})

(def InfraResult {infra/facility infra/ServerTestConfig})

(defn- domain-2-filefacts [file-domain-config]
  (apply merge
         (map
          #(let [path (:path %)] {(infra/path-to-keyword path) {:path path}})
          file-domain-config)))

(defn- domain-2-filetests [file-domain-config]
  (apply merge
         (map
          #(let [{:keys [path exist? mod user group link-to] :or {exist? true}} %
                  test-map (merge {:exist? exist?}
                                  (when (contains? % :mod) {:mod mod})
                                  (when (contains? % :user) {:user user})
                                  (when (contains? % :group) {:group group})
                                  (when (contains? % :link-to) {:type "l"
                                                                :link-to link-to}))]
             {(infra/path-to-keyword path) test-map})
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

(defn- domain-2-netstattests [netstat-domain-config]
  (apply merge
         (map
          #(let [{:keys [process-name port ip exp-proto running?] :or  {running? true}} %
                 test-map (merge {:running? running?
                                  :port port}
                                 (when (contains? % :ip) {:ip ip})
                                 (when (contains? % :exp-proto) {:exp-proto exp-proto}))]
             {(keyword
                (str process-name
                     (if (contains? % :ip) (str "_" (st/replace ip #":" "_") ""))
                     "_" port))
              test-map})
          netstat-domain-config)))

(defn- domain-2-packagetests [package-domain-config]
  (apply merge
         (map
          #(let [{:keys [name installed?] :or {installed? true}} %]
             {(keyword name) {:installed? installed?}})
          package-domain-config)))

(defn- domain-2-certificatefacts [certificate-domain-config]
  (apply merge
         (map
          #(let [{:keys [file expiration-days]} %]
             {(infra/certificate-file-to-keyword file)
              {:file file}})
          certificate-domain-config)))

(defn- domain-2-certificatetests [certificate-domain-config]
  (apply merge
         (map
          #(let [{:keys [file expiration-days] :or {expiration-days 0}} %]
             {(infra/url-to-keyword file) {:expiration-days expiration-days}})
          certificate-domain-config)))

(defn- domain-2-httpfacts [certificate-domain-config]
  (apply merge
         (map
          #(let [{:keys [url expiration-days]} %]
             {(infra/url-to-keyword url)
              {:url url}})
          certificate-domain-config)))

(defn- domain-2-httptests [certificate-domain-config]
  (apply merge
         (map
          #(let [{:keys [url expiration-days] :or {expiration-days 0}} %]
             {(infra/url-to-keyword url) {:expiration-days expiration-days}})
          certificate-domain-config)))

(defn- domain-2-iproutefacts [iproute-config]
  (apply merge
         (map
          #(let [{:keys [ip hostname]} %
                 ips (if hostname (infra/hostname-to-ips hostname) [ip])]
             (->> ips
                  (map (fn [ip] [(infra/ip-to-keyword ip) {:ip ip}]))
                  (into {})))
          iproute-config)))

(defn- domain-2-iproutetests [iproute-config]
  (apply merge
         (map
          #(let [{:keys [ip via hostname]} %
                 ips (if hostname (infra/hostname-to-ips hostname) [ip])]
             (->> ips
                  (map (fn [ip] [(infra/ip-to-keyword ip)
                                 {:via via :source (or hostname ip)}]))
                  (into {})))
          iproute-config)))

(defn- domain-2-commandfacts [command-domain-config]
  (apply merge
         (map
           #(let [{:keys [cmd exit-code stdout]} %]
              {(infra/command-to-keyword cmd)
               {:cmd cmd}})
           command-domain-config)))

(defn- domain-2-commandtests [command-domain-config]
  (apply merge
         (map
           #(let [{:keys [cmd exit-code stdout]} %]
              {(infra/command-to-keyword cmd)
               (merge
                {:exit-code exit-code}
                (when (contains? % :stdout) {:stdout stdout}))})
           command-domain-config)))

(s/defn ^:always-validate
  infra-configuration :- InfraResult
  [domain-config :- ServerTestDomainConfig]
  (let [{:keys [file package netstat netcat certificate-file http iproute command]} domain-config]
    {infra/facility
     (merge
      (if (contains? domain-config :package)
        {:package-fact nil
         :package-test (domain-2-packagetests package)}
        {})
      (if (contains? domain-config :netstat)
        {:netstat-fact nil
         :netstat-test (domain-2-netstattests netstat)}
        {})
      (if (contains? domain-config :file)
        {:file-fact (domain-2-filefacts file)
         :file-test (domain-2-filetests file)}
        {})
      (if (contains? domain-config :netcat)
        {:netcat-fact (domain-2-netcatfacts netcat)
         :netcat-test (domain-2-netcattests netcat)}
        {})
      (if (contains? domain-config :certificate-file)
        {:certificate-file-fact (domain-2-certificatefacts certificate-file)
         :certificate-file-test (domain-2-certificatetests certificate-file)}
        {})
      (if (contains? domain-config :http)
        {:http-fact (domain-2-httpfacts http)
         :http-test (domain-2-httptests http)}
        {})
      (if (contains? domain-config :iproute)
        {:iproute-fact (domain-2-iproutefacts iproute)
         :iproute-test (domain-2-iproutetests iproute)}
        {})
      (if (contains? domain-config :command)
        {:command-fact (domain-2-commandfacts command)
         :command-test (domain-2-commandtests command)}
        {}))}))
