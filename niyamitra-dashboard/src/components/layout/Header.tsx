"use client";

import { Bell, Search, User } from "lucide-react";

export function Header() {
  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6">
      <div className="flex items-center gap-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search tasks, documents..."
            className="rounded-lg border border-gray-300 bg-gray-50 py-2 pl-10 pr-4 text-sm focus:border-niyamitra-500 focus:outline-none focus:ring-1 focus:ring-niyamitra-500"
          />
        </div>
      </div>
      <div className="flex items-center gap-4">
        <button className="relative rounded-lg p-2 text-gray-400 hover:bg-gray-50 hover:text-gray-600">
          <Bell className="h-5 w-5" />
          <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-red-500" />
        </button>
        <div className="flex items-center gap-2 rounded-lg border border-gray-200 px-3 py-1.5">
          <User className="h-5 w-5 text-gray-400" />
          <span className="text-sm font-medium text-gray-700">Admin</span>
        </div>
      </div>
    </header>
  );
}
