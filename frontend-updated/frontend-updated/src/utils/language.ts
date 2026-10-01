import { Language } from "../types/product";

export const ENGLISH_LANGUAGE_NAME = "English";

/**
 * Finds the "English" row from the reference-data Language list. Used to
 * always request the English ProductTranslation overlay for catalog display
 * now that the user-facing language dropdown has been removed (see Navbar) -
 * English display is a fixed site-wide behavior, not a per-user preference,
 * per the "English titles displayed wherever required" requirement.
 */
export const findEnglishLanguageId = (languages: Language[] | undefined | null): number | undefined =>
  languages?.find((l) => l.languageName.trim().toLowerCase() === ENGLISH_LANGUAGE_NAME.toLowerCase())?.languageId;

export const isEnglishLanguageName = (languageName: string | undefined | null): boolean =>
  (languageName || "").trim().toLowerCase() === ENGLISH_LANGUAGE_NAME.toLowerCase();
