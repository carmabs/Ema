package ${packageName}

import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.state.EmaExtraData

import org.kodein.di.DI
import org.kodein.di.instance

class ${functionalityName}Fragment : EmaFragment<${functionalityName}State, ${functionalityName}ViewModel, <#if navigator?has_content>${navigator}<#else>${functionalityName}Navigator</#if>.Navigation>() {

    override fun injectFragmentModule(kodein: DI.MainBuilder): DI.Module? = null

    override val fragmentViewModelScope: Boolean = true

    override val viewModelSeed: ${functionalityName}ViewModel by instance()

    override val navigator: EmaNavigator<<#if navigator?has_content>${navigator}<#else>${functionalityName}Navigator</#if>.Navigation> by instance<<#if navigator?has_content>${navigator}<#else>${functionalityName}Navigator</#if>>()

    override fun onInitialized(viewModel: ${functionalityName}ViewModel) {

    }

    override fun onStateNormal(data: ${functionalityName}State) {

    }

    override fun onStateAlternative(data: EmaExtraData) {

    }

    override fun onSingleEvent(data: EmaExtraData) {

    }

    override fun onStateError(error: Throwable) {

    }

    override val layoutId: Int = R.layout.fragmentID

}