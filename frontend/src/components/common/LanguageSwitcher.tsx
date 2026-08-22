import { useTranslation } from 'react-i18next'

const LANGUAGES = ['fr', 'en'] as const

export function LanguageSwitcher() {
  const { i18n } = useTranslation()
  const current = LANGUAGES.includes(i18n.language as (typeof LANGUAGES)[number])
    ? i18n.language
    : 'fr'

  return (
    <select
      className="select select-ghost select-sm w-auto"
      value={current}
      onChange={(event) => void i18n.changeLanguage(event.target.value)}
      aria-label="Language"
    >
      {LANGUAGES.map((lng) => (
        <option key={lng} value={lng}>
          {lng.toUpperCase()}
        </option>
      ))}
    </select>
  )
}
