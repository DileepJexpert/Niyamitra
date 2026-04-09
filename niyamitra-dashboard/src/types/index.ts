export interface Tenant {
  id: string;
  companyName: string;
  gstin: string;
  industryCategory: IndustryCategory;
  state: string;
  district: string;
  contactEmail: string;
  contactPhone: string;
  onboardedAt: string;
  active: boolean;
}

export interface User {
  id: string;
  tenantId: string;
  fullName: string;
  phone: string;
  email: string;
  role: UserRole;
  whatsappOptIn: boolean;
  preferredLanguage: string;
  active: boolean;
}

export interface ComplianceTask {
  id: string;
  tenantId: string;
  ruleId: string;
  ruleName: string;
  complianceCategory: ComplianceCategory;
  status: TaskStatus;
  dueDate: string;
  completedDate: string | null;
  assignedUserId: string;
  assignedUserName: string;
  notes: string;
  escalationLevel: number;
  acknowledged: boolean;
  createdAt: string;
}

export interface NiyamitraDocument {
  id: string;
  tenantId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadSource: string;
  processingStatus: DocumentProcessingStatus;
  extractionResult: Record<string, unknown> | null;
  uploadedAt: string;
  uploadedBy: string;
}

export interface AnupalanRule {
  id: string;
  ruleName: string;
  description: string;
  complianceCategory: ComplianceCategory;
  applicableIndustries: string;
  applicableStates: string;
  renewalFrequencyDays: number;
  penaltyDescription: string;
  active: boolean;
}

export interface AnupalanScore {
  tenantId: string;
  overallScore: number;
  overdueRatio: number;
  lateSubmissionRatio: number;
  pendingRatio: number;
  violationCount: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  pendingTasks: number;
}

export interface KavachNotification {
  id: string;
  tenantId: string;
  userId: string;
  channel: string;
  messageType: string;
  message: string;
  status: string;
  sentAt: string;
}

export interface DashboardStats {
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  pendingTasks: number;
  upcomingTasks: number;
  anupalanScore: number;
}

export type IndustryCategory =
  | "CHEMICAL"
  | "PHARMACEUTICAL"
  | "TEXTILE"
  | "FOOD_PROCESSING"
  | "AUTOMOBILE"
  | "ELECTRONICS"
  | "METAL"
  | "PLASTIC"
  | "PAPER"
  | "OTHER";

export type UserRole = "OWNER" | "FLOOR_MANAGER" | "COMPLIANCE_OFFICER" | "ACCOUNTANT";

export type TaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "OVERDUE" | "ESCALATED";

export type ComplianceCategory =
  | "ENVIRONMENTAL"
  | "FIRE_SAFETY"
  | "LABOR"
  | "FACTORY_LICENSE"
  | "HAZARDOUS_WASTE"
  | "BOILER_SAFETY";

export type DocumentProcessingStatus =
  | "UPLOADED"
  | "PROCESSING"
  | "EXTRACTED"
  | "FAILED"
  | "MANUAL_REVIEW";
