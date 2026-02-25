package com.epages.restdocs.apispec;

import org.springframework.restdocs.operation.Operation;

/**
 * Shadow class that replaces com.epages.restdocs.apispec.SecurityRequirementsHandler
 * from restdocs-api-spec-mockmvc library.
 *
 * <p>The original handler casts HttpHeaders to Map (or uses filterKeys which requires Map),
 * but in Spring Framework 7, HttpHeaders no longer implements Map.
 * This replacement simply returns null (no security requirements) to avoid the incompatibility.
 *
 * <p>Test-classpath classes are loaded before library JARs, so the JVM picks up this
 * implementation instead of the library's broken one.
 */
public class SecurityRequirementsHandler {

    public SecurityRequirements extractSecurityRequirements(Operation operation) {
        return null;
    }
}
