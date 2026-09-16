import type { ReactNode } from "react";
import "./Metadata.css";

export interface MetadataProps {
  items: readonly (readonly [string, ReactNode])[];
  layout?: "rows" | "grid";
}
export function Metadata({ items, layout = "rows" }: MetadataProps) {
  return (
    <dl className={`metadata metadata--${layout}`}>
      {items.map(([label, value]) => (
        <div key={label}>
          <dt>{label}</dt>
          <dd>{value}</dd>
        </div>
      ))}
    </dl>
  );
}
