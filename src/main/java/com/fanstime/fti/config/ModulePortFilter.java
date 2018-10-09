package com.fanstime.fti.config;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.fanstime.fti.util.AppConst.JSON_RPC_ALIAS_PATH;
import static com.fanstime.fti.util.AppConst.JSON_RPC_PATH;

/**
 * Filters web and rpc requests to ensure that
 * they are performed to the right port
 */
@Slf4j
@WebFilter()
public class ModulePortFilter implements Filter {
    private Integer rpcPort;
    private Integer webPort;

    public ModulePortFilter(Integer rpcPort, Integer webPort) {
        this.rpcPort = rpcPort;
        this.webPort = webPort;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(request instanceof HttpServletRequest) {
            if (isRpcRequest((HttpServletRequest) request)) { // RPC request
                if (isRequestToWebPort(request)) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else { // Not RPC request
                if (isRequestToRpcPort(request)) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isRpcRequest(HttpServletRequest request) {
        return request.getRequestURI().equals(JSON_RPC_PATH) ||
                ("POST".equals(request.getMethod()) && request.getRequestURI().equals(JSON_RPC_ALIAS_PATH));
    }

    private boolean isRequestToRpcPort(ServletRequest request) {
        return rpcPort != null && request.getLocalPort() == rpcPort;
    }

    private boolean isRequestToWebPort(ServletRequest request) {
        return webPort != null && request.getLocalPort() == webPort;
    }

    @Override
    public void destroy() {

    }

    public static final Filter DUMMY = new Filter() {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            chain.doFilter(request, response);
        }
        @Override
        public void destroy() {
        }
    };
}
