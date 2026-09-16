import { Field } from '../../components/Field'
import { Notice } from '../../components/Notice'
import { SelectField } from '../../components/SelectField'
import type * as Demo from '../../data/demo'
import { TaskFields } from '../../components/TaskFields'

type TaskFormMode = 'proposal' | 'register' | 'revise'
export function createTaskForm(mode: TaskFormMode, data: Pick<typeof Demo, 'tasks' | 'members' | 'taskForms' | 'memberManagement'>) {
  const { tasks, members, taskForms } = data
  const formData = {
    proposal: {
      ...taskForms.proposal,
      guidance: '제출하면 승인 대기 상태로 등록됩니다.',
      assignee: <Field id="task-assignee" label="담당자" value={`${tasks[6].assignee} (나)`} hint="담당자는 본인으로 자동 지정되며 변경할 수 없습니다." />,
    },
    register: {
      ...taskForms.register,
      guidance: '승인된 작업으로 등록됩니다.',
      assignee: <SelectField id="task-assignee" label="담당자" value={String(data.memberManagement.currentUserId)} options={[
        ...members.map(member => ({ value: String(member.id), label: member.name + (member.id === data.memberManagement.currentUserId ? ' (나)' : '') })),
        { value: '', label: '미할당' },
      ]} />,
    },
    revise: {
      ...taskForms.revise,
      guidance: '내용을 변경하면 승인 대기로 돌아갑니다. 다시 승인받은 뒤 진행할 수 있습니다.',
      assignee: undefined,
    },
  }

  const form = formData[mode]
  return <>
    {mode !== 'revise' && <p className="muted">할 일과 완료 조건을 함께 작성해 주세요.</p>}
    <TaskFields title={{ id: 'task-title', label: '제목', required: true, value: form.title }}
      description={{ id: 'task-description', label: '작업 내용', required: true, multiline: true, value: form.description }}
      assignee={form.assignee} />
    <Notice>{form.guidance}</Notice>
  </>
}
