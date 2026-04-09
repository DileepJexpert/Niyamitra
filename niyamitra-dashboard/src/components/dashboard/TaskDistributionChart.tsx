"use client";

import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts";

interface TaskDistributionChartProps {
  completed: number;
  pending: number;
  overdue: number;
  inProgress: number;
}

const COLORS = ["#22c55e", "#eab308", "#ef4444", "#3b82f6"];

export function TaskDistributionChart({ completed, pending, overdue, inProgress }: TaskDistributionChartProps) {
  const data = [
    { name: "Completed", value: completed },
    { name: "Pending", value: pending },
    { name: "Overdue", value: overdue },
    { name: "In Progress", value: inProgress },
  ].filter((d) => d.value > 0);

  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-900">Task Distribution</h3>
      <p className="text-sm text-gray-500">Current compliance task breakdown</p>
      <div className="mt-4 h-64">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={90}
              paddingAngle={4}
              dataKey="value"
            >
              {data.map((_, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
