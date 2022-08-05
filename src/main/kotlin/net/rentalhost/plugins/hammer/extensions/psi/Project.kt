package net.rentalhost.plugins.hammer.extensions.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

fun Project.searchScope(): GlobalSearchScope =
    GlobalSearchScope.projectScope(this)
