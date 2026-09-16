import "./RadioOption.css";

export interface RadioOptionProps {
  name: string;
  value: string;
  title: string;
  detail?: string;
  checked?: boolean;
  disabled?: boolean;
}
export function RadioOption({
  name,
  value,
  title,
  detail,
  checked,
  disabled,
}: RadioOptionProps) {
  return (
    <label className={`radio-option ${disabled ? "is-disabled" : ""}`}>
      <input
        type="radio"
        name={name}
        value={value}
        defaultChecked={checked}
        disabled={disabled}
      />
      <span>
        <strong>{title}</strong>
        {detail && <small>{detail}</small>}
      </span>
    </label>
  );
}
