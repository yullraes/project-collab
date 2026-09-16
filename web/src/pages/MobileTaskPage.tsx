import { MobileDetail } from "../components/MobileDetail";
import { TaskDetailContent } from "../components/TaskDetailContent";
import { createTaskDetailProps } from "./composition/taskDetailProps";
import { details, project, stateLabels, type DetailId } from "../data/demo";

export function MobileTaskPage({ detail = "rejected" }: { detail?: DetailId }) {
  const data = details[detail];
  const props = createTaskDetailProps(data, project, stateLabels, true);

  return (
    <MobileDetail
      projectName={project.name}
      viewerLabel={data.viewer}
      backAction={<a href="?screen=03">← 작업</a>}
      menu={props.menu}
      actions={props.actions}
    >
      <span className="muted">{props.numberLabel}</span>
      <TaskDetailContent {...props.content} />
    </MobileDetail>
  );
}
