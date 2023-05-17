(ns journey-to-obsidian-cal.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:use [selmer.parser]))

(def test-file-path "../../journey_backup/1558985740837-u16ygino4zy65wqw.json")
(def input-dir "../../journey_backup/")
(def dest-dir "../../Calendar/")

(def test-data (json/read-str (slurp test-file-path)))

;; TODO
;; convert tags to @tags
;; figure out photos
;; create template to write to
;;  - text
;;  - tags
;;  - hidden original extra json blob data?
;;  loop through dir

(get test-data "date_journal")
(get test-data "text")

(defn get-datetime-from-entry [entry-json]
  (java.util.Date. (get entry-json "date_journal")))
;; (get-datetime-from-entry test-data)

(defn get-date-from-entry [entry-json]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd")
           (get-datetime-from-entry entry-json)))
;; (get-date-from-entry (json/read-str (slurp test-file-path)))
(defn get-time-from-entry [entry-json]
  (.format (java.text.SimpleDateFormat. "hh:mm aa")
           (get-datetime-from-entry entry-json)))
;; (get-time-from-entry (json/read-str (slurp test-file-path)))

(defn get-filepath-for-entry [entry-json]
  (let [filename (str (get-date-from-entry entry-json) ".md")]
    ;; check that no existing file exists with that name
    (str dest-dir filename)))



(json/write-str (dissoc test-data "text"))
;; create new file
;;
;; file template
(def journal-template "# {{ date }}
{{ time }}

{{ journal_entry|safe }}

```json
{{ extra_json|json|safe }}
```

")

(defn render-new-entry [data]
  (render journal-template
          {:date (get-date-from-entry data)
           :time (get-time-from-entry data)
           :journal_entry (get data "text")
           :extra_json (dissoc data "text")}))
;; (render-new-entry test-data)

(defn write-new-entry! [data]
  (with-open [w (clojure.java.io/writer  (get-filepath-for-entry data) :append true)]
    (.write w (render-new-entry data))))


(map #(write-new-entry! (json/read-str (slurp %))) (rest (file-seq (clojure.java.io/file input-dir))))

;; (write-new-entry! test-data)
;;=> ("folder"
;;     "mood"
;;     "preview_text"
;;     "music_title"
;;     "timezone"
;;     "music_artist"
;;     "date_journal"
;;     "label"
;;     "tags"
;;     "id"
;;     "date_modified"
;;     "address"
;;     "text"
;;     "lon"
;;     "favourite"
;;     "type"
;;     "lat"
;;     "photos"
;;     "weather"
;;     "sentiment")
