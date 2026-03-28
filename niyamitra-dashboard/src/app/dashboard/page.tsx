"use client";

import { useApi, useTenantId } from "@/lib/hooks";
import { taskApi } from "@/lib/api";
import { StatCard } from "@/components/ui/StatCard";
import { AnupalanScoreGauge } from "@/components/dashboard/AnupalanScoreGauge";
import { TaskDistributionChart } from "@/components/dashboard/TaskDistributionChart";
import { UpcomingDeadlines } from "@/components/dashboard/UpcomingDeadlines";
import { ComplianceTrend } from "@/components/dashboard/ComplianceTrend";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { ClipboardList, CheckCircle, AlertTriangle, Clock } from "lucide-react";
import type { ComplianceTask, AnupalanScore } from "@/types";

export default function DashboardPage() {
  const tenantId = useTenantId();

  const { data: tasks, loading: tasksLoading } = useApi<ComplianceTask[]>(
    () => taskApi.list(tenantId) as Promise<ComplianceTask[]>,
    [tenantId]
  );

  const { data: score, loading: scoreLoading } = useApi<AnupalanScore>(
    () => taskApi.score(tenantId) as Promise<AnupalanScore>,
    [tenantId]
  );

  if (tasksLoading || scoreLoading) return <LoadingSpinner />;

  const taskList = tasks || [];
  const completed = taskList.filter((t) => t.status === "COMPLETED").length;
  const overdue = taskList.filter((t) => t.status === "OVERDUE").length;
  const pending = taskList.filter((t) => t.status === "PENDING").length;
  const inProgress = taskList.filter((t) => t.status === "IN_PROGRESS").length;
  const anupalanScore = score?.overallScore ?? 0;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Dashboard</h2>
        <p className="text-sm text-gray-500">Compliance overview for your organization</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Tasks" value={taskList.length} icon={ClipboardList} />
        <StatCard title="Completed" value={completed} icon={CheckCircle} />
        <StatCard title="Overdue" value={overdue} icon={AlertTriangle} />
        <StatCard title="Pending" value={pending + inProgress} icon={Clock} />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <AnupalanScoreGauge
          score={anupalanScore}
          totalTasks={taskList.length}
          completedTasks={completed}
          overdueTasks={overdue}
        />
        <TaskDistributionChart
          completed={completed}
          pending={pending}
          overdue={overdue}
          inProgress={inProgress}
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <UpcomingDeadlines tasks={taskList} />
        <ComplianceTrend />
      </div>
    </div>
  );
}
