package com.ecom.contract.enums;

public enum UserRole {
    SELLER, BUYER, ADMIN, UNKNOWN;

    public static UserRole fromClaim(String claim) {
        // Return UNKNOWN for null or blank claim strings
        if (claim == null || claim.isBlank()) {
            return UNKNOWN;
        }
        // Try mapping the upper-cased claim to a known role
        try {
            // Use Locale.ROOT because the default locale can change letters 
            // (e.g., in Turkish, admin becomes ADMIN with a dotted capital I and would map to UNKNOWN).
            return UserRole.valueOf(claim.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Return UNKNOWN for anything else that does not match
            return UNKNOWN;
        }
    }
}
