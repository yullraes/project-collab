import "./ProjectSummary.css";

export interface ProjectSummaryProps {
  name: string;
  description: string;
  dates?: readonly { label: string; value: string }[];
}
export function ProjectSummary({
  name,
  description,
  dates,
}: ProjectSummaryProps) {
  return (
    <section className="project-summary">
      <div className="project-summary__heading">
        <h2>{name}</h2>
        {dates && (
          <dl className="project-summary__dates">
            {dates.map((date) => (
              <div key={date.label}>
                <dt>{date.label}</dt>
                <dd>{date.value}</dd>
              </div>
            ))}
          </dl>
        )}
      </div>
      <p>{description}</p>
    </section>
  );
}
