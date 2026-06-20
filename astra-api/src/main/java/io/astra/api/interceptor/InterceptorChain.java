package io.astra.api.interceptor;

import io.astra.api.*;
import java.util.*;

public interface InterceptorChain {
    void addInterceptor(ActionInterceptor interceptor);
    void removeInterceptor(ActionInterceptor interceptor);
    List<ActionInterceptor> getInterceptors();
}
