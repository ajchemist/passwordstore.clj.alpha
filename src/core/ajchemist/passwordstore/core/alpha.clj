(ns ajchemist.passwordstore.core.alpha
  (:require
   [clojure.string :as str]
   [clojure.java.io :as jio]
   [clojure.java.shell :as jsh]
   [ajchemist.passwordstore.core.shell.alpha :as shell]
   )
  (:import
   java.io.File
   ))


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


(defn generate
  [pass-name]
  (tap> [:info "pass" "generate" pass-name])
  (shell/exit! (jsh/sh "pass" "generate" pass-name)))


(defn fscopy-from-file
  [from to]
  (let [from (.getPath (jio/as-file from))
        to   (str to)]
    (tap> [:info "gopass" "fscopy" from to])
    (shell/exit! (jsh/sh "gopass" "fscopy" from to))))


(defn fscopy-from-vault
  [from to]
  (let [from (str from)
        to   (.getPath (jio/as-file to))]
    (tap> [:info "gopass" "fscopy" from to])
    (shell/exit! (jsh/sh "gopass" "fscopy" from to))))


(defn git-pull
  ([]
   (shell/exit! (jsh/sh "pass" "git" "pull" "origin" "master"))))
