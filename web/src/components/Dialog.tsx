import { useId, type ReactNode } from "react";
import "./Dialog.css";

export interface DialogProps {
  title: string;
  children: ReactNode;
  footer: ReactNode;
  closeAction: ReactNode;
  size?: "small" | "medium" | "large";
}
export function Dialog({
  title,
  children,
  footer,
  closeAction,
  size = "medium",
}: DialogProps) {
  const titleId = useId();
  return (
    <div className="overlay">
      <section
        className={`dialog dialog--${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <header className="dialog-header">
          <h2 id={titleId}>{title}</h2>
          {closeAction}
        </header>
        <div className="dialog-body">{children}</div>
        <footer className="dialog-footer">{footer}</footer>
      </section>
    </div>
  );
}
