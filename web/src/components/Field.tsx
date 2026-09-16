import "./Field.css";

export interface FieldProps {
  id: string;
  label: string;
  value?: string;
  placeholder?: string;
  hint?: string;
  error?: string;
  required?: boolean;
  multiline?: boolean;
  disabled?: boolean;
  maxLength?: number;
}
export function Field({
  id,
  label,
  value = "",
  placeholder,
  hint,
  error,
  required = false,
  multiline = false,
  disabled,
  maxLength,
}: FieldProps) {
  const props = {
    id,
    value,
    placeholder,
    readOnly: true,
    required,
    disabled,
    maxLength,
    "aria-invalid": Boolean(error),
    "aria-describedby": error || hint ? `${id}-help` : undefined,
  };
  return (
    <div className="field">
      <label htmlFor={id}>
        {label}
        {required && <span className="required"> *</span>}
      </label>
      {multiline ? <textarea {...props} rows={5} /> : <input {...props} />}
      {(error || hint) && (
        <p id={`${id}-help`} className={error ? "field-error" : "field-hint"}>
          {error || hint}
        </p>
      )}
    </div>
  );
}
