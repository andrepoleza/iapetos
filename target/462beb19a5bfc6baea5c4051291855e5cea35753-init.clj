nil (do (set! *warn-on-reflection* nil) (require (quote clojure.test)) (do (clojure.core/ns leiningen.core.injected) (defn- hooks [v] (-> (clojure.core/deref v) meta :leiningen.core.project/hooks)) (defn- original [v] (-> (clojure.core/deref v) meta :leiningen.core.project/original)) (defn- compose-hooks [f1 f2] (fn [& args] (apply f2 f1 args))) (defn- join-hooks [original hooks] (reduce compose-hooks original hooks)) (defn- run-hooks [hooks original args] (apply (join-hooks original hooks) args)) (defn- prepare-for-hooks [v] (when-not (hooks v) (let [hooks (atom {})] (alter-var-root v (fn [original] (with-meta (fn [& args] (run-hooks (vals (clojure.core/deref hooks)) original args)) (assoc (meta original) :leiningen.core.project/hooks hooks :leiningen.core.project/original original))))))) (defonce hook-scopes []) (defn start-scope [] (locking hook-scopes (alter-var-root (var hook-scopes) conj {}))) (defn- scope-update-fn [scopes target-var] (conj (pop scopes) (update-in (peek scopes) [target-var] (fn* [p1__515#] (if p1__515# p1__515# (clojure.core/deref (hooks target-var))))))) (defn- possibly-record-in-scope [target-var] (locking hook-scopes (when (seq hook-scopes) (alter-var-root (var hook-scopes) scope-update-fn target-var)))) (defn end-scope [] (locking hook-scopes (let [head (peek hook-scopes)] (alter-var-root (var hook-scopes) pop) (doseq [[var old-hooks] head] (reset! (hooks var) old-hooks))))) (defmacro with-scope "Defines a scope which records any change to hooks during the dynamic extent\nof its body, and restores hooks to their original state on exit of the scope." [& body] (clojure.core/seq (clojure.core/concat (clojure.core/list (quote try)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote leiningen.core.project/start-scope))))) body (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote finally)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote leiningen.core.project/end-scope))))))))))) (defn add-hook "Add a hook function f to target-var. Hook functions are passed the\n  target function and all their arguments and must apply the target to\n  the args if they wish to continue execution." ([target-var f] (add-hook target-var f f)) ([target-var key f] (prepare-for-hooks target-var) (possibly-record-in-scope target-var) (swap! (hooks target-var) assoc key f))) (defn- clear-hook-mechanism [target-var] (alter-var-root target-var (constantly (original target-var)))) (defn remove-hook "Remove hook identified by key from target-var." [target-var key] (when-let [hooks (hooks target-var)] (swap! hooks dissoc key) (when (empty? (clojure.core/deref hooks)) (clear-hook-mechanism target-var)))) (defn clear-hooks "Remove all hooks from target-var." [target-var] (when-let [hooks (hooks target-var)] (swap! hooks empty) (clear-hook-mechanism target-var))) (defmacro prepend [target-var & body] (clojure.core/seq (clojure.core/concat (clojure.core/list (quote leiningen.core.project/add-hook)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote var)) (clojure.core/list target-var)))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/fn)) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (quote f__516__auto__)) (clojure.core/list (quote &)) (clojure.core/list (quote args__517__auto__)))))) body (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/apply)) (clojure.core/list (quote f__516__auto__)) (clojure.core/list (quote args__517__auto__))))))))))) (defmacro append [target-var & body] (clojure.core/seq (clojure.core/concat (clojure.core/list (quote leiningen.core.project/add-hook)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote var)) (clojure.core/list target-var)))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/fn)) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (quote f__518__auto__)) (clojure.core/list (quote &)) (clojure.core/list (quote args__519__auto__)))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/let)) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list (quote val__520__auto__)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/apply)) (clojure.core/list (quote f__518__auto__)) (clojure.core/list (quote args__519__auto__))))))))) body (clojure.core/list (quote val__520__auto__))))))))))) (defmacro with-hooks-disabled [f & body] (clojure.core/seq (clojure.core/concat (clojure.core/list (quote do)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/when-not)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote var)) (clojure.core/list (quote leiningen.core.project/hooks))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote var)) (clojure.core/list f))))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote throw)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote java.lang.Exception.)) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/str)) (clojure.core/list "No hooks on ") (clojure.core/list f))))))))))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote clojure.core/with-redefs)) (clojure.core/list (clojure.core/apply clojure.core/vector (clojure.core/seq (clojure.core/concat (clojure.core/list f) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote var)) (clojure.core/list (quote leiningen.core.project/original))))) (clojure.core/list (clojure.core/seq (clojure.core/concat (clojure.core/list (quote var)) (clojure.core/list f))))))))))) body)))))) (clojure.core/ns user)) (clojure.core/let [namespaces658 (clojure.core/reduce (clojure.core/fn [acc__7726__auto__ [f__7727__auto__ args__7728__auto__]] (if (clojure.core/vector? f__7727__auto__) (clojure.core/filter (fn* [p1__7725__7729__auto__] (clojure.core/apply (clojure.core/first f__7727__auto__) p1__7725__7729__auto__ args__7728__auto__)) acc__7726__auto__) acc__7726__auto__)) (quote (iapetos.core.counter-macro-test iapetos.core.counter-test iapetos.core.gauge-test iapetos.core.histogram-test iapetos.core.summary-test iapetos.metric-test iapetos.test.generators)) [[(constantly true) ()]])] (clojure.core/when (clojure.core/seq namespaces658) (clojure.core/apply clojure.core/require :reload namespaces658)) (clojure.core/let [failures__7737__auto__ (clojure.core/atom {}) selected-namespaces__7738__auto__ (clojure.core/distinct (clojure.core/for [ns__7731__auto__ namespaces658 [___7732__auto__ var__7733__auto__] (clojure.core/ns-publics ns__7731__auto__) :when (clojure.core/some (clojure.core/fn [[selector__7734__auto__ args__7735__auto__]] (clojure.core/apply (if (clojure.core/vector? selector__7734__auto__) (clojure.core/second selector__7734__auto__) selector__7734__auto__) (clojure.core/merge (clojure.core/-> var__7733__auto__ clojure.core/meta :ns clojure.core/meta) (clojure.core/assoc (clojure.core/meta var__7733__auto__) :leiningen.test/var var__7733__auto__)) args__7735__auto__)) [[(constantly true) ()]])] ns__7731__auto__)) ___7739__auto__ (clojure.core/when true (leiningen.core.injected/add-hook (var clojure.test/report) (clojure.core/fn [report__7740__auto__ m__7741__auto__ & args__7742__auto__] (clojure.core/when (#{:fail :error} (:type m__7741__auto__)) (clojure.core/when-let [first-var__7743__auto__ (clojure.core/-> clojure.test/*testing-vars* clojure.core/first clojure.core/meta)] (clojure.core/let [ns-name__7744__auto__ (clojure.core/-> first-var__7743__auto__ :ns clojure.core/ns-name clojure.core/name) test-name__7745__auto__ (clojure.core/-> first-var__7743__auto__ :name clojure.core/name)] (clojure.core/swap! failures__7737__auto__ (clojure.core/fn [___7739__auto__] (clojure.core/update-in (clojure.core/deref failures__7737__auto__) [ns-name__7744__auto__] (clojure.core/fnil clojure.core/conj []) test-name__7745__auto__))) (clojure.core/newline) (clojure.core/println "lein test :only" (clojure.core/str ns-name__7744__auto__ "/" test-name__7745__auto__))))) (if (clojure.core/= :begin-test-ns (:type m__7741__auto__)) (clojure.test/with-test-out (clojure.core/newline) (clojure.core/println "lein test" (clojure.core/ns-name (:ns m__7741__auto__)))) (clojure.core/apply report__7740__auto__ m__7741__auto__ args__7742__auto__))))) summary__7746__auto__ (clojure.core/binding [clojure.test/*test-out* clojure.core/*out*] ((clojure.core/fn [namespaces__7708__auto__ selectors__7709__auto__ func__7710__auto__] (clojure.core/let [copy-meta__7711__auto__ (clojure.core/fn [var__7712__auto__ from-key__7713__auto__ to-key__7714__auto__] (clojure.core/if-let [x__7715__auto__ (clojure.core/get (clojure.core/meta var__7712__auto__) from-key__7713__auto__)] (clojure.core/alter-meta! var__7712__auto__ (fn* [p1__7705__7716__auto__] (clojure.core/-> p1__7705__7716__auto__ (clojure.core/assoc to-key__7714__auto__ x__7715__auto__) (clojure.core/dissoc from-key__7713__auto__)))))) vars__7717__auto__ (if (clojure.core/seq selectors__7709__auto__) (clojure.core/->> namespaces__7708__auto__ (clojure.core/mapcat (clojure.core/comp clojure.core/vals clojure.core/ns-interns)) (clojure.core/remove (clojure.core/fn [var__7712__auto__] (clojure.core/some (clojure.core/fn [[selector__7718__auto__ args__7719__auto__]] (clojure.core/let [sfn__7720__auto__ (if (clojure.core/vector? selector__7718__auto__) (clojure.core/second selector__7718__auto__) selector__7718__auto__)] (clojure.core/apply sfn__7720__auto__ (clojure.core/merge (clojure.core/-> var__7712__auto__ clojure.core/meta :ns clojure.core/meta) (clojure.core/assoc (clojure.core/meta var__7712__auto__) :leiningen.test/var var__7712__auto__)) args__7719__auto__))) selectors__7709__auto__))))) copy__7721__auto__ (fn* [p1__7706__7722__auto__ p2__7707__7723__auto__] (clojure.core/doseq [v__7724__auto__ vars__7717__auto__] (copy-meta__7711__auto__ v__7724__auto__ p1__7706__7722__auto__ p2__7707__7723__auto__)))] (copy__7721__auto__ :test :leiningen/skipped-test) (try (func__7710__auto__) (finally (copy__7721__auto__ :leiningen/skipped-test :test))))) selected-namespaces__7738__auto__ [[(constantly true) ()]] (fn* [] (clojure.core/apply clojure.test/run-tests selected-namespaces__7738__auto__))))] (clojure.core/spit ".lein-failures" (if true (clojure.core/pr-str (clojure.core/deref failures__7737__auto__)) "#<disabled :monkeypatch-clojure-test>")) (if true (java.lang.System/exit (clojure.core/+ (:error summary__7746__auto__) (:fail summary__7746__auto__))) (clojure.core/+ (:error summary__7746__auto__) (:fail summary__7746__auto__))))))