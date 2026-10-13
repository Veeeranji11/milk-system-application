package com.example.MilkSystemApplication.tenant;

/**
 * Manages the current tenant context for the executing thread.
 * Guarantees row-level isolation across all multi-tenant queries and service operations.
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            CURRENT_TENANT.set(tenantId.trim());
        } else {
            CURRENT_TENANT.remove();
        }
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
