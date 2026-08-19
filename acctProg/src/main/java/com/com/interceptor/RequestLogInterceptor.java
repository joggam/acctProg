package com.com.interceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER =
            LogManager.getLogger(RequestLogInterceptor.class);

    private static final String START_TIME = "REQUEST_START_TIME";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        // ContextPath 제거
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()
                && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        if (handler instanceof HandlerMethod) {

            HandlerMethod handlerMethod = (HandlerMethod) handler;

            String controllerName =
                    handlerMethod.getBeanType().getSimpleName();

            String methodName =
                    handlerMethod.getMethod().getName();

            LOGGER.info("[REQUEST] {} {} -> {}.{}",
                    httpMethod,
                    uri,
                    controllerName,
                    methodName);

        } else {

            LOGGER.info("[REQUEST] {} {}",
                    httpMethod,
                    uri);
        }

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) throws Exception {

        Object startObj = request.getAttribute(START_TIME);

        long elapsedTime = 0;

        if (startObj instanceof Long) {
            elapsedTime =
                    System.currentTimeMillis() - (Long) startObj;
        }

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()
                && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        if (ex != null) {

            LOGGER.error(
                    "[RESPONSE] {} {} -> {} ({}ms) ERROR : {}",
                    httpMethod,
                    uri,
                    response.getStatus(),
                    elapsedTime,
                    ex.getMessage(),
                    ex);

        } else {

            LOGGER.info(
                    "[RESPONSE] {} {} -> {} ({}ms)",
                    httpMethod,
                    uri,
                    response.getStatus(),
                    elapsedTime);
        }
    }
}