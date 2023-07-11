package kr.evangers.rapidthemore.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class ParentFragment constructor(
    @LayoutRes layoutRes: Int,
) : Fragment(layoutRes) {

    @Deprecated(
        message = "필요시 onPostCreateView() override할 것",
        level = DeprecationLevel.ERROR
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            onPostCreateView(savedInstanceState)
        }
    }

    @Deprecated(
        message = "onViewCreatedSg() override 할 것",
        level = DeprecationLevel.ERROR
    )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)
        onViewCreatedSg(view, savedInstanceState)
    }

    @Deprecated(
        message = "onDestroyViewSg() override 할 것",
        level = DeprecationLevel.ERROR
    )
    override fun onDestroyView() {
        onDestroyViewSg()
        super.onDestroyView()
    }

    protected open fun onViewCreatedSg(view: View, savedInstanceState: Bundle?) = Unit
    protected open fun onPostCreateView(savedInstanceState: Bundle?) = Unit
    protected open fun onDestroyViewSg() = Unit
    abstract fun bindView(view: View)
    abstract fun initUi()
    abstract fun initBinding()

}