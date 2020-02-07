package ${packageName}

import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.core.state.EmaExtraData

import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class ${functionalityName}Activity : EmaActivity<${functionalityName}State, ${functionalityName}ViewModel, <#if navigator?has_content>${navigator}<#else>${functionalityName}Navigator</#if>.Navigation>() {

	override fun injectActivityModule(kodein: Kodein.MainBuilder): Kodein.Module? = null

	override fun provideFixedToolbarTitle(): String? = null

    override fun onInitialized(viewModel: ${functionalityName}ViewModel) {

    }

    override val viewModelSeed: ${functionalityName}ViewModel by instance()

    override val navigator: EmaNavigator<<#if navigator?has_content>${navigator}<#else>${functionalityName}Navigator</#if>.Navigation> by instance<<#if navigator?has_content>${navigator}<#else>${functionalityName}Navigator</#if>>()

    override fun onStateNormal(data: ${functionalityName}State) {

    }

    override fun onStateAlternative(data: EmaExtraData) {

    }

    override fun onStateError(error: Throwable) {

    }

    override fun onSingleEvent(data: EmaExtraData) {

    }

    override val navGraph : Int = R.navigation.graphID
}