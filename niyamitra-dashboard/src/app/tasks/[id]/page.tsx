"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useApi } from "@/lib/hooks";
import { taskApi } from "@/lib/api";
import { Badge } from "@/components/ui/Badge";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { formatDate, formatDateTime } from "@/lib/utils";
import { ArrowLeft, Calendar, User, CheckCircle, Clock, AlertTriangle } from "lucide-react";
import type { ComplianceTask } from "@/types";

export default function TaskDetailPage() {
  const params = useParams();
  const router = useRouter();
  const taskId = params.id as string;
  const [actionLoading, setActionLoading] = useState(false);

  const { data: task, loading, refetch } = useApi<ComplianceTask>(
    () => taskApi.get(taskId) as Promise<ComplianceTask>,
    [taskId]
  );

  const handleStatusUpdate = async (status: string) => {
    setActionLoading(true);
    try {
      await taskApi.updateStatus(taskId, status);
      await refetch();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Failed to update status");
    } finally {
      setActionLoading(false);
    }
  };

  const handleAcknowledge = async () => {
    setActionLoading(true);
    try {
      await taskApi.acknowledge(taskId);
      await refetch();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Failed to acknowledge");
    } finally {
      setActionLoading(false);
    }
  };

  const handleReschedule = async () => {
    const newDate = prompt("Enter new due date (YYYY-MM-DD):");
    if (!newDate) return;
    const reason = prompt("Reason for rescheduling:") || "Dashboard reschedule";
    setActionLoading(true);
    try {
      await taskApi.reschedule(taskId, newDate, reason);
      await refetch();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Failed to reschedule");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <LoadingSpinner />;
  if (!task) return <p className="text-center text-gray-500">Task not found</p>;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <button onClick={() => router.back()} className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-700">
        <ArrowLeft className="h-4 w-4" /> Back to Tasks
      </button>

      <div className="card">
        <div className="flex items-start justify-between">
          <div>
            <h2 className="text-xl font-bold text-gray-900">{task.ruleName}</h2>
            <p className="mt-1 text-sm text-gray-500">{task.complianceCategory}</p>
          </div>
          <Badge status={task.status} />
        </div>

        <div className="mt-6 grid grid-cols-2 gap-4">
          <div className="flex items-center gap-2 text-sm">
            <Calendar className="h-4 w-4 text-gray-400" />
            <span className="text-gray-500">Due Date:</span>
            <span className="font-medium">{formatDate(task.dueDate)}</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <User className="h-4 w-4 text-gray-400" />
            <span className="text-gray-500">Assigned:</span>
            <span className="font-medium">{task.assignedUserName || "Unassigned"}</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Clock className="h-4 w-4 text-gray-400" />
            <span className="text-gray-500">Created:</span>
            <span className="font-medium">{formatDateTime(task.createdAt)}</span>
          </div>
          {task.escalationLevel > 0 && (
            <div className="flex items-center gap-2 text-sm">
              <AlertTriangle className="h-4 w-4 text-red-400" />
              <span className="text-gray-500">Escalation:</span>
              <span className="font-medium text-red-600">Level {task.escalationLevel}</span>
            </div>
          )}
        </div>

        {task.notes && (
          <div className="mt-4 rounded-lg bg-gray-50 p-3">
            <p className="text-xs font-medium text-gray-500">Notes</p>
            <p className="mt-1 text-sm text-gray-700">{task.notes}</p>
          </div>
        )}

        {task.completedDate && (
          <div className="mt-4 flex items-center gap-2 rounded-lg bg-green-50 p-3 text-sm text-green-700">
            <CheckCircle className="h-4 w-4" />
            Completed on {formatDateTime(task.completedDate)}
          </div>
        )}
      </div>

      <div className="card">
        <h3 className="text-sm font-semibold text-gray-900">Actions</h3>
        <div className="mt-3 flex flex-wrap gap-2">
          {task.status !== "COMPLETED" && (
            <>
              <button
                onClick={() => handleStatusUpdate("IN_PROGRESS")}
                disabled={actionLoading || task.status === "IN_PROGRESS"}
                className="btn-primary"
              >
                Mark In Progress
              </button>
              <button
                onClick={() => handleStatusUpdate("COMPLETED")}
                disabled={actionLoading}
                className="btn-primary bg-green-600 hover:bg-green-700"
              >
                Mark Complete
              </button>
              <button onClick={handleReschedule} disabled={actionLoading} className="btn-secondary">
                Reschedule
              </button>
            </>
          )}
          {!task.acknowledged && task.escalationLevel > 0 && (
            <button onClick={handleAcknowledge} disabled={actionLoading} className="btn-secondary">
              Acknowledge Escalation
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
