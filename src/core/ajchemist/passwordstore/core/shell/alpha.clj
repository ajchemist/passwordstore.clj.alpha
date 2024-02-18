(ns ajchemist.passwordstore.core.shell.alpha)


(defn exit!
  [{:keys [exit out err] :as sh-return}]
  (let [output (str err out)]
    (when-not (str/blank? output)
      (println output)))
  (when-not (zero? exit)
    (throw (ex-info "Non-zero exit." sh-return))))
