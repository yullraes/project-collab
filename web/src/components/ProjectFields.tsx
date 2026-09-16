import { Field, type FieldProps } from "./Field";

export interface ProjectFieldsProps {
  name: FieldProps;
  description: FieldProps;
}
export function ProjectFields({ name, description }: ProjectFieldsProps) {
  return (
    <>
      <Field {...name} />
      <Field {...description} />
    </>
  );
}
