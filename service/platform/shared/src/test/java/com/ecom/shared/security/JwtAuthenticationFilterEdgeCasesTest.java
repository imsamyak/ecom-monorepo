package com.ecom.shared.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterEdgeCasesTest {

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void clear() {
        // Never leak an authentication into the next test
        SecurityContextHolder.clearContext();
    }

    // Sends a request with the given Authorization header and returns the resulting authentication
    private Authentication run(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Run the filter
        filter.doFilter(request, response, chain);

        // The chain must always continue, whether or not the token was accepted
        verify(chain).doFilter(request, response);
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void roleIsUpperCasedAndPrefixedWithRole() throws Exception {
        UUID id = UUID.randomUUID();

        // A lower-case role in the token
        Authentication auth = run("Bearer " + id + ":seller");

        // Principal carries the upper-cased role and the authority gets the ROLE_ prefix
        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
        assertEquals(id, principal.userId());
        assertEquals("SELLER", principal.role());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")));
    }

    @Test
    void credentialsAreNullAndAuthenticationIsMarkedAuthenticated() throws Exception {
        // A valid token
        Authentication auth = run("Bearer " + UUID.randomUUID() + ":seller");

        // No credentials are kept, and the request counts as authenticated
        assertNull(auth.getCredentials());
        assertTrue(auth.isAuthenticated());
    }

    @Test
    void nonBearerSchemeIsIgnored() throws Exception {
        // Only the Bearer scheme is understood
        assertNull(run("Basic " + UUID.randomUUID() + ":SELLER"));
    }

    @Test
    void lowerCaseBearerSchemeIsIgnored() throws Exception {
        // The scheme match is case sensitive
        assertNull(run("bearer " + UUID.randomUUID() + ":SELLER"));
    }

    @Test
    void emptyBearerTokenIsIgnored() throws Exception {
        // Nothing after "Bearer " means no user
        assertNull(run("Bearer "));
    }

    @Test
    void nonUuidUserIdIsIgnored() throws Exception {
        // The user part must be a UUID
        assertNull(run("Bearer not-a-uuid:SELLER"));
    }

    @Test
    void extraSegmentsAfterTheRoleAreIgnoredButTokenStillAccepted() throws Exception {
        // A token with more than two segments
        Authentication auth = run("Bearer " + UUID.randomUUID() + ":SELLER:extra");

        // Only the first two segments are used
        assertEquals("SELLER", ((JwtPrincipal) auth.getPrincipal()).role());
    }

    @Test
    void missingHeaderLeavesContextEmptyAndContinuesChain() throws Exception {
        // No Authorization header at all
        assertNull(run(null));
    }
}
