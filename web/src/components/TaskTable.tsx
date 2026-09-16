import { StatusBadge, type StatusBadgeProps } from "./StatusBadge";
import "./Table.css";
import "./TaskTable.css";

export interface TaskRow {
  id: string;
  numberLabel: string;
  title: string;
  href: string;
  status: StatusBadgeProps;
  assigneeLabel: string;
  assigneeMuted?: boolean;
  creatorLabel: string;
  updatedAt: string;
}
export interface TaskTableProps {
  rows: readonly TaskRow[];
  loading?: boolean;
  loadingRowCount?: number;
  loadingLabel?: string;
}
export function TaskTable({
  rows,
  loading = false,
  loadingRowCount = 7,
  loadingLabel,
}: TaskTableProps) {
  return (
    <div className="table-scroll" aria-busy={loading}>
      <table className="task-table">
        <thead>
          <tr>
            <th scope="col" className="number-col">
              번호
            </th>
            <th scope="col">제목</th>
            <th scope="col">상태</th>
            <th scope="col">담당자</th>
            <th scope="col" className="creator-col">
              생성자
            </th>
            <th scope="col" className="date-col">
              최근 수정일
            </th>
          </tr>
        </thead>
        <tbody>
          {loading
            ? Array.from({ length: loadingRowCount }, (_, index) => (
                <tr key={index} aria-hidden="true">
                  <td className="number-col">
                    <span className="skeleton" />
                  </td>
                  <td>
                    <span className="skeleton" />
                  </td>
                  <td>
                    <span className="skeleton" />
                  </td>
                  <td>
                    <span className="skeleton" />
                  </td>
                  <td className="creator-col">
                    <span className="skeleton" />
                  </td>
                  <td className="date-col">
                    <span className="skeleton" />
                  </td>
                </tr>
              ))
            : rows.map((row) => (
                <tr key={row.id}>
                  <td className="number-col muted">{row.numberLabel}</td>
                  <td>
                    <a className="title-link" href={row.href}>
                      {row.title}
                    </a>
                  </td>
                  <td>
                    <StatusBadge {...row.status} />
                  </td>
                  <td className={row.assigneeMuted ? "muted" : undefined}>
                    {row.assigneeLabel}
                  </td>
                  <td className="creator-col">{row.creatorLabel}</td>
                  <td className="date date-col">{row.updatedAt}</td>
                </tr>
              ))}
        </tbody>
      </table>
      {loading && (
        <span className="sr-only" role="status">
          {loadingLabel}
        </span>
      )}
    </div>
  );
}
