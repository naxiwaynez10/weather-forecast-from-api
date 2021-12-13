(ns ^:figwheel-hooks naxiwaynez.weatherforcast
  (:require
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.string :as str]
   [ajax.core :as ajax]))
;; In Namespace: naxiwaynez
;;; Use closure -M:fig:build to build and start your server
;; @params 
;;timing tells which url to use e.g daily or hourly
;: pi-key is the key used to fetch data from online url: https://api.openweathermap.org/data/2.5/forecast/daily?q=London&units=metric&cnt=7&appid={API key}. though it works but i had to use offline dataset
;; url tell which to used
;; ctn : number of results to query at a time
(defonce app-state (r/atom {:title "Nasiru's Weather Forcast"
                            :timing ""
                            :api-key ""
                            :url ""
                            :which ""
                            :ctn ""
                            :temperatures {:day {:today {:label "Today"
                                                            :value nil}
                                                 :tomorrow {:label "Tomorrow"
                                                            :value nil}}
                                          :week {:day3 {:label "Day3"
                                                         :value nil}
                                                  :day4 {:label "Day4"
                                                         :value nil}
                                                  :day5 {:label "Day5"
                                                         :value nil}
                                                  :day6 {:label "Day6"
                                                         :value nil}
                                                  :day7 {:label "Day7"
                                                         :value nil}}
                                           :hours {:now {:label "Now"
                                                         :value nil}
                                                   :hr2 {:label "1 hrs"
                                                         :value nil}
                                                   :hr3 {:label "2 hrs"
                                                         :value nil}}}}))

(defonce country (r/atom {:fields ["Select an option," "For the next 3 hrs,next-3-hours" "For today and tomorrow,today-and-tomorrow", "For a week,week"]}))


(defn check [res]
  ;; split each line and replace every " with ""
  (swap! country
         assoc :fields (rest (str/split-lines (str/replace res "\"" "")))))

;; function to handle the weather api call
(defn handler [resp]

  (cond
    (identical? "today-tomorrow" (:timing @app-state)) 
    ((let [today (get-in resp ["list" 0 "temp" "day"])
        tomorrow (get-in resp ["list" 1 "temp" "day"])]
    (swap! app-state
           update-in [:temperatures :day :today :value] (constantly today))
    (swap! app-state
           update-in [:temperatures :day :tomorrow :value] (constantly tomorrow))))
  
    (identical? "week" (:timing @app-state)) 
    ((let [today (get-in resp ["list" 0  "temp", "day"])
           tomorrow (get-in resp ["list" 1 "temp", "day"])
           day3 (get-in resp ["list" 2 "temp", "day"])
           day4 (get-in resp ["list" 3 "temp", "day"])
           day5 (get-in resp ["list" 4 "temp", "day"])
           day6 (get-in resp ["list" 5 "temp", "day"])
           day7 (get-in resp ["list" 6 "temp", "day"])]
    (swap! app-state
           update-in [:temperatures :day :today :value] (constantly today))
    (swap! app-state
           update-in [:temperatures :day :tomorrow :value] (constantly tomorrow))
    (swap! app-state
           update-in [:temperatures :week :day3 :value] (constantly day3))
    (swap! app-state
           update-in [:temperatures :week :day4 :value] (constantly day4))
    (swap! app-state
           update-in [:temperatures :week :day5 :value] (constantly day5))
    (swap! app-state
           update-in [:temperatures :week :day6 :value] (constantly day6))
    (swap! app-state
           update-in [:temperatures :week :day7 :value] (constantly day7))))
    
        (identical? "hours" (:timing @app-state)) 
    ((let [now (get-in resp ["list" 0 "main" "temp"])
           hr1 (get-in resp ["list" 1 "main" "temp"])
           hr2 (get-in resp ["list" 2 "main" "temp"])]
    (swap! app-state
           update-in [:temperatures :hours :now :value] (constantly now))
    (swap! app-state
           update-in [:temperatures :hours :hr2 :value] (constantly hr1))
    (swap! app-state
           update-in [:temperatures :hours :hr3 :value] (constantly hr2))))
    
    ))

;; Call to country csv file
(defn get-countries! []

    (ajax/GET "https://raw.githubusercontent.com/icyrockcom/country-capitals/master/data/country-list.csv"
       {:handler check}))

;; Call to weather api
(defn get-temperatures! []
  (.alert js/window (:url @app-state))
  (ajax/GET (:url @app-state)
    {:params {"q" (:which @app-state)
              "units" "imperial" ;; alternatively, use "metric"
              "appid" (:api-key @app-state)
              "cnt" (:ctn @app-state)}
     :handler handler}))

;; function to choose which url to use depending on the selected input
(defn which! [evt]
  
  (swap! app-state
         assoc :which  (.-value (.-target evt)))
(get-countries!)
  (cond
   (identical? "week" (:which @app-state)) (swap! app-state
                                             assoc :timing "week" :ctn 7 :url "daily.json")
  (identical? "next-3-hours" (:which @app-state)) (swap! app-state
                                             assoc :timing "hours" :ctn 3 :url "hourly.json")
  (identical? "today-and-tomorrow" (:which @app-state)) (swap! app-state
                                             assoc :timing "today-tomorrow" :ctn 2 :url "daily.json")
  :else (get-temperatures!)))

;; All options to select
(defn option [country]
  (let [x (str/split country ",")]
       [:option {:value (second x)} (first x)])
  )

(defn title []
  [:h1 (:title @app-state)])


;; Temperature card
(defn temperature [temp]
  [:div {:class "temperature"}
   [:div {:class "value"}
    (:value temp)]
   [:h2 (:label temp)]])

(defn get-app-element []
  (gdom/getElement "app"))


;; declearing all dom element in one container for easy mounting
(defn app []  
   
  [:div {:class "postal-code"}

   [title]
 [:div {:class "temperatures"}
  (for [temp (vals (get-in  @app-state [:temperatures :day]))]
    [temperature temp])] 
   [:br]
[:div {:class "temperatures"}
 (for [temp (vals (get-in  @app-state [:temperatures :week]))]
   [temperature temp])]
   [:br]
  [:div {:class "temperatures" }

 (for [temp (vals (get-in  @app-state [:temperatures :hours]))]

   [temperature temp])]
  
   [:h3 "Enter your API Key"]

   [:input {:type "text"

            :placeholder "API KEY here"

            :value (:api-key @app-state)
            :on-change #(swap! app-state assoc :api-key (-> % .-target .-value))}]

   [:select {:id "menu"
             :on-change which!}
    (for [item (:fields @country)]
      (option  item))]
   ])


;; Mount the app container
(defn mount [el]
  (rdom/render [app] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
