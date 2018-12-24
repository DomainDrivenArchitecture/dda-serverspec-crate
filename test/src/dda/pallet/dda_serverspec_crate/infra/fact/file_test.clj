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

(ns dda.pallet.dda-serverspec-crate.infra.fact.file-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.pallet.dda-serverspec-crate.infra.fact.file :as sut]))

;(s/set-fn-validation! true)

(def one-file
  "/file'17418'mje'mje'600'f''Sun Mar 12 13:16:42.0709127452 2017'Sun Mar 12 13:16:42.0709127452 2017'Sun Mar 12 13:16:51.0409287069 2017'")

(def one-directory
  "/dir'4096'mje'mje'775'd''Sat Mar 11 21:37:35.0220829981 2017'Sat Mar 11 21:37:35.0220829981 2017'Fri May 12 10:31:29.0649385998 2017'")

(def one-linked-file
  "/linked-entry'11'mje'mje'777'l'/link/target/file'Fri May 12 10:22:20.0668140924 2017'Fri May 12 10:22:20.0668140924 2017'Fri May 12 10:22:21.0816135163 2017'")

(def one-linked-dir
  "/linked-dir'12'mje'mje'777'l'/link/target'Fri May 12 10:25:06.0903306720 2017'Fri May 12 10:25:06.0903306720 2017'Fri May 12 10:25:09.0595293205 2017'")

(def not-existing
  "find: `/not-existing`: No such file or directory")

(def not-existing2
  "find: \"/absent\": Datei oder Verzeichnis nicht gefunden")

(def not-existing3
  "find: '/absent': Datei oder Verzeichnis nicht gefunden")

(def some-result
  "/home/gec/test/t1'0'gec'gec'664'f''Fri May 12 19:06:04.0519157000 2017'Fri May 12 19:06:04.0519157000 2017'Fri May 12 19:06:04.0519157000 2017
/home/gec/test/t2'0'gec'gec'664'f''Fri May 12 19:06:04.0519157000 2017'Fri May 12 19:06:04.0519157000 2017'Fri May 12 19:06:04.0519157000 2017
/home/gec/test/t3'0'gec'gec'664'f''Fri May 12 19:06:04.0519157000 2017'Fri May 12 19:06:04.0519157000 2017'Fri May 12 19:06:04.0519157000 2017
find: `/not-existing`: No such file or directory
")

(def issue_11
  "find: '/root/.yarn': No such file or directory
/usr/local/bin/packer'95984305'root'root'755'f''Tue Jun 26 09:50:45.7889129420 2018'Tue May 29 12:26:08.0000000000 2018'Tue May 29 12:26:08.0000000000 2018
/usr/local/bin/aws'814'root'root'755'f''Tue Jun 26 09:50:39.7689119130 2018'Tue Jun 26 09:46:54.0000000000 2018'Tue Jun 26 09:46:54.0000000000 2018
find: '/root/.npm': No such file or directory
/usr/local/bin/check-asg.sh'1954'root'root'775'f''Tue Jun 26 09:50:39.7809119150 2018'Tue Jun 26 09:28:05.0000000000 2018'Tue Jun 26 09:28:05.0000000000 2018
/usr/local/bin/terraform'69122624'root'root'775'f''Tue Jun 26 09:50:50.0969136750 2018'Tue Apr 10 16:52:30.0000000000 2018'Tue Apr 10 16:52:30.0000000000 2018
/usr/local/bin/aws_get_prod_ci_role_session.sh'784'root'root'775'f''Tue Jun 26 09:50:39.7689119130 2018'Tue Jun 26 09:28:05.0000000000 2018'Tue Jun 26 09:28:05.0000000000 2018
/root/.gradle'4096'root'root'755'd''Tue Jun 26 09:52:02.5569255210 2018'Tue Jun 26 09:42:33.0000000000 2018'Tue Jun 26 09:52:02.6729255390 2018
/usr/local/bin/aws_get_session'33'root'root'777'l'/usr/local/bin/aws_get_session.sh'Tue Jun 26 09:50:39.7689119130 2018'Tue Jun 26 09:47:32.0000000000 2018'Tue Jun 26 09:47:32.0000000000 2018
/usr/local/bin/aws_get_session.sh'2312'root'root'775'f''Tue Jun 26 09:50:39.7689119130 2018'Tue Jun 26 09:28:05.0000000000 2018'Tue Jun 26 09:28:05.0000000000 2018
/usr/local/lib/dda-pallet/dda-serverspec.jar'35610432'root'root'644'f''Tue Jun 26 09:50:52.2889140470 2018'Tue Jun 26 09:41:10.0000000000 2018'Tue Jun 26 09:41:10.0000000000 2018
/usr/local/bin/amicleaner'215'root'root'755'f''Tue Jun 26 09:50:39.7609119120 2018'Tue Jun 26 09:48:46.0000000000 2018'Tue Jun 26 09:48:46.0000000000 2018
")

(def empty-result
  "
")


(deftest test-parse-line
  (testing
    "test parsing ls output"
    (is (= "/file"
           (:path (sut/parse-find-line one-file))))
    (is (:fact-exist? (sut/parse-find-line one-file)))
    (is (= "f"
           (:fact-type (sut/parse-find-line one-file))))
    (is (= 17418
           (:fact-size-in-bytes (sut/parse-find-line one-file))))
    (is (= "/link/target"
           (:fact-link-to (sut/parse-find-line one-linked-dir))))
    (is (= "mje"
           (:fact-user (sut/parse-find-line one-linked-dir))))
    (is (= "mje"
           (:fact-group (sut/parse-find-line one-linked-dir))))
    (is (= "777"
           (:fact-mod (sut/parse-find-line one-linked-dir))))
    (is (= {:path "/not-existing" :fact-exist? false}
           (sut/parse-find-line not-existing)))
    (is (= {:path "/absent" :fact-exist? false}
           (sut/parse-find-line not-existing2)))
    (is (= {:path "/absent" :fact-exist? false}
           (sut/parse-find-line not-existing3)))
    (is (not (:fact-exist? (sut/parse-find-line not-existing))))))


(deftest test-parse
  (testing
    "test parsing ls output"
      (is (= 4
             (count (keys (sut/parse-find some-result)))))
      (is (= 12
             (count (keys (sut/parse-find issue_11)))))
      (is (= 0
             (count (keys (sut/parse-find empty-result)))))))
