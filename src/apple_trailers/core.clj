(ns apple-trailers.core
  (:require [org.httpkit.client :as http]
            [hickory.core :as hickory]
            [hickory.select :as s]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.io File]))

(def base-url "http://trailers.apple.com/")

(defn- movie-name
  "Determines the movie name from a trailer URL"
  [url]
  (string/replace (->> (http/get url) deref :body
                       hickory/parse
                       hickory/as-hickory
                       (s/select (s/child (s/tag "title")))
                       first :content first)
                  " - Movie Trailers - iTunes" ""))

(defn- trailer
  "Given a Hickory parsed HTML snippet, produces a hash-map with
  the name of the clip and the highest quality download URL for that trailer"
  [t]
  {:name (-> (s/select (s/child (s/tag "h3")) t)
             first :content first)
   :url (last (map #(get-in % [:attrs :href])
                   (->> (s/select (s/child (s/class "dropdown-download")) t)
                        first
                        (s/select (s/child (s/tag "a"))))))})

(defn- download
  "Download a trailer and save it with the name of the trailer"
  [destination t]
  (io/copy (:body @(http/get (:url t) {:as :byte-array}))
           (File. (str destination "/" (:name t)
                       (re-find #"\.[m]+.*" (:url t))))))

(defn trailers
  "Given a trailer URL, produces a hash-map of the movie name and all trailers"
  [url]
  {:title (movie-name url)
   :trailers (map trailer
                  (->> (http/get (str url "/includes/playlists/itunes.inc"))
                       deref :body
                       hickory/parse
                       hickory/as-hickory
                       (s/select (s/child (s/class "trailer")))))})

(defn search
  "Given a string, returns the trailers of the highest matching query result"
  [s]
  (trailers (->> (-> (http/get (str base-url
                                    "/trailers/home/scripts/quickfind.php")
                               {:query-params {:q s}})
                     deref :body
                     (json/read-str :key-fn keyword)
                     :results first
                     :location)
                 (str base-url))))

(defn download-all
  "Download all trailers from a normalized hash-map"
  ([ts]
   (download-all ts "resources"))
  ([ts destination]
   (.mkdir (File. (str destination "/" (:title ts))))
   (map #(download (str destination "/" (:title ts)) %) (:trailers ts))))
