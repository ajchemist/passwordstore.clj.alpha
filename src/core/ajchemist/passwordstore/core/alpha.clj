(ns ajchemist.passwordstore.core.alpha
  (:require
   [clojure.string :as str]
   [clojure.java.shell :as jsh]
   )
  (:import
   java.io.File
   ))


#_(defn sh-exit!
  [{:keys [exit out err] :as sh-return}]
  (let [output (str err out)]
    (when-not (str/blank? output)
      (println output)))
  (when-not (zero? exit)
    (throw (ex-info "Non-zero exit." sh-return))))


(defn show
  "Return non-nil only if the secret exists in `pass-name`"
  [pass-name]
  {:pre [(string? pass-name) (not (str/blank? pass-name))]}
  (let [{:keys [exit out err]} (jsh/sh "pass" "show" pass-name)
        err-output             (str err)]
    (when-not (str/blank? err-output) (println err-output))
    (cond
      (not (zero? exit))
      nil

      (or
        (str/index-of out (str pass-name "\n└── "))
        (str/index-of out (str pass-name "\n├── "))) ; dir entry check
      nil

      (re-find #"(?i)content-disposition: attachment;" out)
      (let [tmpf (File/createTempFile "pass-copy-" "")]
        (jsh/sh "gopass" "fscopy" pass-name (.getPath tmpf))
        (slurp tmpf))

      :else (str/trim-newline out))))
