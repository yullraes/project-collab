import "./Field.css";

export interface SelectOption {
  value: string;
  label: string;
}
export interface SelectFieldProps {
  id: string;
  label: string;
  options: readonly SelectOption[];
  value?: string;
  hint?: string;
  disabled?: boolean;
}
export function SelectField({
  id,
  label,
  options,
  value,
  hint,
  disabled,
}: SelectFieldProps) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <select
        id={id}
        defaultValue={value}
        disabled={disabled}
        aria-describedby={hint ? `${id}-help` : undefined}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {hint && (
        <p className="field-hint" id={`${id}-help`}>
          {hint}
        </p>
      )}
    </div>
  );
}
