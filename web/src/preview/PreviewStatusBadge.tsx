import { StatusBadge, type TaskState } from "../components/StatusBadge";
import { stateLabels } from "../data/demo";

export function PreviewStatusBadge({ state }: { state: TaskState }) {
  return <StatusBadge state={state} label={stateLabels[state]} />;
}
