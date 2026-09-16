import type { ReactNode } from 'react'
import { Button } from '../../components/Button'
import type { DialogProps } from '../../components/Dialog'
import { Field } from '../../components/Field'
import { Metadata } from '../../components/Metadata'
import { Notice } from '../../components/Notice'
import { StatusBadge } from '../../components/StatusBadge'
import { SummaryBlock } from '../../components/SummaryBlock'
import { ProjectFields } from '../../components/ProjectFields'
import { ChoiceGroup } from '../../components/ChoiceGroup'
import { UserLookup } from '../../components/UserLookup'
import { PageLink } from './PageLink'
import { createTaskForm } from './taskForm'
import { createProjectFields } from './pageProps'
import type * as Demo from '../../data/demo'

export type DialogId = 'createProject' | 'proposeTask' | 'registerTask' | 'editTask' | 'approveTask' | 'rejectTask' | 'requestChanges' | 'addMember' | 'changeRole' | 'conflict' | 'assignTask' | 'removeMember' | 'deleteProject'

// Page가 선택한 시나리오와 데이터를 필드·행동·대화상자 props로 조합한다.
export function createDialogProps(id: DialogId, data: Pick<typeof Demo, 'members' | 'project' | 'tasks' | 'stateLabels' | 'memberManagement' | 'lookupUser' | 'taskForms'>): DialogProps {
  const { members, project, tasks, stateLabels, memberManagement, lookupUser } = data
  const projectFields = createProjectFields(project)
  const owner = members.find(member => member.id === memberManagement.protectedOwnerId)!
  const removedMember = members.find(member => member.id === memberManagement.removedMemberId)!
  function taskSummary(index = 6) {
    const task = tasks[index]
    return <SummaryBlock title={task.title} description={task.description}
      eyebrow={<StatusBadge state={task.state} label={stateLabels[task.state]} />} meta={`생성자 ${task.creator} · 담당자 ${task.assignee}`} />
  }
  interface DialogComposition { title: string; body: ReactNode; submit: string; back: string; danger?: boolean; disabled?: boolean; size?: 'small' | 'medium' | 'large' }
  const dialogs: Record<DialogId, DialogComposition> = {
    createProject: { title: '프로젝트 만들기', size: 'small', back: '01', submit: '프로젝트 만들기', body: <>
      <ProjectFields {...projectFields} /><Notice>프로젝트를 만들면 소유자로 참여합니다.</Notice>
    </> },
    proposeTask: { title: '작업 만들기', back: '03', submit: '제안 제출', size: 'medium', body: createTaskForm('proposal', data) },
    registerTask: { title: '작업 만들기', back: '03', submit: '작업 등록', size: 'medium', body: createTaskForm('register', data) },
    editTask: { title: '작업 내용 수정', back: '07', submit: '저장하고 재승인 요청', size: 'large', body: createTaskForm('revise', data) },
    approveTask: { title: '작업 승인', back: '04', submit: '승인', body: <>
      <p className="muted">내용과 담당자를 확인해 주세요. 담당자가 지정된 작업은 승인 후 시작할 수 있습니다.</p>{taskSummary()}
      <ChoiceGroup legend="담당자 지정" name="approval-assignee" options={[
        { id: 'keep', value: 'keep', title: `현재 담당자 유지 · ${tasks[6].assignee}`, detail: '현재 담당자를 그대로 유지합니다.', checked: true },
        { id: 'change', value: 'change', title: '다른 멤버 지정', detail: '프로젝트의 다른 멤버에게 배정합니다.' },
        { id: 'none', value: 'none', title: '미할당', detail: '작업 시작 전에 담당자를 지정해야 합니다.' },
      ]} />
    </> },
    rejectTask: { title: '작업 반려', back: '04', submit: '반려', danger: true, body: <>{taskSummary()}<Field id="rejection-reason" label="반려 사유" placeholder="보완이 필요한 내용을 적어 주세요." required multiline error="반려 사유를 입력해 주세요." /><p className="field-hint">반려 사유가 작업 상세에 표시됩니다.</p></> },
    requestChanges: { title: '보완을 요청할까요?', size: 'small', back: '08', submit: '보완 요청', body: <>{taskSummary(2)}<Notice>작업이 진행 중으로 돌아갑니다.</Notice></> },
    addMember: { title: '멤버 추가', back: '16', submit: '추가', body: <>
      <UserLookup field={{ id: 'member-id', label: '사용자 ID', value: String(lookupUser.id), required: true }} lookupAction={<Button>사용자 확인</Button>}
        result={<Notice title="사용자가 확인되었습니다" tone="success"><Metadata items={ [['이름', lookupUser.name], ['사용자 ID', String(lookupUser.id)], ['이메일', lookupUser.email]] } /></Notice>} />
      <Metadata items={ [['추가되는 역할', '멤버']] } /><p className="muted">사용자 ID로 확인된 사용자를 프로젝트 멤버로 추가합니다.</p>
    </> },
    changeRole: { title: '역할 변경', back: '16', submit: '변경사항 저장', disabled: true, body: <>
      <SummaryBlock title={owner.name} description={`사용자 ID ${owner.id} · ${owner.email}`} meta={`현재 역할: ${owner.role}`} />
      <ChoiceGroup legend="새 역할" name="role" options={[
        { id: 'owner', value: 'OWNER', title: '소유자', detail: '현재 역할', checked: true },
        { id: 'admin', value: 'ADMIN', title: '관리자', detail: '멤버 및 작업 관리', disabled: true },
        { id: 'member', value: 'MEMBER', title: '멤버', detail: '작업 제안 및 할당된 작업 수행', disabled: true },
      ]} />
      <Notice tone="warning">프로젝트에는 소유자가 최소 한 명 필요합니다. 다른 소유자를 지정한 뒤 변경해 주세요.</Notice>
    </> },
    conflict: { title: '최신 내용을 확인해 주세요', back: '07', submit: '최신 내용 확인', body: <Notice tone="warning">다른 사용자가 작업을 변경했습니다. 작성 중인 내용은 아래에 남아 있습니다. 최신 내용을 확인한 뒤 다시 진행해 주세요.</Notice> },
    assignTask: { title: '담당자 지정', back: '03', submit: '지정', body: <>
      {taskSummary(0)}<p className="muted">프로젝트 멤버 중 작업을 진행할 담당자를 지정해 주세요.</p>
      <ChoiceGroup legend="담당자" name="assignee" hiddenLegend options={members.map(member => ({ id: String(member.id), value: String(member.id), title: member.name, detail: `${member.role} · ${member.email}`, checked: member.id === memberManagement.selectedAssigneeId }))} /><p className="field-hint">지정된 담당자가 작업을 시작할 수 있습니다.</p>
    </> },
    removeMember: { title: '멤버를 제거할까요?', back: '16', submit: '멤버 제거', danger: true, body: <>
      <SummaryBlock title={removedMember.name} description={`사용자 ID ${removedMember.id} · ${removedMember.role}`} meta={removedMember.email} /><Notice tone="warning"><p>이 멤버가 담당한 작업은 미할당으로 바뀝니다.</p><p>진행 중·검토 중인 작업은 승인됨으로 돌아갑니다.</p><p>완료된 작업의 담당자 정보도 해제됩니다.</p></Notice>
    </> },
    deleteProject: { title: '프로젝트를 삭제할까요?', back: '19', submit: '프로젝트 삭제', danger: true, body: <>
      <SummaryBlock title={project.name} eyebrow={<small>대상 프로젝트</small>} /><Notice tone="danger"><p>프로젝트의 모든 작업과 멤버 정보가 함께 삭제됩니다.</p><p>삭제 후 복구할 수 없습니다.</p></Notice>
    </> },
  }

  const dialog = dialogs[id]
  return {
    title: dialog.title,
    size: dialog.size,
    closeAction: <PageLink screen={dialog.back} tone="quiet" label="닫기" className="icon-button">×</PageLink>,
    footer: <><PageLink screen={dialog.back}>취소</PageLink><Button tone={dialog.danger ? 'danger' : 'primary'} disabled={dialog.disabled}>{dialog.submit}</Button></>,
    children: dialog.body,
  }
}
