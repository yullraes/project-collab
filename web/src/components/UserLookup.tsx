import type { ReactNode } from "react";
import { Field, type FieldProps } from "./Field";
import "./UserLookup.css";

export function UserLookup({
  field,
  lookupAction,
  result,
}: {
  field: FieldProps;
  lookupAction: ReactNode;
  result: ReactNode;
}) {
  return (
    <>
      <div className="lookup-row">
        <Field {...field} />
        {lookupAction}
      </div>
      {result}
    </>
  );
}
