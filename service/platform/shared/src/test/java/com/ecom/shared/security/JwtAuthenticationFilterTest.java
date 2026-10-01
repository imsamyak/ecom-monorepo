package com.ecom.shared.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalWithValidTokenSetsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        UUID testUserId = UUID.randomUUID();
        // Simulate our "UUID:ROLE" token setup
        request.addHeader("Authorization", "Bearer " + testUserId.toString() + ":SELLER");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set in context");
        assertTrue(auth.isAuthenticated(), "User should be authenticated");
        
        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
        assertEquals(testUserId, principal.userId());
        assertEquals("SELLER", principal.role());
        
        // Check authorities mapped properly
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")));
    }

    @Test
    void doFilterInternalWithValidTokenNoRoleDefaultsToUserRole() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        UUID testUserId = UUID.randomUUID();
        request.addHeader("Authorization", "Bearer " + testUserId.toString());

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void doFilterInternalWithMissingHeaderDoesNothing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No Authorization header

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Context should remain empty");
    }

    @Test
    void doFilterInternalWithInvalidTokenFormatDoesNothingAndDoesNotThrow() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer NOT_A_UUID");

        // Should not throw exception, just catch and leave context empty
        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Context should remain empty for bad token");
    }
}
