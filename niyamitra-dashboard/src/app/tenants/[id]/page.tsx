"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useApi } from "@/lib/hooks";
import { tenantApi } from "@/lib/api";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { Badge } from "@/components/ui/Badge";
import { ArrowLeft, User, Plus } from "lucide-react";
import type { Tenant, User as UserType } from "@/types";

export default function TenantDetailPage() {
  const params = useParams();
  const router = useRouter();
  const tenantId = params.id as string;
  const [showAddUser, setShowAddUser] = useState(false);
  const [userForm, setUserForm] = useState({ fullName: "", phone: "", email: "", role: "FLOOR_MANAGER" });
  const [submitting, setSubmitting] = useState(false);

  const { data: tenant, loading: tenantLoading } = useApi<Tenant>(
    () => tenantApi.get(tenantId) as Promise<Tenant>,
    [tenantId]
  );

  const { data: users, loading: usersLoading, refetch: refetchUsers } = useApi<UserType[]>(
    () => tenantApi.listUsers(tenantId) as Promise<UserType[]>,
    [tenantId]
  );

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await tenantApi.addUser(tenantId, userForm);
      setShowAddUser(false);
      setUserForm({ fullName: "", phone: "", email: "", role: "FLOOR_MANAGER" });
      await refetchUsers();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Failed to add user");
    } finally {
      setSubmitting(false);
    }
  };

  if (tenantLoading || usersLoading) return <LoadingSpinner />;
  if (!tenant) return <p className="text-center text-gray-500">Tenant not found</p>;

  const userList = users || [];

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <button onClick={() => router.back()} className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-700">
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      <div className="card">
        <h2 className="text-xl font-bold text-gray-900">{tenant.companyName}</h2>
        <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
          <div><span className="text-gray-500">GSTIN:</span> <span className="font-medium">{tenant.gstin}</span></div>
          <div><span className="text-gray-500">Industry:</span> <span className="font-medium">{tenant.industryCategory}</span></div>
          <div><span className="text-gray-500">State:</span> <span className="font-medium">{tenant.state}</span></div>
          <div><span className="text-gray-500">District:</span> <span className="font-medium">{tenant.district}</span></div>
          <div><span className="text-gray-500">Email:</span> <span className="font-medium">{tenant.contactEmail}</span></div>
          <div><span className="text-gray-500">Phone:</span> <span className="font-medium">{tenant.contactPhone}</span></div>
        </div>
      </div>

      <div className="card">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Users ({userList.length})</h3>
          <button onClick={() => setShowAddUser(true)} className="btn-primary text-xs">
            <Plus className="mr-1 h-3 w-3" /> Add User
          </button>
        </div>

        {showAddUser && (
          <form onSubmit={handleAddUser} className="mt-4 rounded-lg border border-gray-200 p-4 space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <input placeholder="Full Name" required value={userForm.fullName}
                onChange={(e) => setUserForm({ ...userForm, fullName: e.target.value })}
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm" />
              <input placeholder="Phone" required value={userForm.phone}
                onChange={(e) => setUserForm({ ...userForm, phone: e.target.value })}
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm" />
              <input placeholder="Email" type="email" value={userForm.email}
                onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm" />
              <select value={userForm.role}
                onChange={(e) => setUserForm({ ...userForm, role: e.target.value })}
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm">
                <option value="OWNER">Owner</option>
                <option value="FLOOR_MANAGER">Floor Manager</option>
                <option value="COMPLIANCE_OFFICER">Compliance Officer</option>
                <option value="ACCOUNTANT">Accountant</option>
              </select>
            </div>
            <div className="flex gap-2">
              <button type="submit" disabled={submitting} className="btn-primary text-xs">{submitting ? "Adding..." : "Add"}</button>
              <button type="button" onClick={() => setShowAddUser(false)} className="btn-secondary text-xs">Cancel</button>
            </div>
          </form>
        )}

        <div className="mt-4 space-y-2">
          {userList.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-4">No users yet</p>
          ) : (
            userList.map((u) => (
              <div key={u.id} className="flex items-center justify-between rounded-lg border border-gray-100 p-3">
                <div className="flex items-center gap-3">
                  <User className="h-5 w-5 text-gray-400" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">{u.fullName}</p>
                    <p className="text-xs text-gray-500">{u.phone} &middot; {u.email}</p>
                  </div>
                </div>
                <Badge status={u.role} />
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
