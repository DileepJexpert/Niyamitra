"use client";

import { useState } from "react";
import Link from "next/link";
import { useApi, useTenantId } from "@/lib/hooks";
import { taskApi } from "@/lib/api";
import { Badge } from "@/components/ui/Badge";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { formatDate, daysUntil, cn } from "@/lib/utils";
import { ClipboardList, Filter, ChevronRight } from "lucide-react";
import type { ComplianceTask, TaskStatus } from "@/types";

const STATUS_FILTERS: { label: string; value: TaskStatus | "ALL" }[] = [
  { label: "All", value: "ALL" },
  { label: "Pending", value: "PENDING" },
  { label: "In Progress", value: "IN_PROGRESS" },
  { label: "Overdue", value: "OVERDUE" },
  { label: "Escalated", value: "ESCALATED" },
  { label: "Completed", value: "COMPLETED" },
];

export default function TasksPage() {
  const tenantId = useTenantId();
  const [statusFilter, setStatusFilter] = useState<TaskStatus | "ALL">("ALL");

  const { data: tasks, loading } = useApi<ComplianceTask[]>(
    () => taskApi.list(tenantId) as Promise<ComplianceTask[]>,
    [tenantId]
  );

  if (loading) return <LoadingSpinner />;

  const taskList = tasks || [];
  const filtered = statusFilter === "ALL" ? taskList : taskList.filter((t) => t.status === statusFilter);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Compliance Tasks</h2>
          <p className="text-sm text-gray-500">{taskList.length} total tasks</p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <Filter className="h-4 w-4 text-gray-400" />
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setStatusFilter(f.value)}
            className={cn(
              "rounded-full px-3 py-1 text-xs font-medium transition-colors",
              statusFilter === f.value
                ? "bg-niyamitra-600 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            )}
          >
            {f.label}
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          icon={ClipboardList}
          title="No tasks found"
          description="No compliance tasks match the current filter."
        />
      ) : (
        <div className="space-y-2">
          {filtered.map((task) => {
            const days = daysUntil(task.dueDate);
            return (
              <Link
                key={task.id}
                href={`/tasks/${task.id}`}
                className="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4 transition-colors hover:border-niyamitra-300 hover:bg-niyamitra-50/30"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-3">
                    <h3 className="text-sm font-semibold text-gray-900">{task.ruleName}</h3>
                    <Badge status={task.status} />
                    {task.escalationLevel > 0 && (
                      <span className="badge bg-purple-100 text-purple-800">
                        L{task.escalationLevel} Escalation
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-xs text-gray-500">
                    {task.complianceCategory} &middot; Assigned to {task.assignedUserName || "Unassigned"}
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <p className={cn("text-sm font-medium", days <= 0 ? "text-red-600" : days <= 5 ? "text-orange-600" : "text-gray-700")}>
                      {task.status === "COMPLETED" ? "Done" : days <= 0 ? "Overdue" : `${days}d left`}
                    </p>
                    <p className="text-xs text-gray-500">{formatDate(task.dueDate)}</p>
                  </div>
                  <ChevronRight className="h-5 w-5 text-gray-300" />
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
