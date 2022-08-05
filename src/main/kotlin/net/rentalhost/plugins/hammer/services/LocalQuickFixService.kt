package net.rentalhost.plugins.hammer.services

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.jetbrains.php.lang.psi.elements.PhpTypeDeclaration
import net.rentalhost.plugins.hammer.extensions.psi.replaceWith

object LocalQuickFixService {
    abstract class SimpleQuickFix constructor(
        private val quickFixTitle: String
    ): LocalQuickFix {
        override fun getFamilyName(): String = "\uD83D\uDD28 $quickFixTitle"
    }

    class SimpleTypeReplaceQuickFix(
        quickFixTitle: String,
        private val entireTypesReplacement: String,
        private val considerParent: Boolean = false
    ): SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            SettingsService.increaseFixes()

            val phpTypeDeclaration =
                if (considerParent) descriptor.psiElement.parent
                else descriptor.psiElement

            if (phpTypeDeclaration is PhpTypeDeclaration) {
                phpTypeDeclaration.replaceWith(project, entireTypesReplacement)
            }
        }
    }

    class SimpleDeleteQuickFix(quickFixTitle: String, val element: SmartPsiElementPointer<PsiElement>? = null): SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            SettingsService.increaseFixes()

            (element?.element ?: descriptor.psiElement).delete()
        }
    }

    class SimpleReplaceQuickFix: SimpleQuickFix {
        private var replaceFrom: SmartPsiElementPointer<PsiElement>? = null
        private val replaceTo: SmartPsiElementPointer<PsiElement>

        constructor(quickFixTitle: String, replaceTo: PsiElement): super(quickFixTitle) {
            this.replaceTo = SmartPointerManager.createPointer(replaceTo)
        }

        constructor(quickFixTitle: String, replaceTo: Lazy<PsiElement>): super(quickFixTitle) {
            this.replaceTo = SmartPointerManager.createPointer(replaceTo.value)
        }

        constructor(quickFixTitle: String, replaceFrom: PsiElement, replaceTo: PsiElement): super(quickFixTitle) {
            this.replaceFrom = SmartPointerManager.createPointer(replaceFrom)
            this.replaceTo = SmartPointerManager.createPointer(replaceTo)
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            SettingsService.increaseFixes()

            val replaceFrom =
                if (replaceFrom == null) descriptor.psiElement
                else ((replaceFrom as SmartPsiElementPointer).element ?: return)

            replaceFrom.replace(replaceTo.element ?: return)
        }
    }

    class SimpleInlineQuickFix constructor(
        quickFixTitle: String,
        private val applyFix: () -> Unit
    ): SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            SettingsService.increaseFixes()

            applyFix.invoke()
        }
    }

    class SimpleLeafReplaceQuickFix(
        quickFixTitle: String,
        private val leafReplacement: SmartPsiElementPointer<PsiElement>
    ): SimpleQuickFix(quickFixTitle) {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            SettingsService.increaseFixes()

            descriptor.psiElement.replace(leafReplacement.element ?: return)
        }
    }
}
