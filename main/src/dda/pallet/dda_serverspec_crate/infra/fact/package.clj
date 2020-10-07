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

(ns dda.pallet.dda-serverspec-crate.infra.fact.package
  (:require
   [clojure.string :as st]
   [schema.core :as s]
   [dda.pallet.dda-serverspec-crate.infra.core.fact :refer :all]))

(def fact-id-package ::package)

(def PackageResult {:state s/Str
                    :package s/Str
                    :version s/Str
                    :arch s/Str
                    :desc s/Str})

(def PackageResults [PackageResult])

(s/defn cut-off-header :- [s/Str]
  [script-result]
  (drop-while #(re-matches #"\s*(\[sudo\]|Desired|\||\+).*" %)
    (st/split-lines script-result)))

(s/defn zipmap-packages :- PackageResults
  [result-lines :- [s/Str]]
  (map #(zipmap
          [:state :package :version :arch :desc]
          (st/split % #"\s+|/"))
       result-lines))

(s/defn remove-arch-from-name :- PackageResults
  [result-maps :- PackageResults]
  (map
    (fn [e] (do
              (update-in
                e [:package]
                (fn [f] (first (st/split f #":"))))))
    result-maps))

(s/defn parse-package :- PackageResults
  [script-result]
  (-> script-result
      cut-off-header
      zipmap-packages
      remove-arch-from-name))

(defn collect-package-fact
  "Defines the netstat resource.
   This is automatically done serverstate crate is used."
  []
  (collect-fact fact-id-package '("LC_ALL=C" "dpkg" "-l") :transform-fn parse-package))
