"use client";

import { useRef, useState } from "react";
import { useApi, useTenantId } from "@/lib/hooks";
import { documentApi } from "@/lib/api";
import { Badge } from "@/components/ui/Badge";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { formatDateTime, formatFileSize } from "@/lib/utils";
import { FileText, Upload, Download, Trash2, Eye } from "lucide-react";
import type { NiyamitraDocument } from "@/types";

export default function DocumentsPage() {
  const tenantId = useTenantId();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const { data: documents, loading, refetch } = useApi<NiyamitraDocument[]>(
    () => documentApi.list(tenantId) as Promise<NiyamitraDocument[]>,
    [tenantId]
  );

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await documentApi.upload(tenantId, file, "DASHBOARD");
      await refetch();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleDownload = async (id: string) => {
    try {
      const { url } = await documentApi.downloadUrl(id);
      window.open(url, "_blank");
    } catch (err) {
      alert(err instanceof Error ? err.message : "Download failed");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this document?")) return;
    try {
      await documentApi.delete(id);
      await refetch();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Delete failed");
    }
  };

  if (loading) return <LoadingSpinner />;

  const docs = documents || [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Document Vault</h2>
          <p className="text-sm text-gray-500">{docs.length} documents</p>
        </div>
        <div>
          <input
            ref={fileInputRef}
            type="file"
            className="hidden"
            onChange={handleUpload}
            accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="btn-primary"
          >
            <Upload className="mr-2 h-4 w-4" />
            {uploading ? "Uploading..." : "Upload Document"}
          </button>
        </div>
      </div>

      {docs.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="No documents yet"
          description="Upload your first compliance document."
          action={{ label: "Upload Document", onClick: () => fileInputRef.current?.click() }}
        />
      ) : (
        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">File Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">Size</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase text-gray-500">Uploaded</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {docs.map((doc) => (
                <tr key={doc.id} className="hover:bg-gray-50">
                  <td className="whitespace-nowrap px-6 py-4">
                    <div className="flex items-center gap-2">
                      <FileText className="h-5 w-5 text-gray-400" />
                      <span className="text-sm font-medium text-gray-900">{doc.fileName}</span>
                    </div>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{doc.fileType}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{formatFileSize(doc.fileSize)}</td>
                  <td className="whitespace-nowrap px-6 py-4">
                    <Badge status={doc.processingStatus} />
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{formatDateTime(doc.uploadedAt)}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      {doc.extractionResult && (
                        <button title="View extraction" className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
                          <Eye className="h-4 w-4" />
                        </button>
                      )}
                      <button onClick={() => handleDownload(doc.id)} title="Download" className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
                        <Download className="h-4 w-4" />
                      </button>
                      <button onClick={() => handleDelete(doc.id)} title="Delete" className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
