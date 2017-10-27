# dda-serverspec-crate

[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-serverspec-crate.svg)](https://clojars.org/dda/dda-serverspec-crate)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-serverspec-crate.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-serverspec-crate)

[Meet us on Slack](http://clojurians.herokuapp.com/) | [Meet us on Meetup](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

## compatability
dda-pallet is compatible to the following versions
 * pallet 0.8
 * clojure 1.7
 * (x)ubunutu 16.04

## Usage documentation
This crate provides integration tests for servers. Tests are driven by the pallet-node and executed either remote (by ssh) or on localhost (direct).
Tests are executed as follows:
1. phase settings
   1. execute a minimal bash on system under test
   2. parse script output in clojure.
   3. store parsed output as fact in session
2. phase test
   1. get fact from session
   2. compare expectation against fact

### Define Resources to test
You might also use the whole file as a resource which means we just create a copy of the file:

```clojure
{(s/optional-key :package) {s/Keyword {:installed? s/Bool}}
 (s/optional-key :netstat) {s/Keyword {:port s/Str}}
 (s/optional-key :file) [{:path s/Str
                          (s/optional-key :exist?) s/Bool}]
 (s/optional-key :netcat) [{:host s/Str
                            :port s/Num
                            (s/optional-key :reachable?) s/Bool}]}
```

### Use server-test standalone

```clojure
(ns [your-ns-goes-here]
  (:require
    [dda.cm.operation :as operation]
    [dda.cm.existing :as existing]
    [dda.pallet.dda-serverspec-crate.domain :as domain]))

(def provisioning-ip
  "[your ip]]")

(def provisioning-user
  {:login "[your username]"
   :password "[your password, if your ssh key is not allready authorized]"})

(def domain-config {:netstat {:sshd {:port "22"}}
                    :file {:root-sth {:path "/root"
                                      :exist? true}
                           :etc {:path "/etc"
                                 :exist? true}
                           :absent {:path "/absent"
                                    :exist? false}}})

(defn provider []
  (existing/provider provisioning-ip "[choose a node-id]" "dda-servertest-group"))

(defn integrated-group-spec []
  (merge
    (domain/dda-servertest-group (domain/crate-stack-configuration domain-config))
    (existing/node-spec provisioning-user)))

(defn server-test []
  (operation/do-server-test (provider) (integrated-group-spec)))

(server-test)
```

## License
Published under [apache2.0 license](LICENSE.md)
