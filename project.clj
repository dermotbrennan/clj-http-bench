(defproject clj-http-bench "1.0.0-SNAPSHOT"
  :description "A HTTP benchmarking commandline tool written in clojure. Similar to ab"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [clj-http "0.2.6"]]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :main clj-http-bench.core)