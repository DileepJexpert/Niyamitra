"use client";

import { getScoreColor, getScoreBgColor } from "@/lib/utils";

interface AnupalanScoreGaugeProps {
  score: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
}

export function AnupalanScoreGauge({ score, totalTasks, completedTasks, overdueTasks }: AnupalanScoreGaugeProps) {
  const radius = 80;
  const circumference = 2 * Math.PI * radius;
  const progress = (score / 100) * circumference;
  const roundedScore = Math.round(score);

  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-900">Anupalan Score</h3>
      <p className="text-sm text-gray-500">Compliance health indicator</p>
      <div className="mt-4 flex items-center justify-center">
        <div className="relative">
          <svg width="200" height="200" viewBox="0 0 200 200">
            <circle cx="100" cy="100" r={radius} fill="none" stroke="#e5e7eb" strokeWidth="12" />
            <circle
              cx="100"
              cy="100"
              r={radius}
              fill="none"
              stroke="currentColor"
              strokeWidth="12"
              strokeDasharray={circumference}
              strokeDashoffset={circumference - progress}
              strokeLinecap="round"
              transform="rotate(-90 100 100)"
              className={getScoreColor(score)}
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className={`text-4xl font-bold ${getScoreColor(score)}`}>{roundedScore}</span>
            <span className="text-sm text-gray-500">out of 100</span>
          </div>
        </div>
      </div>
      <div className="mt-4 grid grid-cols-3 gap-4 text-center">
        <div>
          <p className="text-2xl font-bold text-gray-900">{totalTasks}</p>
          <p className="text-xs text-gray-500">Total</p>
        </div>
        <div>
          <p className="text-2xl font-bold text-green-600">{completedTasks}</p>
          <p className="text-xs text-gray-500">Completed</p>
        </div>
        <div>
          <p className="text-2xl font-bold text-red-600">{overdueTasks}</p>
          <p className="text-xs text-gray-500">Overdue</p>
        </div>
      </div>
      <div className="mt-4">
        <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
          <div className={`h-full rounded-full ${getScoreBgColor(score)}`} style={{ width: `${score}%` }} />
        </div>
      </div>
    </div>
  );
}
