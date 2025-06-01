package me.rhunk.snapenhance.mapper.impl

import me.rhunk.snapenhance.mapper.AbstractClassMapper
import me.rhunk.snapenhance.mapper.ext.findConstString
import me.rhunk.snapenhance.mapper.ext.getClassName

class ScoreUpdateMapper : AbstractClassMapper("ScoreUpdate") {
    val classReference = classReference("class")

    init {
        mapper {
            for (classDef in classes) {
                val toStringMethod = classDef.methods.firstOrNull {
                    it.name == "toString"
                } ?: continue
                if (classDef.methods.none {
                    it.name == "<init>" &&
                    it.parameterTypes.size > 4
                }) continue

                if (toStringMethod.implementation?.findConstString("selectFriendUserScoresNeedToUpdate", contains = true) != true) continue

                classReference.set(classDef.getClassName())
                return@mapper
            }
        }
    }
}