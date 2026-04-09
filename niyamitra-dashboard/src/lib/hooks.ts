"use client";

import { useState, useEffect, useCallback } from "react";

export function useApi<T>(fetcher: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetcher();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
    } finally {
      setLoading(false);
    }
  }, deps);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { data, loading, error, refetch };
}

// Simple tenant context — in production, use Keycloak JWT claims
const DEMO_TENANT_ID = "demo-tenant-id";

export function useTenantId(): string {
  if (typeof window !== "undefined") {
    return localStorage.getItem("niyamitra_tenant_id") || DEMO_TENANT_ID;
  }
  return DEMO_TENANT_ID;
}

export function setTenantId(id: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem("niyamitra_tenant_id", id);
  }
}
