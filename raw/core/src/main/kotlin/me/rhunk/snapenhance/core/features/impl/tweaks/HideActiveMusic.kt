package me.rhunk.snapenhance.core.features.impl.tweaks

import android.media.AudioManager
import me.rhunk.snapenhance.core.features.Feature
import me.rhunk.snapenhance.core.features.FeatureLoadParams
import me.rhunk.snapenhance.core.util.hook.HookStage
import me.rhunk.snapenhance.core.util.hook.hook

class HideActiveMusic: Feature("Hide Active Music", loadParams = FeatureLoadParams.ACTIVITY_CREATE_SYNC) {
    override fun onActivityCreate() {
        if (!context.config.global.hideActiveMusic.get()) return
        AudioManager::class.java.hook("isMusicActive", HookStage.BEFORE) {
            it.setResult(false)
        }
    }
}