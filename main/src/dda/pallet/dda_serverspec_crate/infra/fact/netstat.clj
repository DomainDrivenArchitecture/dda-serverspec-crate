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

(ns dda.pallet.dda-serverspec-crate.infra.fact.netstat
  (:require
    [schema.core :as s]
    [clojure.string :as cs]
    [pallet.actions :as actions]
    [dda.pallet.dda-serverspec-crate.infra.core.fact :refer :all]))

(def fact-id-netstat ::netstat)

(def NetstatResult (seq {:fact-proto s/Str
                         :fact-recv-q s/Str
                         :fact-send-q s/Str
                         :fact-ip s/Str
                         :fact-port s/Str
                         :fact-foreign-adress s/Str
                         :fact-state s/Str
                         :fact-user s/Str
                         :fact-inode s/Str
                         :fact-pid s/Str
                         :fact-process-name s/Str}))

(s/defn split-netstat-line :- '(s/Str)
  [line :- s/Str]
  (let [local-address (subs line 20 44)
        parsed-local-adress (re-find (re-matcher #"(.+):(\d+)" local-address))]
    (map cs/trim
      (concat
        [(subs line 0 6) (subs line 6 13) (subs line 13 20)
         (nth parsed-local-adress 1) (nth parsed-local-adress 2)
         (subs line 44 68) (subs line 68 80)
         (subs line 80 91) (subs line 91 103)]
        (cs/split (subs line 103) #"/")))))

(defn parse-netstat
  [netstat-resource]
  (map #(zipmap
          [:fact-proto :fact-recv-q :fact-send-q :fact-ip :fact-port :fact-foreign-adress :fact-state :fact-user :fact-inode :fact-pid :fact-process-name]
          (split-netstat-line (cs/trim %)))
     (drop-while #(not (re-matches #"\s*(tcp|udp).*" %))
       (cs/split netstat-resource #"\n"))))

(defn collect-netstat-fact
  "Defines the netstat resource.
   This is automatically done serverstate crate is used."
  []
  (collect-fact
    fact-id-netstat
    (str "netstat -tulpen" "; echo ''")
    :transform-fn parse-netstat))

(s/defn install
  []
  (actions/package "net-tools"))
