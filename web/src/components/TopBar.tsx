import "./TopBar.css";

export interface TopBarProps {
  title?: string;
  userLabel: string;
}

// 원본: design/stitch/03.html의 상단 머리글.
export function TopBar({ title, userLabel }: TopBarProps) {
  return (
    <header className="topbar">
      {title && <div className="topbar__title">{title}</div>}
      <div className="topbar__user">
        <span>{userLabel}</span>
      </div>
    </header>
  );
}
