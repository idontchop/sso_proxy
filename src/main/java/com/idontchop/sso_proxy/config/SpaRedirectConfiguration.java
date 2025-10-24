package com.idontchop.sso_proxy.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class SpaRedirectConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // If the resource exists, return it
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // For Angular routing - return index.html for non-API routes
                        if (!resourcePath.startsWith("api/") && !resourcePath.contains(".")) {
                            return new ClassPathResource("static/index.html");
                        }
                        
                        return null;
                    }
                });
    }

    @Bean
    public FilterRegistrationBean<SpaForwardFilter> spaForwardFilter() {
        FilterRegistrationBean<SpaForwardFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SpaForwardFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }

    private static class SpaForwardFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String path = httpRequest.getRequestURI();

            // Don't forward API calls, static resources, or specific endpoints
            if (path.startsWith("/api/") || 
                path.startsWith("/h2-console") ||
                path.contains(".") ||
                path.equals("/")) {
                chain.doFilter(request, response);
                return;
            }

            // Forward all other routes to the Angular app
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }
}
