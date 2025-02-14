package com.domnerka.util;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for accessing security context information such as username,
 * user ID, groups, and roles.
 */
@UtilityClass
public class SecurityContextUtility{

    /**
     * Retrieves the username from the JWT authentication token.
     *
     * @return the username as a {@link String}
     */
    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        return (String) token.getTokenAttributes().get("preferred_username");
    }

    /**
     * Retrieves the user ID from the security context.
     *
     * @return the user ID as a {@link String}
     */
    public static String getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // getName returns the userID
    }

    /**
     * Retrieves the list of groups the user belongs to from the JWT authentication
     * token.
     *
     * @return a list of group names as {@link List <String>}
     */
    public static List<String> getGroups() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        Object userGroups = token.getTokenAttributes().get("usergroups");
        if (userGroups instanceof List<?>) {
            return ((List<?>) userGroups).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves the list of realm roles assigned to the user from the JWT
     * authentication token.
     *
     * @return a list of realm roles as {@link List<String>}
     */
    public static List<String> getRealmRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        Object rolesObject = ((LinkedTreeMap<?, ?>) token.getTokenAttributes().get("realm_access")).get("roles");
        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves the full name of the user from the JWT authentication token.
     *
     * @return the user's full name as a {@link String}, combining family name and
     *         given name
     */
    public static String getUserFullName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        return token.getTokenAttributes().get("family_name") + " " + token.getTokenAttributes().get("given_name");
    }
}
