package com.idontchop.sso_proxy.config;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Gateway configuration for routing authenticated requests to backend services.
 * 
 * This configuration allows the SSO proxy to:
 * 1. Authenticate users via /api/auth/* endpoints
 * 2. Route authenticated requests to backend microservices
 * 3. Add authentication headers to proxied requests
 * 
 * To add routes, uncomment the examples in gatewayRoutes() method and modify
 * the paths and backend URLs according to your microservices architecture.
 */
@Configuration
public class GatewayConfig {

    /**
     * Configure gateway routes to backend services.
     * Add your microservice routes here.
     * 
     * Example route configuration:
     * This example routes /api/example/** to http://localhost:8081
    /**
     * Configure gateway routes to backend services.
     * Add your microservice routes here.
     * 
     * Example route configuration:
     * This example routes /api/example/** to http://localhost:8081
     * 
     * To add more routes, follow the same pattern.
     */
    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        // Example route - uncomment and modify as needed
        /*
        return GatewayRouterFunctions.route("example_service")
                .GET("/api/example/**", HandlerFunctions.http("http://localhost:8081"))
                .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
                .filter(addRequestHeader("X-Forwarded-By", "SSO-Proxy"))
                .build();
        */
        
        // Return empty route for now - add your actual routes above
        return GatewayRouterFunctions.route("placeholder")
                .GET("/api/gateway-placeholder/**", request -> 
                    ServerResponse.ok().body("Gateway is configured but no routes are active yet"))
                .build();
                
        /* Additional route examples:
        
        // Route to user service (all HTTP methods)
        return GatewayRouterFunctions.route("user_service")
                .route(RequestPredicates.path("/api/users/**"), 
                       HandlerFunctions.http("http://localhost:8082"))
                .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
                .build();
            
        // Route to product service (GET only)
        return GatewayRouterFunctions.route("product_service")
                .GET("/api/products/**", HandlerFunctions.http("http://localhost:8083"))
                .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
                .build();
            
        // Multiple routes in one bean
        return GatewayRouterFunctions.route("user_service")
                .route(RequestPredicates.path("/api/users/**"), 
                       HandlerFunctions.http("http://localhost:8082"))
                .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
                .build()
                .and(GatewayRouterFunctions.route("product_service")
                    .route(RequestPredicates.path("/api/products/**"), 
                           HandlerFunctions.http("http://localhost:8083"))
                    .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
                    .build());
        */
    }
    
    /**
     * Example: Configure specific routes for different environments
     * Uncomment and customize based on your needs
     */
    /*
    @Bean
    @Profile("dev")
    public RouterFunction<ServerResponse> devRoutes() {
        return GatewayRouterFunctions.route("dev_service")
                .route(RequestPredicates.path("/api/dev/**"), 
                       HandlerFunctions.http("http://localhost:9000"))
                .build();
    }
    */
}
