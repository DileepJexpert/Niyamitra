"use client";

import { formatDate, daysUntil, cn } from "@/lib/utils";
import { Clock, AlertTriangle } from "lucide-react";
import type { ComplianceTask } from "@/types";

interface UpcomingDeadlinesProps {
  tasks: ComplianceTask[];
}

export function UpcomingDeadlines({ tasks }: UpcomingDeadlinesProps) {
  const upcoming = tasks
    .filter((t) => t.status !== "COMPLETED")
    .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime())
    .slice(0, 5);

  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-900">Upcoming Deadlines</h3>
      <p className="text-sm text-gray-500">Tasks due soon</p>
      <div className="mt-4 space-y-3">
        {upcoming.length === 0 ? (
          <p className="text-sm text-gray-400 py-4 text-center">No upcoming deadlines</p>
        ) : (
          upcoming.map((task) => {
            const days = daysUntil(task.dueDate);
            const isUrgent = days <= 5;
            return (
              <div key={task.id} className="flex items-center justify-between rounded-lg border border-gray-100 p-3">
                <div className="flex items-center gap-3">
                  {isUrgent ? (
                    <AlertTriangle className="h-5 w-5 text-red-500" />
                  ) : (
                    <Clock className="h-5 w-5 text-gray-400" />
                  )}
                  <div>
                    <p className="text-sm font-medium text-gray-900">{task.ruleName}</p>
                    <p className="text-xs text-gray-500">{task.complianceCategory}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className={cn("text-sm font-medium", isUrgent ? "text-red-600" : "text-gray-700")}>
                    {days <= 0 ? "Overdue" : `${days} days`}
                  </p>
                  <p className="text-xs text-gray-500">{formatDate(task.dueDate)}</p>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
