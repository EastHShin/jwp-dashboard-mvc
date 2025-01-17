package nextstep.mvc.controller.tobe;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackages;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackages) {
        this.basePackages = basePackages;
        this.handlerExecutions = new HashMap<>();
    }

    @Override
    public void initialize() {
        log.info("Initialized AnnotationHandlerMapping!");
        final Map<Class<?>, Object> handlers = ControllerScanner.scan(basePackages);

        for (Class<?> handlerClass : handlers.keySet()) {
            initializeHandlerExecutions(handlerClass, handlers.get(handlerClass));
        }
    }


    private void initializeHandlerExecutions(Class<?> handlerClass, Object handler) {
        final List<Method> handlerMethods = extractValidHandler(handlerClass);

        for (Method handlerMethod : handlerMethods) {
            final HandlerExecution handlerExecution = new HandlerExecution(handler, handlerMethod);

            putToHandlerExecutions(handlerMethod, handlerExecution);
        }
    }

    private List<Method> extractValidHandler(Class<?> handlerClass) {
        final Method[] declaredMethods = handlerClass.getDeclaredMethods();
        return Arrays.stream(declaredMethods)
                .filter(this::isValidRequestMapping)
                .collect(Collectors.toList());
    }

    private boolean isValidRequestMapping(Method handlerMethod) {
        final RequestMapping requestMapping = handlerMethod.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            return false;
        }

        if (requestMapping.value().isEmpty()) {
            return false;
        }

        return requestMapping.method().length > 0;
    }

    private void putToHandlerExecutions(Method handlerMethod, HandlerExecution handlerExecution) {
        final RequestMapping requestMapping = handlerMethod.getAnnotation(RequestMapping.class);

        final String url = requestMapping.value();
        final RequestMethod[] requestMethods = requestMapping.method();

        for (RequestMethod requestMethod : requestMethods) {
            final HandlerKey handlerKey = new HandlerKey(url, requestMethod);
            handlerExecutions.put(handlerKey, handlerExecution);
            logPathAndHandler(handlerKey, handlerExecution);
        }
    }

    private void logPathAndHandler(HandlerKey handlerKey, HandlerExecution handlerExecution) {
        log.info("Path : " + handlerKey.getRequestMethod() + " " + handlerKey.getUrl() +
                ", Controller : " + handlerExecution.getController().getClass());
    }

    @Override
    public Object getHandler(final HttpServletRequest request) {
        return handlerExecutions.get(HandlerKey.from(request));
    }
}
