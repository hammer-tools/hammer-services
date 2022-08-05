package net.rentalhost.plugins.hammer.services

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.Notification
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import net.rentalhost.plugins.hammer.services.NotificationService.NotificationItem
import java.time.ZonedDateTime

abstract class PluginUpdateService(val projectService: ProjectService): ProjectManagerListener {
    private val plugin: IdeaPluginDescriptor = PluginManagerCore.getPlugin(PluginId.findId("net.rentalhost.plugins.php.hammer"))!!

    private val tripleHome = NotificationItem("home", "project home", "home", projectService.urls.homeUrl)
    private val tripleChangelog = NotificationItem("changelog", "changelog", projectService.urls.changelogUrl)
    private val tripleFreemium = NotificationItem("freemium", "freemium", projectService.urls.freemiumUrl)
    private val tripleInspections = NotificationItem("inspections", "inspections", projectService.urls.inspectionsUrl)

    private val tripleReviewNow = NotificationItem("review", "review", projectService.urls.reviewsUrl) { notification -> closeReview(notification) }
    private val tripleReviewLater = NotificationItem("Remember later") { notification -> closeReview(notification, true) }
    private val tripleReviewNever = NotificationItem("Never ask again") { notification -> closeReview(notification) }
    private val tripleReviewHome = tripleHome.withoutActionButton()

    private fun closeReview(notification: Notification, disableReview: Boolean = false) {
        notification.expire()

        if (disableReview) {
            projectService.settings.reviewDisable()
        }
    }

    override fun projectOpened(project: Project) {
        with(projectService.settings.getServiceInstance().state) {
            countProjects++

            if (!pluginFreshInstalled) {
                pluginFreshInstalled = true

                notifyInstall()
            }
            else if (pluginVersion != plugin.version) {
                notifyUpdate(pluginVersion, plugin.version)
            }
            else if (projectService.settings.isReviewTime()) {
                notifyReview()
            }

            pluginVersion = plugin.version

            if (installedAt == null) {
                val date = ZonedDateTime.now()

                installedAt = date.toEpochSecond()
                reviewAfter = date.plusDays(30).toEpochSecond()
            }
        }
    }

    private fun notifyInstall() {
        NotificationService.notify(
            "net.rentalhost.plugins.notification.INSTALLED",
            ResourceService.read("/plugin/welcome.html"),
            listOf(tripleHome, tripleInspections, tripleFreemium)
        )
    }

    private fun notifyUpdate(versionBefore: String?, versionAfter: String) = NotificationService.notify(
        "net.rentalhost.plugins.notification.UPDATED",
        ResourceService.read("/plugin/news.html")
            .replace("\$beforeNote", if (versionBefore != null) ", replacing the previously installed version (was $versionBefore)" else "")
            .replace("\$pluginVersion", versionAfter),
        listOf(tripleChangelog, tripleFreemium)
    )

    private fun notifyReview() {
        projectService.settings.reviewRememberLater()

        NotificationService.notify(
            "net.rentalhost.plugins.notification.REVIEW",
            ResourceService.read("/plugin/review.html"),
            listOf(tripleReviewNow, tripleReviewLater, tripleReviewNever, tripleReviewHome)
        )
    }
}
