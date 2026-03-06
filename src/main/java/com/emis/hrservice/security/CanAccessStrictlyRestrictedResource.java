package com.emis.hrservice.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
        "@schoolAuth.authorize(authentication, #p0," +
                " T(com.emis.hrservice.enums.ResourceAction).STRICTLY_RESTRICTED_RESOURCE)"
)
public @interface CanAccessStrictlyRestrictedResource {}