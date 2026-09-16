import type { SidebarItem, SidebarProps } from '../../components/Sidebar'
import type { TopBarProps } from '../../components/TopBar'
import type { ProjectFieldsProps } from '../../components/ProjectFields'
import type { ProjectData } from '../../data/demo'

// 페이지 공통 탐색 구성을 AppShell의 props로 변환한다.
const brand = {
  name: 'Project Collab',
  description: '업무 협업 도구',
}

const projectItems: readonly SidebarItem[] = [
  { id: 'tasks', label: '작업', href: '?screen=03' },
  { id: 'members', label: '멤버', href: '?screen=16' },
  { id: 'settings', label: '설정', href: '?screen=19' },
]

const globalItems: readonly SidebarItem[] = [
  { id: 'projects', label: '내 프로젝트', href: '?screen=01' },
]

export function createShellProps({ active = '작업', viewer, global = false, projectName }: {
  active?: string; viewer: string; global?: boolean; projectName?: string
}) {
  const sidebar: SidebarProps = {
    brand,
    navigationLabel: '주 메뉴',
    backLink: global ? undefined : {
      label: '내 프로젝트로',
      href: '?screen=01',
      icon: '←',
    },
    projectName: global ? undefined : projectName,
    items: (global ? globalItems : projectItems).map((item) => ({
      ...item,
      active: item.label === active,
    })),
  }

  const topBar: TopBarProps = {
    title: global ? undefined : '프로젝트 개요',
    userLabel: viewer,
  }

  return { sidebar, topBar }
}

export function createProjectFields(project: ProjectData): ProjectFieldsProps {
  return {
    name: { id: 'project-name', label: '프로젝트 이름', value: project.name, required: true },
    description: { id: 'project-description', label: '프로젝트 설명', value: project.description, required: true, multiline: true },
  }
}
