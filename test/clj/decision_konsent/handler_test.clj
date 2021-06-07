(ns decision-konsent.handler-test
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [decision-konsent.handler :refer :all]
    [decision-konsent.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'decision-konsent.config/env
                 #'decision-konsent.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))
  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))
  (testing "register ok"
    (let [response ((app) (-> (request :post "/api/user/register")
                              (json-body {:email "benno@soso", :password "something", :confirm "something"})))]
      (is (= 200 (:status response)))
      (is (= {:message "User registered. Please login."} (m/decode-response-body response)))))
  (testing "login ok"
    (let [response ((app) (-> (request :post "/api/user/login")
                              (json-body {:email "benno@soso", :password "something"})))]
      (is (= 200 (:status response)))
      (is (= {:identity {:email "benno@soso"}} (m/decode-response-body response)))))
  (testing "register fail (user already exists)"
    (let [response1 ((app) (-> (request :post "/api/user/register")
                               (json-body {:email "benno@soso", :password "something", :confirm "something"})))]
      ;; 409 = conflict
      (is (= 409 (:status response1)))
      (is (= {:message "Failed. User already exists."} (m/decode-response-body response1)))))
  (testing "register fail (confirm password wrong"
    (let [response1 ((app) (-> (request :post "/api/user/register")
                               (json-body {:email "bennoWorks@soso", :password "something", :confirm "somethingDifferent"})))]
      ;; 400 = bad request
      (is (= 400 (:status response1)))
      (is (= {:message "Password and confirm do not match."} (m/decode-response-body response1)))))
  (testing "login fail (wrong password)"
    (let [response ((app) (-> (request :post "/api/user/login")
                              (json-body {:email "benno@soso", :password "somethingFAIL"})))]
      ;; 401 = unauthorized
      (is (= 401 (:status response)))
      (is (= {:message "Incorrect login or password."} (m/decode-response-body response)))))
  (testing "login fail (wrong user)"
    (let [response ((app) (-> (request :post "/api/user/login")
                              (json-body {:email "UNKNOWN@soso", :password "something"})))]
      (is (= 401 (:status response)))
      (is (= {:message "Incorrect login or password."} (m/decode-response-body response))))))


#_(testing "parameter coercion error"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (json-body {:x 10, :y "invalid"})))]
      (is (= 400 (:status response)))))

#_(testing "response coercion error"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (json-body {:x -10, :y 6})))]
      (is (= 500 (:status response)))))

#_(testing "content negotiation"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (body (pr-str {:x 10, :y 6}))
                              (content-type "application/edn")
                              (header "accept" "application/transit+json")))]
      (is (= 200 (:status response)))
      (is (= {:total 16} (m/decode-response-body response)))))

#_(testing "services"

    (testing "success"
      (let [response ((app) (-> (request :post "/api/math/plus")
                                (json-body {:x 10, :y 6})))]
        (is (= 200 (:status response)))
        (is (= {:total 16} (m/decode-response-body response)))))

    (testing "parameter coercion error"
      (let [response ((app) (-> (request :post "/api/math/plus")
                                (json-body {:x 10, :y "invalid"})))]
        (is (= 400 (:status response)))))

    (testing "response coercion error"
      (let [response ((app) (-> (request :post "/api/math/plus")
                                (json-body {:x -10, :y 6})))]
        (is (= 500 (:status response)))))

    (testing "content negotiation"
      (let [response ((app) (-> (request :post "/api/math/plus")
                                (body (pr-str {:x 10, :y 6}))
                                (content-type "application/edn")
                                (header "accept" "application/transit+json")))]
        (is (= 200 (:status response)))
        (is (= {:total 16} (m/decode-response-body response))))))
