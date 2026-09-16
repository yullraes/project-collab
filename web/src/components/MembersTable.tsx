import type { ReactNode } from "react";
import "./Table.css";
import "./MembersTable.css";

export interface MemberRow {
  id: string;
  name: string;
  annotation?: string;
  userId: string;
  email: string;
  roleLabel: string;
  joinedAt: string;
  actions: ReactNode;
}
export function MembersTable({
  rows,
  totalLabel,
  emptyContent,
}: {
  rows: readonly MemberRow[];
  totalLabel: string;
  emptyContent?: ReactNode;
}) {
  return (
    <div className="surface table-scroll">
      {rows.length === 0 ? (
        emptyContent
      ) : (
        <table className="members-table">
          <thead>
            <tr>
              {["이름", "사용자 ID", "이메일", "역할", "참여일", "관리"].map(
                (label) => (
                  <th scope="col" key={label}>
                    {label}
                  </th>
                ),
              )}
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>
                  <strong>{row.name}</strong>
                  {row.annotation && (
                    <span className="muted"> {row.annotation}</span>
                  )}
                </td>
                <td className="muted">{row.userId}</td>
                <td>{row.email}</td>
                <td>
                  <span className="role-label">{row.roleLabel}</span>
                </td>
                <td className="date">{row.joinedAt}</td>
                <td>
                  <div className="table-actions">{row.actions}</div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <div className="table-footer">{totalLabel}</div>
    </div>
  );
}
