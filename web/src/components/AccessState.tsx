import type { ReactNode } from "react";
import "./AccessState.css";

export function AccessState({
  message,
  action,
}: {
  message: ReactNode;
  action: ReactNode;
}) {
  return (
    <div className="access-state">
      {message}
      {action}
    </div>
  );
}
