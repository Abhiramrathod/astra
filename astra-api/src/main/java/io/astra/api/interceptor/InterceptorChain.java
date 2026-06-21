package io.astra.api.interceptor;

import io.astra.api.*;
import java.util.*;

/**
 * Chain of {@link ActionInterceptor}s applied to each action.
 */
public interface InterceptorChain {
    void addInterceptor(ActionInterceptor interceptor);
    void removeInterceptor(ActionInterceptor interceptor);
    List<ActionInterceptor> getInterceptors();
}
