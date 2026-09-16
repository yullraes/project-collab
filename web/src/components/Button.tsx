import type { AnchorHTMLAttributes, ButtonHTMLAttributes } from "react";
import "./Button.css";

export type Tone = "primary" | "secondary" | "danger" | "quiet";
export function Button({
  tone = "secondary",
  className = "",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { tone?: Tone }) {
  return (
    <button
      type="button"
      className={`button button--${tone} ${className}`}
      {...props}
    />
  );
}
export function ActionLink({
  tone = "secondary",
  className = "",
  ...props
}: AnchorHTMLAttributes<HTMLAnchorElement> & { tone?: Tone }) {
  return <a className={`button button--${tone} ${className}`} {...props} />;
}
