import { RadioOption, type RadioOptionProps } from "./RadioOption";

export interface ChoiceGroupProps {
  legend: string;
  name: string;
  hiddenLegend?: boolean;
  options: readonly (Omit<RadioOptionProps, "name"> & { id: string })[];
}
export function ChoiceGroup({
  legend,
  name,
  hiddenLegend,
  options,
}: ChoiceGroupProps) {
  return (
    <fieldset>
      <legend className={hiddenLegend ? "sr-only" : undefined}>{legend}</legend>
      {options.map(({ id, ...option }) => (
        <RadioOption key={id} name={name} {...option} />
      ))}
    </fieldset>
  );
}
