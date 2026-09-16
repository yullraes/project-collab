import "./AppShell.css";
import type { ReactNode } from "react";
import { Sidebar } from "./Sidebar";
import type { SidebarProps } from "./Sidebar";
import { TopBar } from "./TopBar";
import type { TopBarProps } from "./TopBar";

export interface AppShellProps {
  children: ReactNode;
  sidebar: SidebarProps;
  topBar: TopBarProps;
}

export function AppShell({ children, sidebar, topBar }: AppShellProps) {
  return (
    <div className="app-shell">
      <a href="#main" className="skip-link">
        본문으로 이동
      </a>
      <Sidebar {...sidebar} />
      <div className="workspace">
        <TopBar {...topBar} />
        <main id="main" className="main-content">
          {children}
        </main>
      </div>
    </div>
  );
}
