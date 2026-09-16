import type { ReactNode } from "react";
import "./Table.css";
import "./ProjectsTable.css";

export interface ProjectRow {
  id: string;
  name: string;
  description: string;
  updatedAt: string;
  href: string;
}
export function ProjectsTable({
  rows,
  emptyContent,
}: {
  rows: readonly ProjectRow[];
  emptyContent?: ReactNode;
}) {
  return (
    <div className="surface table-scroll">
      {rows.length === 0 ? (
        emptyContent
      ) : (
        <table className="project-table">
          <thead>
            <tr>
              <th scope="col">프로젝트</th>
              <th scope="col">설명</th>
              <th scope="col">최근 수정일</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>
                  <a className="title-link" href={row.href}>
                    {row.name}
                  </a>
                </td>
                <td>{row.description}</td>
                <td className="date">{row.updatedAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
