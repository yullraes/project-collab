import "./Preview.css";
import {
  Button,
  Field,
  Notice,
  PageHeading,
  StatusBadge,
  ViewLink,
} from "./ui";
import { screenNames } from "../data/demo";
import type { TaskState } from "../data/demo";

export function GalleryPreview() {
  return (
    <main className="preview-page">
      <PageHeading
        title="화면 모음"
        description="Stitch 화면 28개를 공통 컴포넌트로 구성한 정적 시안입니다. 업무 버튼과 검색·저장은 동작하지 않습니다."
        action={<ViewLink screen="system">디자인 시스템</ViewLink>}
      />
      <div className="preview-grid">
        {screenNames.map((title, index) => {
          const id = String(index + 1).padStart(2, "0");
          return (
            <a href={`?screen=${id}`} key={id} className="preview-card">
              <span>{id}</span>
              <strong>{title}</strong>
              <span aria-hidden="true">↗</span>
            </a>
          );
        })}
      </div>
    </main>
  );
}
export function DesignSystemPreview() {
  return (
    <main className="preview-page">
      <PageHeading
        title="Project Collab 디자인 시스템"
        description="색상·글자·간격과 공통 요소의 정적 기준입니다."
        action={<ViewLink screen="gallery">화면 모음</ViewLink>}
      />
      <section className="specimen">
        <h2>색상</h2>
        <div className="swatches">
          {[
            ["primary", "주요 행동", "#245C45"],
            ["canvas", "화면 배경", "#F7F8F7"],
            ["surface", "본문 배경", "#FFFFFF"],
            ["text", "기본 글자", "#202622"],
            ["muted", "보조 글자", "#626B65"],
            ["border", "구분선", "#DDE2DE"],
          ].map(([name, label, value]) => (
            <div key={name}>
              <span className={`swatch swatch--${name}`} />
              <strong>{label}</strong>
              <code>{value}</code>
            </div>
          ))}
        </div>
      </section>
      <section className="specimen">
        <h2>글자와 간격</h2>
        <p className="type-title">작업을 확인하고 다음 단계를 결정합니다</p>
        <p>본문 14px / 줄 높이 1.6 · 제목 26px · 보조 글자 12px</p>
        <p className="muted">
          기본 간격 4 / 8 / 12 / 16 / 24 / 32px · 모서리 4px · 표 행 최소 56px
        </p>
      </section>
      <section className="specimen">
        <h2>버튼</h2>
        <div className="inline-group">
          <Button tone="primary">주요 행동</Button>
          <Button>보조 행동</Button>
          <Button tone="quiet">텍스트 행동</Button>
          <Button tone="danger">삭제</Button>
          <Button disabled>실행 불가</Button>
        </div>
      </section>
      <section className="specimen">
        <h2>작업 상태</h2>
        <div className="inline-group">
          {(
            [
              "PENDING",
              "REJECTED",
              "ACCEPTED",
              "IN_PROGRESS",
              "IN_REVIEW",
              "DONE",
            ] as TaskState[]
          ).map((state) => (
            <StatusBadge key={state} state={state} />
          ))}
        </div>
      </section>
      <section className="specimen">
        <h2>입력과 안내</h2>
        <div className="specimen-grid">
          <Field
            id="sample-input"
            label="작업 제목"
            value="결제 오류 재현"
            required
          />
          <Field
            id="sample-error"
            label="반려 사유"
            error="반려 사유를 입력해 주세요."
            required
          />
        </div>
        <Notice title="최신 내용을 확인해 주세요" tone="warning">
          다른 사용자가 작업을 변경했습니다. 최신 내용을 확인한 뒤 다시 진행해
          주세요.
        </Notice>
      </section>
    </main>
  );
}
