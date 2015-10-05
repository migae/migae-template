(ns {{ns.sym}}.core
  (:require [;;{{ns}}.dom-helpers :as dom]
            [clojure.string :as str]
              [hipo.core :as hipo]
              [dommy.core :refer-macros [sel sel1]]))

            ;; [goog.events :as evt]
            ;; [goog.ui.Zippy.Events :as ze]
            ;; [goog.ui.Component :as comp]
            ;; [goog.ui.Menu :as menu]
            ;; [goog.debug.DivConsole :as divconsole]
            ;; [goog.debug.FancyWindow :as winconsole]
            ;; [goog.debug.Logger :as log]
            ;; [goog.object :as obj]))

;; see
;; http://closure-library.googlecode.com/git/closure/goog/demos/debug.html
;; http://closure-library.googlecode.com/git/closure/goog/demos/menu.html

(def logger (log/getLogger "{{ns}}.core"))
(def EVENTS (obj/getValues comp/EventType))

(defn logEvent
  [e]
  (let [
        name (if-let [cap (.getCaption (.-target e))]
               cap "Menu")]
    (.info logger (str "event: <" (.-type e) "> dispatched by " name))))

(defn init-logger []
  (let [;; make sure popups are not blocked
        winconsole (goog.debug.FancyWindow. "{{ns}}.core")]
    (do
      (.setLevel logger goog.debug.Logger.Level/FINE)
      (.init winconsole)
      (.setEnabled winconsole true)
      (set! (.-showSeverityLevel (.getFormatter winconsole)) true)
      (.fine logger
                (str "Listening for: " (str/join EVENTS ", ") "."))))
  )

(defn ^:export main
  []
  (do (init-logger)
      (let [el3 (dom/get-element :theMenu)
            m3 (goog.ui.Menu.)]
        (do
          (.decorate m3 el3)
          (evt/listen m3 EVENTS logEvent)))))

(main)

