(ns hickeybot.handler
  (:require [cheshire.core :as json]
            [compojure.core :refer [POST defroutes]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as jetty]
            [clj-http.client :as client]
            [jackknife.core :refer (with-timeout)]
            [environ.core :refer (env)]
            [clojure.repl :refer (doc source)])
  (:gen-class))

(def INCOMING-WEBHOOK (env :incoming-webhook))
(def NS-PUBLICS (ns-publics 'clojure.core))

(defmacro with-out-str-and-ret [& body]
  (let [body (cons '(use 'clojure.repl) body)]
    `(let [s# (new java.io.StringWriter)
          ret# (binding [*out* s#]
                 ~@body)]
       (str s# (when-not (nil? ret#)
                 (str "\n" (pr-str ret#)))))))

(defn doc! [text]
  (when text
    (when-let [m (meta (get NS-PUBLICS (symbol text)))]
      (let [[name arglists doc] ((juxt :name :arglists :doc) m)]
        (client/post INCOMING-WEBHOOK
                     {:form-params
                      {:text (str "```" name "\n" arglists "\n" doc "```")}
                      :content-type :json}))
      {:status 200})))

(defn eval!
  "A unsafe and bad thing to do without any sanitization of user input!"
  [text]
  (when text
    (let [res (with-timeout [(* 1000 60)]
                (with-out-str-and-ret (load-string text)))]
      (client/post INCOMING-WEBHOOK
                   {:form-params
                    {:text (str "```hickeybot> " text "\n" res "```")}
                    :content-type :json}))
    {:status 200}))

(defroutes app-routes
  (POST "/hickeybot" [command text :as req]
        (case command
          "/doc" (doc! text)
          "/eval" (eval! text)))
  (route/resources "/")
  (route/not-found "Nope"))

(def app
  (-> app-routes
      (handler/api)))

(defn -main [& [port]]
  (let [port (Integer. (or port 9000))]
    (jetty/run-jetty app {:port port :join? false})))
