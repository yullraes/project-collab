import type { ReactNode } from "react";
import { TaskTable, type TaskTableProps } from "./TaskTable";
import "./Table.css";
import "./TaskListSection.css";

export interface TaskListSectionProps extends TaskTableProps {
  label: string;
  heading: ReactNode;
  filters: ReactNode;
  pagination: ReactNode;
  emptyContent?: ReactNode;
}
export function TaskListSection({
  label,
  heading,
  filters,
  pagination,
  emptyContent,
  ...table
}: TaskListSectionProps) {
  return (
    <section className="surface" aria-label={label}>
      <div className="task-list-heading">{heading}</div>
      {filters}
      {!table.loading && table.rows.length === 0 ? (
        emptyContent
      ) : (
        <TaskTable {...table} />
      )}
      {pagination}
    </section>
  );
}
