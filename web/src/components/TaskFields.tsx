import type { ReactNode } from "react";
import { Field, type FieldProps } from "./Field";

export interface TaskFieldsProps {
  title: FieldProps;
  description: FieldProps;
  assignee?: ReactNode;
}
export function TaskFields({ title, description, assignee }: TaskFieldsProps) {
  return (
    <>
      <Field {...title} />
      {assignee}
      <Field {...description} />
    </>
  );
}
