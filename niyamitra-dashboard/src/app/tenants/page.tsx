"use client";

import { useState } from "react";
import Link from "next/link";
import { tenantApi } from "@/lib/api";
import { setTenantId } from "@/lib/hooks";
import { formatDate } from "@/lib/utils";
import { Building2, Plus, ChevronRight, Check } from "lucide-react";
import type { Tenant, IndustryCategory } from "@/types";

export default function TenantsPage() {
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [showOnboard, setShowOnboard] = useState(false);
  const [formData, setFormData] = useState({
    companyName: "",
    gstin: "",
    industryCategory: "CHEMICAL" as IndustryCategory,
    state: "",
    district: "",
    contactEmail: "",
    contactPhone: "",
    ownerName: "",
    ownerPhone: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const handleOnboard = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const result = await tenantApi.onboard(formData) as unknown as Tenant;
      setTenants((prev) => [...prev, result]);
      setShowOnboard(false);
      setFormData({
        companyName: "", gstin: "", industryCategory: "CHEMICAL", state: "", district: "",
        contactEmail: "", contactPhone: "", ownerName: "", ownerPhone: "",
      });
    } catch (err) {
      alert(err instanceof Error ? err.message : "Onboarding failed");
    } finally {
      setSubmitting(false);
    }
  };

  const handleSelect = (id: string) => {
    setTenantId(id);
    alert("Tenant selected. Dashboard will now show data for this tenant.");
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Tenants</h2>
          <p className="text-sm text-gray-500">Manage organizations</p>
        </div>
        <button onClick={() => setShowOnboard(true)} className="btn-primary">
          <Plus className="mr-2 h-4 w-4" /> Onboard Tenant
        </button>
      </div>

      {showOnboard && (
        <form onSubmit={handleOnboard} className="card space-y-4">
          <h3 className="text-lg font-semibold">Onboard New Tenant</h3>
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: "Company Name", key: "companyName", type: "text" },
              { label: "GSTIN", key: "gstin", type: "text" },
              { label: "State", key: "state", type: "text" },
              { label: "District", key: "district", type: "text" },
              { label: "Contact Email", key: "contactEmail", type: "email" },
              { label: "Contact Phone", key: "contactPhone", type: "tel" },
              { label: "Owner Name", key: "ownerName", type: "text" },
              { label: "Owner Phone", key: "ownerPhone", type: "tel" },
            ].map(({ label, key, type }) => (
              <div key={key}>
                <label className="block text-sm font-medium text-gray-700">{label}</label>
                <input
                  type={type}
                  required
                  value={formData[key as keyof typeof formData]}
                  onChange={(e) => setFormData({ ...formData, [key]: e.target.value })}
                  className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-niyamitra-500 focus:outline-none focus:ring-1 focus:ring-niyamitra-500"
                />
              </div>
            ))}
            <div>
              <label className="block text-sm font-medium text-gray-700">Industry</label>
              <select
                value={formData.industryCategory}
                onChange={(e) => setFormData({ ...formData, industryCategory: e.target.value as IndustryCategory })}
                className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-niyamitra-500 focus:outline-none focus:ring-1 focus:ring-niyamitra-500"
              >
                {["CHEMICAL", "PHARMACEUTICAL", "TEXTILE", "FOOD_PROCESSING", "AUTOMOBILE", "ELECTRONICS", "METAL", "PLASTIC", "PAPER", "OTHER"].map((v) => (
                  <option key={v} value={v}>{v.replace("_", " ")}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex gap-2">
            <button type="submit" disabled={submitting} className="btn-primary">
              {submitting ? "Onboarding..." : "Onboard"}
            </button>
            <button type="button" onClick={() => setShowOnboard(false)} className="btn-secondary">Cancel</button>
          </div>
        </form>
      )}

      {tenants.length > 0 && (
        <div className="space-y-2">
          {tenants.map((t) => (
            <div key={t.id} className="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4">
              <div className="flex items-center gap-3">
                <Building2 className="h-8 w-8 text-niyamitra-500" />
                <div>
                  <h3 className="text-sm font-semibold text-gray-900">{t.companyName}</h3>
                  <p className="text-xs text-gray-500">{t.gstin} &middot; {t.industryCategory} &middot; {t.state}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => handleSelect(t.id)} className="btn-secondary text-xs">
                  <Check className="mr-1 h-3 w-3" /> Select
                </button>
                <Link href={`/tenants/${t.id}`} className="rounded p-2 text-gray-400 hover:bg-gray-100">
                  <ChevronRight className="h-5 w-5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {tenants.length === 0 && !showOnboard && (
        <div className="card text-center py-12">
          <Building2 className="mx-auto h-12 w-12 text-gray-300" />
          <h3 className="mt-4 text-lg font-medium text-gray-900">No tenants onboarded</h3>
          <p className="mt-1 text-sm text-gray-500">Click &quot;Onboard Tenant&quot; to add your first organization.</p>
        </div>
      )}
    </div>
  );
}
