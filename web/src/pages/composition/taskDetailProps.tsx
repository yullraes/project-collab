import { Button } from '../../components/Button'
import { Notice } from '../../components/Notice'
import { ActionMenu } from '../../components/ActionMenu'
import type { TaskDetailContentProps } from '../../components/TaskDetailContent'
import type { DetailFixture, ProjectData, TaskState } from '../../data/demo'

export function createTaskDetailProps(detail: DetailFixture, project: ProjectData, labels: Record<TaskState, string>, mobile = false) {
  const { task } = detail
  const actions = <>{detail.secondary && <Button>{detail.secondary}</Button>}{detail.primary && <Button tone="primary" disabled={detail.primaryDisabled}>{detail.primary}</Button>}</>
  const content: TaskDetailContentProps = {
    title: task.title,
    status: { state: task.state, label: labels[task.state] },
    description: task.description,
    guidance: detail.instruction,
    notice: detail.rejection && <Notice title="반려 사유" tone="danger">{detail.rejection}</Notice>,
    metadata: {
      layout: !mobile && ['REJECTED', 'ACCEPTED'].includes(task.state) ? 'grid' : 'rows',
      items: [
        ['생성자', task.creator],
        ['담당자', <>{task.assignee}{detail.assignment && <Button tone="quiet">변경</Button>}</>],
        ['생성일', project.createdAt], ['최근 수정일', project.updatedAt],
      ],
    },
    note: detail.note,
    actions: mobile ? undefined : actions,
  }
  const menu = <ActionMenu label="추가 작업">{detail.menu.map(label =>
    <Button key={label} tone={label.includes('삭제') || label.includes('철회') ? 'danger' : 'quiet'}>{label}</Button>
  )}</ActionMenu>

  return { content, actions, menu, numberLabel: `#${task.taskId}`, label: task.title }
}
