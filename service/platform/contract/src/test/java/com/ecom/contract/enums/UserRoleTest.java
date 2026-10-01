package com.ecom.contract.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    void mapsKnownValuesCaseInsensitively() {
        // Verify that exact and case-insensitive string values map to the correct SELLER enum
        assertThat(UserRole.fromClaim("SELLER")).isEqualTo(UserRole.SELLER);
        assertThat(UserRole.fromClaim("seller")).isEqualTo(UserRole.SELLER);
        assertThat(UserRole.fromClaim("SeLlEr")).isEqualTo(UserRole.SELLER);
        
        // Verify that exact and case-insensitive string values map to the correct BUYER enum
        assertThat(UserRole.fromClaim("BUYER")).isEqualTo(UserRole.BUYER);
        assertThat(UserRole.fromClaim("buyer")).isEqualTo(UserRole.BUYER);
        
        // Verify that exact and case-insensitive string values map to the correct ADMIN enum
        assertThat(UserRole.fromClaim("ADMIN")).isEqualTo(UserRole.ADMIN);
        assertThat(UserRole.fromClaim("admin")).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void mapsNullBlankAndUnknownToUnknown() {
        // Verify that the UNKNOWN string correctly maps to the UNKNOWN enum
        assertThat(UserRole.fromClaim("UNKNOWN")).isEqualTo(UserRole.UNKNOWN);
        assertThat(UserRole.fromClaim("unknown")).isEqualTo(UserRole.UNKNOWN);
        
        // Verify that random strings, blank, or null fallback to the UNKNOWN enum
        assertThat(UserRole.fromClaim("GUEST")).isEqualTo(UserRole.UNKNOWN);
        assertThat(UserRole.fromClaim("")).isEqualTo(UserRole.UNKNOWN);
        assertThat(UserRole.fromClaim("  ")).isEqualTo(UserRole.UNKNOWN);
        assertThat(UserRole.fromClaim(null)).isEqualTo(UserRole.UNKNOWN);
    }
}
