import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  trend?: { value: number; positive: boolean };
  className?: string;
}

export function StatCard({ title, value, subtitle, icon: Icon, trend, className }: StatCardProps) {
  return (
    <div className={cn("card", className)}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500">{title}</p>
          <p className="mt-1 text-3xl font-bold text-gray-900">{value}</p>
          {subtitle && <p className="mt-1 text-sm text-gray-500">{subtitle}</p>}
          {trend && (
            <p className={cn("mt-1 text-sm font-medium", trend.positive ? "text-green-600" : "text-red-600")}>
              {trend.positive ? "+" : ""}{trend.value}% from last month
            </p>
          )}
        </div>
        <div className="rounded-lg bg-niyamitra-50 p-3">
          <Icon className="h-6 w-6 text-niyamitra-600" />
        </div>
      </div>
    </div>
  );
}
