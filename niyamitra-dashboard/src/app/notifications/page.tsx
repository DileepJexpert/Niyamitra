"use client";

import { useApi, useTenantId } from "@/lib/hooks";
import { notificationApi } from "@/lib/api";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { formatDateTime } from "@/lib/utils";
import { Bell, MessageSquare, AlertTriangle, FileWarning, Newspaper } from "lucide-react";
import type { KavachNotification } from "@/types";

function getNotificationIcon(type: string) {
  switch (type) {
    case "WHATSAPP": return <MessageSquare className="h-5 w-5 text-green-500" />;
    case "ESCALATION": return <AlertTriangle className="h-5 w-5 text-red-500" />;
    case "EXPIRY": return <FileWarning className="h-5 w-5 text-orange-500" />;
    case "GAZETTE": return <Newspaper className="h-5 w-5 text-blue-500" />;
    default: return <Bell className="h-5 w-5 text-gray-400" />;
  }
}

export default function NotificationsPage() {
  const tenantId = useTenantId();

  const { data: notifications, loading } = useApi<KavachNotification[]>(
    () => notificationApi.list(tenantId) as Promise<KavachNotification[]>,
    [tenantId]
  );

  if (loading) return <LoadingSpinner />;

  const items = notifications || [];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Notifications</h2>
        <p className="text-sm text-gray-500">Kavach notification history</p>
      </div>

      {items.length === 0 ? (
        <EmptyState
          icon={Bell}
          title="No notifications"
          description="Notifications from Kavach AI will appear here."
        />
      ) : (
        <div className="space-y-2">
          {items.map((n) => (
            <div key={n.id} className="flex items-start gap-4 rounded-lg border border-gray-200 bg-white p-4">
              <div className="mt-0.5">{getNotificationIcon(n.messageType)}</div>
              <div className="flex-1">
                <p className="text-sm text-gray-900">{n.message}</p>
                <div className="mt-1 flex items-center gap-3 text-xs text-gray-500">
                  <span>{n.channel}</span>
                  <span>&middot;</span>
                  <span>{formatDateTime(n.sentAt)}</span>
                  <span>&middot;</span>
                  <span className={n.status === "SENT" ? "text-green-600" : "text-gray-500"}>{n.status}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
