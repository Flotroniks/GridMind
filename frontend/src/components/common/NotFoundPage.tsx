import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

export function NotFoundPage() {
  const { t } = useTranslation()

  return (
    <div className="mx-auto flex w-full max-w-md flex-col items-center gap-4 text-center">
      <h1 className="text-3xl font-bold">{t('notFoundPage.title')}</h1>
      <p className="text-base-content/70">{t('notFoundPage.message')}</p>
      <Link to="/" className="btn btn-primary">
        {t('notFoundPage.backHome')}
      </Link>
    </div>
  )
}
