(ns blockchain.helper)

(defn add-tail [vect item]
  "Conj is consistent about where to add new element."
  "Make our own function to do that."
  (vec (concat vect [item])))

(defn fix-prc [addr]
  "Fix address without http protocol"
  (if (starts-with? addr "http")
    addr
    (str "http://" addr)))
