package io.astra.api;

/** Thread-local holder for a query string, used during execution. */
public final class QueryContext {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private QueryContext() {}

    public static void set(String query) {
        HOLDER.set(query);
    }

    public static String get() {
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
