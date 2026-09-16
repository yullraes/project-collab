import "./StatusBadge.css";

export type TaskState =
  | "PENDING"
  | "REJECTED"
  | "ACCEPTED"
  | "IN_PROGRESS"
  | "IN_REVIEW"
  | "DONE";
export interface StatusBadgeProps {
  state: TaskState;
  label: string;
}
export function StatusBadge({ state, label }: StatusBadgeProps) {
  return (
    <span className={`status status--${state.toLowerCase()}`}>{label}</span>
  );
}
