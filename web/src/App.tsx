import type { ReactNode } from "react";
import { ProjectsPage } from "./pages/ProjectsPage";
import { TasksPage } from "./pages/TasksPage";
import { MembersPage } from "./pages/MembersPage";
import { SettingsPage } from "./pages/SettingsPage";
import { ForbiddenPage } from "./pages/ForbiddenPage";
import { MobileTaskPage } from "./pages/MobileTaskPage";
import { DesignSystemPreview, GalleryPreview } from "./preview/PreviewPages";

// App은 정적 시안에 맞는 Page만 선택한다. 데이터와 컴포넌트 조합은 Page가 담당한다.
const screens: Record<string, ReactNode> = {
  "01": <ProjectsPage />,
  "02": <ProjectsPage create />,
  "03": <TasksPage />,
  "04": <TasksPage detail="pending" />,
  "05": <TasksPage detail="rejected" />,
  "06": <TasksPage detail="accepted" />,
  "07": <TasksPage detail="inProgress" />,
  "08": <TasksPage detail="inReview" />,
  "09": <TasksPage detail="done" />,
  "10": <TasksPage dialog="proposeTask" />,
  "11": <TasksPage dialog="registerTask" />,
  "12": <TasksPage detail="inProgress" dialog="editTask" />,
  "13": <TasksPage detail="pending" dialog="approveTask" />,
  "14": <TasksPage detail="pending" dialog="rejectTask" />,
  "15": <TasksPage detail="inReview" dialog="requestChanges" />,
  "16": <MembersPage />,
  "17": <MembersPage dialog="addMember" />,
  "18": <MembersPage dialog="changeRole" />,
  "19": <SettingsPage />,
  "20": <TasksPage detail="inProgress" dialog="conflict" />,
  "21": <TasksPage mode="empty" />,
  "22": <TasksPage mode="loading" />,
  "23": <ForbiddenPage />,
  "24": <TasksPage detail="selfApproval" />,
  "25": <TasksPage detail="unassigned" dialog="assignTask" />,
  "26": <MembersPage dialog="removeMember" />,
  "27": <SettingsPage confirmDelete />,
  "28": <MobileTaskPage />,
  gallery: <GalleryPreview />,
  system: <DesignSystemPreview />,
};

export default function App() {
  const screen =
    new URLSearchParams(window.location.search).get("screen") ?? "03";
  return screens[screen] ?? <GalleryPreview />;
}
