(defproject dda/dda-serverspec-crate "2.0.1"
  :description "A crate to get facts from server nodes and test these facst against your expectation."
  :url "https://domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[dda/dda-pallet "4.0.0"]
                 [iproute "0.1.5"]]
  :source-paths ["main/src"]
  :resource-paths ["main/resources"]
  :target-path "target/%s/"
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:dev {:source-paths ["integration/src"
                                  "test/src"
                                  "uberjar/src"]
                   :resource-paths ["integration/resources"
                                    "test/resources"]
                   :dependencies
                   [[dda/pallet "0.9.1" :classifier "tests"]
                    [ch.qos.logback/logback-classic "1.3.0-alpha4"]
                    [org.slf4j/jcl-over-slf4j "1.8.0-beta4"]]
                   :plugins [[lein-sub "0.3.0"]
                             [lein-pprint "1.1.2"]]
                   :leiningen/reply  {:dependencies [[org.slf4j/jcl-over-slf4j "1.8.0-alpha2"
                                                      :exclusions [commons-logging]]]}}
             :test {:test-paths ["test/src"]
                    :resource-paths ["test/resources"]
                    :dependencies [[dda/pallet "0.9.1" :classifier "tests"]
                                   [dda/data-test "0.1.1"]
                                   [org.clojure/test.check "1.1.0"]]}
             :uberjar {:source-paths ["uberjar/src"]
                       :resource-paths ["uberjar/resources"]
                       :aot :all
                       :main dda.pallet.dda-serverspec-crate.main
                       :uberjar-name "dda-serverspec-standalone.jar"
                       :dependencies [[org.clojure/tools.cli "1.0.194"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha4"
                                       :exclusions [com.sun.mail/javax.mail]]
                                      [org.slf4j/jcl-over-slf4j "1.8.0-beta4"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :local-repo-classpath true)
