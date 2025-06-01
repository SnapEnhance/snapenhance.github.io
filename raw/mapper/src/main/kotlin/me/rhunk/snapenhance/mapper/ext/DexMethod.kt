package me.rhunk.snapenhance.mapper.ext

import com.android.tools.smali.dexlib2.iface.MethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

fun MethodImplementation.findConstString(
    string: String,
    contains: Boolean = false,
    startsWith: Boolean = false,
    ignoreCase: Boolean = false
): Boolean = instructions.filterIsInstance<Instruction21c>().any {
     (it.reference as? StringReference)?.string?.let { str ->
        if (contains && str.contains(string, ignoreCase = ignoreCase)) return@any true
        if (startsWith && str.startsWith(string, ignoreCase = ignoreCase)) return@any true
        str == string
    } == true
}

fun MethodImplementation.getAllConstStrings(): List<String> = instructions.filterIsInstance<Instruction21c>().mapNotNull {
    it.reference as? StringReference
}.map {
    it.string
}

fun MethodImplementation.searchNextFieldReference(constString: String, contains: Boolean = false): FieldReference? = this.instructions.let {
    var found = false
    for (instruction in it) {
        if (instruction is Instruction21c && instruction.reference is StringReference) {
            val str = (instruction.reference as StringReference).string
            if (if (contains) str.contains(constString) else str == constString) {
                found = true
            }
        }

        if (!found) continue

        if (instruction is Instruction22c &&
            instruction.reference is FieldReference
        ) {
            return@let (instruction.reference as FieldReference)
        }
    }
    null
}
