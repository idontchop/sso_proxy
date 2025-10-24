# Gateway Routes - Quick Reference

## Configuration Files

Your gateway routes are now configured in `application.properties`. 

**Current Routes (edit in `src/main/resources/application.properties`):**

```properties
# User Service - http://localhost:8082
spring.cloud.gateway.mvc.routes[0].id=user-service
spring.cloud.gateway.mvc.routes[0].uri=http://localhost:8082
spring.cloud.gateway.mvc.routes[0].predicates[0]=Path=/api/users/**
spring.cloud.gateway.mvc.routes[0].filters[0]=AddRequestHeader=X-Gateway, SSO-Proxy

# Product Service - http://localhost:8083
spring.cloud.gateway.mvc.routes[1].id=product-service
spring.cloud.gateway.mvc.routes[1].uri=http://localhost:8083
spring.cloud.gateway.mvc.routes[1].predicates[0]=Path=/api/products/**
spring.cloud.gateway.mvc.routes[1].filters[0]=AddRequestHeader=X-Gateway, SSO-Proxy
```

## Quick Start: Adding a New Route

### Step 1: Add Route Configuration

Add to `application.properties`:

```properties
# Next available index is [2]
spring.cloud.gateway.mvc.routes[2].id=order-service
spring.cloud.gateway.mvc.routes[2].uri=http://localhost:8084
spring.cloud.gateway.mvc.routes[2].predicates[0]=Path=/api/orders/**
spring.cloud.gateway.mvc.routes[2].filters[0]=AddRequestHeader=X-Gateway, SSO-Proxy
```

### Step 2: Restart the Application

```bash
./mvnw spring-boot:run
```

### Step 3: Test the Route

```bash
# Login first
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' \
  -c cookies.txt

# Test the route
curl http://localhost:8080/api/orders/123 \
  -b cookies.txt
```

## Common Route Patterns

### 1. Simple Route
```properties
spring.cloud.gateway.mvc.routes[X].id=my-service
spring.cloud.gateway.mvc.routes[X].uri=http://localhost:8080
spring.cloud.gateway.mvc.routes[X].predicates[0]=Path=/api/myservice/**
```

### 2. Route with Multiple Filters
```properties
spring.cloud.gateway.mvc.routes[X].id=my-service
spring.cloud.gateway.mvc.routes[X].uri=http://localhost:8080
spring.cloud.gateway.mvc.routes[X].predicates[0]=Path=/api/myservice/**
spring.cloud.gateway.mvc.routes[X].filters[0]=AddRequestHeader=X-Gateway, SSO-Proxy
spring.cloud.gateway.mvc.routes[X].filters[1]=AddRequestHeader=X-Custom, Value
```

### 3. Route with Path Rewriting
```properties
spring.cloud.gateway.mvc.routes[X].id=legacy-service
spring.cloud.gateway.mvc.routes[X].uri=http://localhost:8080
spring.cloud.gateway.mvc.routes[X].predicates[0]=Path=/api/v2/**
spring.cloud.gateway.mvc.routes[X].filters[0]=RewritePath=/api/v2/(?<segment>.*), /api/v1/$\{segment}
```

### 4. Route with Method Matching
```properties
spring.cloud.gateway.mvc.routes[X].id=read-only-service
spring.cloud.gateway.mvc.routes[X].uri=http://localhost:8080
spring.cloud.gateway.mvc.routes[X].predicates[0]=Path=/api/data/**
spring.cloud.gateway.mvc.routes[X].predicates[1]=Method=GET
```

## Switch to YAML (Optional)

If you prefer YAML format:

1. Rename `application.properties` to `application.properties.bak`
2. Rename `application.yml.example` to `application.yml`
3. Edit routes in the cleaner YAML format

## Environment-Specific Routes

### Development (`application-dev.properties`)
```properties
spring.cloud.gateway.mvc.routes[0].uri=http://localhost:8082
```

### Production (`application-prod.properties`)
```properties
spring.cloud.gateway.mvc.routes[0].uri=http://user-service:8080
# or
spring.cloud.gateway.mvc.routes[0].uri=https://user-service.production.com
```

Run with: `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`

## Troubleshooting

### Route Not Working?
1. Check the route ID is unique
2. Verify the backend service is running
3. Check path pattern matches your request
4. Enable debug logging:
   ```properties
   logging.level.org.springframework.cloud.gateway=DEBUG
   ```

### 404 Not Found?
- Verify path pattern: `/api/users/**` matches `/api/users/123` but not `/users/123`
- Check SecurityConfig allows the route

### 502 Bad Gateway?
- Backend service is down or unreachable
- Check URI is correct
- Verify network connectivity

## Next Steps

1. ✅ Routes are configured in `application.properties`
2. 📝 Update route URIs to match your backend services
3. 🔧 Start your backend services
4. 🧪 Test with the login flow
5. 📚 Read `GATEWAY_CONFIGURATION.md` for advanced configuration

## Useful Commands

```bash
# Run the gateway
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Check active routes (enable actuator endpoints)
curl http://localhost:8080/actuator/gateway/routes

# View application properties
./mvnw spring-boot:run -Dspring-boot.run.arguments=--debug
```
