import { cn, getStatusColor } from "@/lib/utils";

interface BadgeProps {
  status: string;
  className?: string;
}

export function Badge({ status, className }: BadgeProps) {
  return (
    <span className={cn("badge", getStatusColor(status), className)}>
      {status.replace("_", " ")}
    </span>
  );
}
