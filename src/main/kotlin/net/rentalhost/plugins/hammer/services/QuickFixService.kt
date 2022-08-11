package net.rentalhost.plugins.hammer.services

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.jetbrains.php.lang.psi.elements.PhpTypeDeclaration
import net.rentalhost.plugins.hammer.extensions.psi.replaceWith

@Suppress("PublicApiImplicitType")
abstract class QuickFixService(private val projectService: ProjectService) {
    abstract class SimpleQuickFix constructor(private val quickFixTitle: String): LocalQuickFix {
        override fun getFamilyName(): String = "\uD83D\uDD28 $quickFixTitle"
    }

    fun simpleTypeReplace(
        quickFixTitle: String,
        entireTypesReplacement: String,
        considerParent: Boolean = false
    ) = object: SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            projectService.settings.increaseFixes()

            val phpTypeDeclaration =
                if (considerParent) descriptor.psiElement.parent
                else descriptor.psiElement

            if (phpTypeDeclaration is PhpTypeDeclaration) {
                phpTypeDeclaration.replaceWith(project, entireTypesReplacement)
            }
        }
    }

    fun simpleLeafReplace(
        quickFixTitle: String,
        leafReplacement: PsiElement
    ) = object: SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            projectService.settings.increaseFixes()

            descriptor.psiElement.replace(leafReplacement)
        }
    }

    fun simpleDelete(
        quickFixTitle: String,
        element: SmartPsiElementPointer<PsiElement>? = null
    ) = object: SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            projectService.settings.increaseFixes()

            (element?.element ?: descriptor.psiElement).delete()
        }
    }

    fun simpleReplace(
        quickFixTitle: String,
        replaceFrom: PsiElement? = null,
        replaceTo: PsiElement
    ) = object: SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            projectService.settings.increaseFixes()

            (replaceFrom ?: descriptor.psiElement).replace(replaceTo)
        }
    }

    fun simpleReplace(quickFixTitle: String, replaceTo: PsiElement) =
        simpleReplace(quickFixTitle, null, replaceTo)

    fun simpleInline(
        quickFixTitle: String,
        applyFix: () -> Unit
    ) = object: SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            projectService.settings.increaseFixes()

            applyFix.invoke()
        }
    }
}
