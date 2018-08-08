(ns blockchain.helper)

(defn cli-inquire [msg]
  (print msg)
  (flush)
  (read-string (read-line)))
