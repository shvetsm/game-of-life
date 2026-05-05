(ns life
  (:require [clojure.string :as str]))

(defn load-env [path]
  (try
    (->> (slurp path)
         str/split-lines
         (map str/trim)
         (remove #(or (str/blank? %) (str/starts-with? % "#")))
         (map #(str/split % #"=" 2))
         (filter #(= 2 (count %)))
         (into {}))
    (catch Exception _
      {})))

(def env (load-env "../.env"))

(defn env-int [key default]
  (try
    (Integer/parseInt (get env key (str default)))
    (catch Exception _ default)))

(defn env-double [key default]
  (try
    (Double/parseDouble (get env key (str default)))
    (catch Exception _ default)))

(def width (env-int "WIDTH" 40))
(def height (env-int "HEIGHT" 20))
(def speed (env-double "SPEED" 0.15))
(def density (env-double "DENSITY" 0.25))

(defn create-grid []
  (vec
    (for [_ (range height)]
      (vec
        (for [_ (range width)]
          (< (rand) density))))))

(defn alive? [grid x y]
  (get-in grid [y x] false))

(defn count-neighbors [grid x y]
  (count
    (for [dy [-1 0 1]
          dx [-1 0 1]
          :when (not= [dx dy] [0 0])
          :let [nx (+ x dx)
                ny (+ y dy)]
          :when (alive? grid nx ny)]
      true)))

(defn next-cell [grid x y]
  (let [alive (alive? grid x y)
        neighbors (count-neighbors grid x y)]
    (or
      (and alive (or (= neighbors 2) (= neighbors 3)))
      (and (not alive) (= neighbors 3)))))

(defn next-generation [grid]
  (vec
    (for [y (range height)]
      (vec
        (for [x (range width)]
          (next-cell grid x y))))))

(defn clear-screen []
  (print "\033[H\033[2J")
  (flush))

(defn render [grid]
  (clear-screen)
  (doseq [row grid]
    (println
      (apply str
             (map #(if % "█" " ") row)))))

(defn -main []
  (loop [grid (create-grid)]
    (render grid)
    (Thread/sleep (long (* speed 1000)))
    (recur (next-generation grid))))

(-main)