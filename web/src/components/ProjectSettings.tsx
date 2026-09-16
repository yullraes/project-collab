import type { ReactNode } from "react";
import { ProjectFields, type ProjectFieldsProps } from "./ProjectFields";
import "./ProjectSettings.css";

export interface ProjectSettingsProps {
  fields: ProjectFieldsProps;
  metadata: ReactNode;
  saveAction: ReactNode;
  danger: { title: string; description: string; action: ReactNode };
}
export function ProjectSettings({
  fields,
  metadata,
  saveAction,
  danger,
}: ProjectSettingsProps) {
  return (
    <section className="settings-form">
      <ProjectFields {...fields} />
      {metadata}
      {saveAction}
      <section className="danger-zone">
        <h2>{danger.title}</h2>
        <p>{danger.description}</p>
        {danger.action}
      </section>
    </section>
  );
}
