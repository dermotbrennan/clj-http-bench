(ns clj-http-bench.core
  (:require [clj-http.client :as client]
            [clojure.string :as str])
  (:use clojure.tools.cli)
  (:import (java.util.concurrent Executors)))

(defn update_average
  "calculate the new average of a series"
  [current_average num_items new_val]
  (let 
     [new_avg (/ (+ (* current_average num_items) new_val)
                 (inc num_items))]
    ;(println current_average num_items new_val  "Average:" new_avg)
    new_avg
    ))
  
(defn time-request
  [url num_completed_requests average_response_time]
  (let [start (. java.lang.System (clojure.core/nanoTime))]
    (do
      (client/get url)
      (let [timediff (- (. java.lang.System (clojure.core/nanoTime)) start)] 
       (dosync
        (alter average_response_time update_average @num_completed_requests timediff)
        (alter num_completed_requests inc))
       timediff))))

(defn -main
  [& args]
  (let [[options args banner] (cli args
          ["-n" "--requests" "Requests" :default 10 :parse-fn #(Integer. %)]
          ["-c" "--concurrency" "Concurrency" :default 10 :parse-fn #(Integer. %)]
          ["-h" "--help" "Show help" :default false :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    ;;(println options " " args)
    ;; shared state num_completed_requests, average time per request
    ;; each thread starts a timer does the request, stops the timer
    ;; updates the completed requests and updates the average
    ;; another threads updates the commandline with the values of the
    ;; completed requests and the average
    (let [url (first args)
          {num_requests :requests concurrency :concurrency} options
          num_completed_requests (ref 0)
          average_response_time (ref 0.0)]
      (println "Benchmarking:" url)
      (println "Requests:" num_requests)
      (println "Concurrency:" concurrency)
      (if-not (str/blank? url)
        (do
          (println "Doing requests...")
          (time
           (let [pool (Executors/newFixedThreadPool concurrency)
                 tasks (map (fn [t] 
                              (fn []
                                (time-request url num_completed_requests average_response_time)))
                                (range num_requests))]
                 (doseq [future (.invokeAll pool tasks)]
                   (.get future))
                 (.shutdown pool)))
          (println "Completed Requests:" (str (deref num_completed_requests)) "/" num_requests)
          (println "Average Response Time:" (str (/ (deref average_response_time) 1000000)) "ms"))
        (println "Blank url!"))
      )))