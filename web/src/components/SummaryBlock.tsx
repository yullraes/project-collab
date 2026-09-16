import type { ReactNode } from "react";
import "./SummaryBlock.css";

export interface SummaryBlockProps {
  title: string;
  description?: string;
  eyebrow?: ReactNode;
  meta?: ReactNode;
}
export function SummaryBlock({
  title,
  description,
  eyebrow,
  meta,
}: SummaryBlockProps) {
  return (
    <div className="summary-block">
      {eyebrow}
      <h3>{title}</h3>
      {description && <p>{description}</p>}
      {meta && <small>{meta}</small>}
    </div>
  );
}
