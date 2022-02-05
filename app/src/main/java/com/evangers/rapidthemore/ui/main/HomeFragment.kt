package com.evangers.rapidthemore.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.viewModels
import com.evangers.rapidthemore.R
import com.evangers.rapidthemore.databinding.FragmentHomeBinding
import com.evangers.rapidthemore.ui.base.ParentFragment
import com.evangers.rapidthemore.ui.util.longToast
import com.evangers.rapidthemore.ui.util.shortToast
import com.google.android.gms.ads.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class HomeFragment : ParentFragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var binding: FragmentHomeBinding

    private lateinit var adView: AdView
    private var initialLayoutComplete = false
    private val adSize: AdSize
        get() {
            val display = requireActivity().windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireContext(),
                adWidth
            )
        }

    override fun bindView(view: View) {
        binding = FragmentHomeBinding.bind(view)
    }

    override fun initUi() {
        initialLayoutComplete = false
        with(binding) {
            paycoButton.setOnClickListener { viewModel.launchPayco() }
            launchSPay.setOnClickListener { viewModel.launchSpay() }
            with(numberButtons) {
                button0.setOnClickListener { viewModel.addDigitLast(0) }
                button1.setOnClickListener { viewModel.addDigitLast(1) }
                button2.setOnClickListener { viewModel.addDigitLast(2) }
                button3.setOnClickListener { viewModel.addDigitLast(3) }
                button4.setOnClickListener { viewModel.addDigitLast(4) }
                button5.setOnClickListener { viewModel.addDigitLast(5) }
                button6.setOnClickListener { viewModel.addDigitLast(6) }
                button7.setOnClickListener { viewModel.addDigitLast(7) }
                button8.setOnClickListener { viewModel.addDigitLast(8) }
                button9.setOnClickListener { viewModel.addDigitLast(9) }
                buttonClear.setOnClickListener { viewModel.clearNumber() }
                buttonRemove.setOnClickListener { viewModel.removeDigitLast() }
            }
            adView = AdView(requireContext())
            adViewContainer.addView(adView)
            adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    loadBanner()
                }
            }
            bannerWebView.settings.javaScriptEnabled = true
            val script = """
                <script src ="https://ads-partners.coupang.com/g.js"></script>
                <script>
                new PartnersCoupang.G({
                    ${getString(R.string.coupang_json)}
                });</script>
            """.trimIndent()
            val html = "<div>$script</div>"
            bannerWebView.loadData(html, "text/html", "utf-8")
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return requireContext().packageManager.getLaunchIntentForPackage(packageName) != null
    }

    override fun initBinding() {
        viewModel.liveData.observe(viewLifecycleOwner, { state ->
            state.amount.getValueIfNotHandled()?.let {
                val formatter = DecimalFormat("#,###.##")
                val result = formatter.format(it)
                binding.numberTextView.text = result
            }
            state.ratio.getValueIfNotHandled()?.let {
                binding.resultTextView.text = getString(R.string.percent_with_number, it.toString())
            }
            state.toastMessage?.getValueIfNotHandled()?.let {
                requireContext().shortToast(it)
            }
            state.longToastMessage?.getValueIfNotHandled()?.let {
                requireContext().longToast(it)
            }
            state.launchPayco?.getValueIfNotHandled()?.let {
                launchPayco()
            }
            state.launchSpay?.getValueIfNotHandled()?.let {
                launchSpay()
            }
        })
    }

    override fun onViewCreatedSg(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedSg(view, savedInstanceState)
        viewModel.start()
        initUi()
        initBinding()
    }


    private fun launchPayco() {
        val packageName = getString(R.string.payco_package)
        if (isAppInstalled(packageName)) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.payco_launcher)))
            viewModel.showPercentToast()
            requireContext().startActivity(intent)
        } else {
            viewModel.showToast(getString(R.string.no_payco_app))
        }
    }

    private fun launchSpay() {
        val packageName = getString(R.string.spay_package)
        if (isAppInstalled(packageName)) {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            viewModel.showPercentToast()
            requireContext().startActivity(intent)
        } else {
            viewModel.showToast(getString(R.string.no_spay_app))
        }
    }

    private fun loadBanner() {
        adView.adUnitId = getString(R.string.admob_home_banner_unit_id)
        adView.adSize = adSize
        val adRequest = AdRequest
            .Builder()
            .build()
        adView.loadAd(adRequest)
    }

}