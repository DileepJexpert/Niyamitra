"use client";

import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

// Sample trend data — in production this would come from an API endpoint
const trendData = [
  { month: "Jan", score: 65 },
  { month: "Feb", score: 68 },
  { month: "Mar", score: 72 },
  { month: "Apr", score: 70 },
  { month: "May", score: 75 },
  { month: "Jun", score: 78 },
];

export function ComplianceTrend() {
  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-900">Compliance Trend</h3>
      <p className="text-sm text-gray-500">Anupalan Score over time</p>
      <div className="mt-4 h-64">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={trendData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="month" tick={{ fontSize: 12 }} />
            <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
            <Tooltip />
            <Area
              type="monotone"
              dataKey="score"
              stroke="#2563eb"
              fill="#dbeafe"
              strokeWidth={2}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
