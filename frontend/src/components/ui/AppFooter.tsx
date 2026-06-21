import { useTranslation } from 'react-i18next'

export function AppFooter() {
  const { t } = useTranslation()
  const year = new Date().getFullYear()

  return (
    <footer className="mx-auto mt-auto max-w-6xl pb-6 pt-16 text-center sm:pt-24">
      <p className="text-xs leading-6 text-secondary sm:text-sm">
        {t('footer.line', { year })}
      </p>
    </footer>
  )
}
