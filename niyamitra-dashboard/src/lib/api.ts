const API_BASE = "/api/v1";

async function fetchApi<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(error.message || `API error: ${res.status}`);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

// Tenant APIs
export const tenantApi = {
  onboard: (data: Record<string, unknown>) =>
    fetchApi<Record<string, unknown>>("/tenants/onboard", { method: "POST", body: JSON.stringify(data) }),
  get: (id: string) => fetchApi<Record<string, unknown>>(`/tenants/${id}`),
  update: (id: string, data: Record<string, unknown>) =>
    fetchApi<Record<string, unknown>>(`/tenants/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  listUsers: (tenantId: string) => fetchApi<Record<string, unknown>[]>(`/tenants/${tenantId}/users`),
  addUser: (tenantId: string, data: Record<string, unknown>) =>
    fetchApi<Record<string, unknown>>(`/tenants/${tenantId}/users`, { method: "POST", body: JSON.stringify(data) }),
};

// Task APIs
export const taskApi = {
  list: (tenantId: string) => fetchApi<Record<string, unknown>[]>(`/tasks?tenantId=${tenantId}`),
  get: (id: string) => fetchApi<Record<string, unknown>>(`/tasks/${id}`),
  create: (data: Record<string, unknown>) =>
    fetchApi<Record<string, unknown>>("/tasks", { method: "POST", body: JSON.stringify(data) }),
  updateStatus: (id: string, status: string) =>
    fetchApi<Record<string, unknown>>(`/tasks/${id}/status?status=${status}`, { method: "PUT" }),
  acknowledge: (id: string) =>
    fetchApi<Record<string, unknown>>(`/tasks/${id}/acknowledge`, { method: "PUT" }),
  reschedule: (id: string, newDate: string, reason: string) =>
    fetchApi<Record<string, unknown>>(`/tasks/${id}/reschedule?newDueDate=${newDate}&reason=${reason}`, { method: "PUT" }),
  dashboard: (tenantId: string) => fetchApi<Record<string, unknown>>(`/tasks/dashboard?tenantId=${tenantId}`),
  score: (tenantId: string) => fetchApi<Record<string, unknown>>(`/tasks/anupalan-score?tenantId=${tenantId}`),
};

// Document APIs
export const documentApi = {
  list: (tenantId: string) => fetchApi<Record<string, unknown>[]>(`/documents?tenantId=${tenantId}`),
  get: (id: string) => fetchApi<Record<string, unknown>>(`/documents/${id}`),
  downloadUrl: (id: string) => fetchApi<{ url: string }>(`/documents/${id}/download-url`),
  delete: (id: string) => fetchApi<void>(`/documents/${id}`, { method: "DELETE" }),
  upload: async (tenantId: string, file: File, uploadSource: string) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("tenantId", tenantId);
    formData.append("uploadSource", uploadSource);
    const res = await fetch(`${API_BASE}/documents/upload`, { method: "POST", body: formData });
    if (!res.ok) throw new Error(`Upload failed: ${res.status}`);
    return res.json();
  },
};

// Rule APIs
export const ruleApi = {
  list: (industry: string, state: string) =>
    fetchApi<Record<string, unknown>[]>(`/anupalan/rules/applicable?industry=${industry}&state=${state}`),
  get: (id: string) => fetchApi<Record<string, unknown>>(`/anupalan/rules/${id}`),
  categories: () => fetchApi<string[]>("/anupalan/rules/categories"),
};

// Notification APIs (read from notification service via gateway)
export const notificationApi = {
  list: (tenantId: string) => fetchApi<Record<string, unknown>[]>(`/kavach/notifications?tenantId=${tenantId}`),
};
