package com.qorvia.eventmanagementservice.security;

import com.qorvia.eventmanagementservice.service.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequireRolesAspect {

    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Around("@annotation(com.qorvia.eventmanagementservice.security.RequireRole) || @annotation(com.qorvia.eventmanagementservice.security.RequireRoles)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("calling the check role method for validating the role");
        String token = jwtService.getJWTFromRequest(request);
        if (token == null || !StringUtils.hasText(token)) {
            throw new SecurityException("Authorization token is missing or invalid");
        }

        Claims claims = jwtService.getClaimsFromToken(token);
        String roles = getRolesFromClaims(claims);

        log.info("jwt token is : {} , with roles : {}", token, roles);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Check if the method has the RequireRole annotation
        if (method.isAnnotationPresent(RequireRole.class)) {
            RequireRole requireRole = method.getAnnotation(RequireRole.class);
            Roles requiredRole = requireRole.role();
            boolean authorized = roles != null && roles.contains("ROLE_" + requiredRole.name());
            if (!authorized) {
                throw new SecurityException("User not authorized");
            }
        }

        if (method.isAnnotationPresent(RequireRoles.class)) {
            RequireRoles requireRoles = method.getAnnotation(RequireRoles.class);
            Roles[] requiredRoles = requireRoles.roles();
            boolean authorized = roles != null && isRolePresent(roles, requiredRoles);
            if (!authorized) {
                throw new SecurityException("User not authorized");
            }
        }

        return joinPoint.proceed();
    }

    private boolean isRolePresent(String roles, Roles[] requiredRoles) {
        for (Roles role : requiredRoles) {
            if (roles.contains("ROLE_" + role.name())) {
                return true;
            }
        }
        return false;
    }

    private String getRolesFromClaims(Claims claims) {
        Object rolesClaim = claims.get("role");
        if (rolesClaim instanceof String) {
            return (String) rolesClaim;
        } else if (rolesClaim instanceof List) {
            List<?> rolesList = (List<?>) rolesClaim;
            return rolesList.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else {
            throw new IllegalArgumentException("Invalid role claim type");
        }
    }
}
