package net.rentalhost.plugins.hammer.services

abstract class ProjectService {
    abstract val id: String

    abstract val name: String

    abstract val sentryDsn: String

    abstract val notificationGroup: String

    abstract val urls: UrlService

    abstract val settings: SettingsService
}
