package net.rentalhost.plugins.hammer.extensions.psi

import com.jetbrains.php.PhpClassHierarchyUtils
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpClassMember

private fun containsName(baseClass: PhpClass, name: String, subject: (PhpClass) -> Array<String>): Boolean {
    var found = false

    PhpClassHierarchyUtils.processSupers(baseClass, true, true) { phpClass ->
        subject.invoke(phpClass).forEach { subjectName ->
            if (subjectName == name) {
                found = true
                return@processSupers false
            }
        }

        return@processSupers true
    }

    return found
}

fun PhpClass.hasTrait(name: String): Boolean =
    containsName(this, name) { phpClass -> phpClass.traitNames }

fun PhpClass.hasInterface(name: String): Boolean =
    containsName(this, name) { phpClass -> phpClass.interfaceNames }

fun PhpClassMember.isMemberOverridden(): Boolean {
    var isOverridden = false

    PhpClassHierarchyUtils.processSuperMembers(this) { _, _, _ ->
        isOverridden = true
        false
    }

    return isOverridden
}

fun PhpClassMember.getMemberOverridden(): PhpClass? {
    var element: PhpClass? = null

    PhpClassHierarchyUtils.processSuperMembers(this) { _, _, phpClass ->
        element = phpClass
        false
    }

    return element
}

fun PhpClassMember.isMemberOverrided(): Boolean {
    var isOverrided = false

    PhpClassHierarchyUtils.processOverridingMembers(this) { _, _, _ ->
        isOverrided = true
        false
    }
    return isOverrided
}

fun PhpClassMember.isDefinedByOwnClass(): Boolean =
    !isMemberOverridden()
