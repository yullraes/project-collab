import type { ReactNode } from "react";
import type { SelectOption } from "./SelectField";
import "./TaskFilterBar.css";

export interface TaskFilterBarProps {
  statusOptions: readonly SelectOption[];
  statusValue: string;
  searchValue: string;
  searchAction: ReactNode;
  disabled?: boolean;
}
export function TaskFilterBar({
  statusOptions,
  statusValue,
  searchValue,
  searchAction,
  disabled,
}: TaskFilterBarProps) {
  return (
    <div className="filters">
      <label>
        <span className="sr-only">상태</span>
        <select defaultValue={statusValue} disabled={disabled}>
          {statusOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>
      <label className="search-field">
        <span className="sr-only">제목 또는 내용 검색</span>
        <input
          placeholder="제목 또는 내용 검색"
          value={searchValue}
          readOnly
          disabled={disabled}
        />
      </label>
      {searchAction}
    </div>
  );
}
