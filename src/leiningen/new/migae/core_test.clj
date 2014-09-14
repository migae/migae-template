(ns {{project}}.core-test
  (:use [{{project}}.core])
  (:refer-clojure :exclude [name hash])
  (:import [com.google.appengine.tools.development.testing
            LocalServiceTestHelper
            LocalServiceTestConfig
            LocalMemcacheServiceTestConfig
            LocalMemcacheServiceTestConfig$SizeUnit
            LocalMailServiceTestConfig
            LocalDatastoreServiceTestConfig
            LocalUserServiceTestConfig]
           [com.google.apphosting.api ApiProxy])
  (:require [clojure.test :refer :all]
            [org.mobileink.migae.infix :as infix]
            [org.mobileink.migae.datastore :as ds]
            [org.mobileink.migae.datastore.service :as dss]
            [org.mobileink.migae.datastore.entity :as dse]
            [org.mobileink.migae.datastore.query  :as dsqry]
            [org.mobileink.migae.datastore.key    :as dskey]
            [clojure.tools.logging :as log :only [trace debug info]]))

(defn- ds-fixture
  [test-fn]
  (let [;; environment (ApiProxy/getCurrentEnvironment)
        ;; delegate (ApiProxy/getDelegate)
        helper (LocalServiceTestHelper.
                (into-array LocalServiceTestConfig
                            [(LocalDatastoreServiceTestConfig.)]))]
    (do (.setUp helper)
        (dss/get-datastore-service) 
        (test-fn)
        (.tearDown helper))))

;(use-fixtures :once (fn [test-fn] (dss/get-datastore-service) (test-fn)))
(use-fixtures :each ds-fixture)

(deftest ^:init ds-init
  (testing "DS init"
    (is (= com.google.appengine.api.datastore.DatastoreServiceImpl
           (class (dss/get-datastore-service))))
    (is (= com.google.appengine.api.datastore.DatastoreServiceImpl
           (class @dss/*datastore-service*)))))

;; ################################################################
(deftest ^:fetch entity-map-fetch-1
  (testing "entitymap deftype fetch 1"
    (let [em ^{:_kind :Employee,
               :_name "asalieri1"},
          {:fname "Antonio",
           :lname "Salieri 1"}]
      (let [e (ds/persist em)
            f (ds/fetch {:_kind :Employee :_name "asalieri1"})]
        (is (= (:_kind (meta f))
               :Employee))
        (is (= (:_name (meta f))
               "asalieri1"))
        (is (= (:fname f)
               "Antonio"))
        (is (= (type (:fname f))
               java.lang.String))
        (is (= (:lname f)
               "Salieri 1"))
      ))))
