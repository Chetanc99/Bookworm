import React from "react";

export interface CardProps {
  children: React.ReactNode;
  className?: string;
  hoverEffect?: boolean;
  glassmorphism?: boolean;
  onClick?: () => void;
}

export const Card: React.FC<CardProps> = ({
  children,
  className = "",
  hoverEffect = true,
  glassmorphism = false,
  onClick,
}) => {
  return (
    <div
      onClick={onClick}
      className={`rounded-2xl border p-6 transition-all duration-300 ${
        glassmorphism
          ? "bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-white/20 dark:border-slate-800/80 shadow-xl"
          : "bg-white dark:bg-slate-900 border-slate-100 dark:border-slate-800/80 shadow-sm"
      } ${
        hoverEffect
          ? "hover:shadow-xl hover:-translate-y-1 hover:border-amber-500/30"
          : ""
      } ${onClick ? "cursor-pointer" : ""} ${className}`}
    >
      {children}
    </div>
  );
};
