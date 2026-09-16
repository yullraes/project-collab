import type { ReactNode } from "react";
import "./Sidebar.css";

export interface SidebarLink {
  label: string;
  href: string;
  icon?: ReactNode;
}

export interface SidebarItem extends SidebarLink {
  id: string;
  active?: boolean;
}

export interface SidebarProps {
  brand: {
    name: string;
    description?: string;
  };
  backLink?: SidebarLink;
  projectName?: string;
  items: readonly SidebarItem[];
  navigationLabel: string;
}

// 원본: design/stitch/03.html의 사이드바.
export function Sidebar({
  brand,
  backLink,
  projectName,
  items,
  navigationLabel,
}: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="sidebar__content">
        <div className="sidebar__brand">
          <div className="sidebar__brand-name">{brand.name}</div>
          {brand.description && (
            <div className="sidebar__brand-description">
              {brand.description}
            </div>
          )}
        </div>

        {backLink && (
          <div className="sidebar__back">
            <a className="sidebar__back-link" href={backLink.href}>
              {backLink.icon && (
                <span className="sidebar__icon" aria-hidden="true">
                  {backLink.icon}
                </span>
              )}
              <span>{backLink.label}</span>
            </a>
          </div>
        )}

        <div className="sidebar__navigation">
          {projectName && <div className="sidebar__project">{projectName}</div>}
          <nav className="sidebar__menu" aria-label={navigationLabel}>
            {items.map((item) => (
              <a
                key={item.id}
                href={item.href}
                aria-current={item.active ? "page" : undefined}
                className="sidebar__item"
              >
                {item.icon && (
                  <span className="sidebar__icon" aria-hidden="true">
                    {item.icon}
                  </span>
                )}
                <span>{item.label}</span>
              </a>
            ))}
          </nav>
        </div>
      </div>
    </aside>
  );
}
