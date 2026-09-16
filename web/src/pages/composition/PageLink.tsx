import type { ReactNode } from 'react'
import { ActionLink, type Tone } from '../../components/Button'

// 시안 주소는 제품 컴포넌트에 전달하기 전에 이곳에서 만든다.
export function PageLink({ screen, children, tone = 'secondary', className = '', label }: {
  screen: string; children: ReactNode; tone?: Tone; className?: string; label?: string
}) {
  return <ActionLink href={`?screen=${screen}`} tone={tone} className={className} aria-label={label}>{children}</ActionLink>
}
