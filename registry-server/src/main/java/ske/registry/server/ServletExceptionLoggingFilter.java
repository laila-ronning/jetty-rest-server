package ske.registry.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletExceptionLoggingFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
