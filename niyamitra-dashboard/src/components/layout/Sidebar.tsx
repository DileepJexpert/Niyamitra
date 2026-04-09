"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  ClipboardList,
  FileText,
  Bell,
  Building2,
  Shield,
} from "lucide-react";

const navigation = [
  { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { name: "Tasks", href: "/tasks", icon: ClipboardList },
  { name: "Documents", href: "/documents", icon: FileText },
  { name: "Notifications", href: "/notifications", icon: Bell },
  { name: "Tenants", href: "/tenants", icon: Building2 },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <div className="hidden w-64 flex-shrink-0 border-r border-gray-200 bg-white lg:block">
      <div className="flex h-16 items-center gap-3 border-b border-gray-200 px-6">
        <Shield className="h-8 w-8 text-niyamitra-600" />
        <div>
          <h1 className="text-lg font-bold text-niyamitra-900">Niyamitra</h1>
          <p className="text-xs text-gray-500">Compliance Platform</p>
        </div>
      </div>
      <nav className="mt-4 space-y-1 px-3">
        {navigation.map((item) => {
          const isActive = pathname.startsWith(item.href);
          return (
            <Link
              key={item.name}
              href={item.href}
              className={cn(
                "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                isActive
                  ? "bg-niyamitra-50 text-niyamitra-700"
                  : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
              )}
            >
              <item.icon className={cn("h-5 w-5", isActive ? "text-niyamitra-600" : "text-gray-400")} />
              {item.name}
            </Link>
          );
        })}
      </nav>
      <div className="absolute bottom-4 left-0 w-64 px-4">
        <div className="rounded-lg bg-kavach-50 p-3">
          <p className="text-xs font-medium text-kavach-700">Kavach AI Active</p>
          <p className="text-xs text-kavach-600">WhatsApp agent is running</p>
        </div>
      </div>
    </div>
  );
}
