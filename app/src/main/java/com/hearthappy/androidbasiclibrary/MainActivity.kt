package com.hearthappy.androidbasiclibrary

import android.graphics.Color
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.hearthappy.androidbasiclibrary.databinding.ActivityMainBinding
import com.hearthappy.androidbasiclibrary.example1.Example1Activity
import com.hearthappy.androidbasiclibrary.example2.Example2Activity
import com.hearthappy.basic.AbsBaseActivity
import com.hearthappy.basic.ext.findViewCoordinates
import com.hearthappy.basic.ext.getInfiniteLoopRealPosition
import com.hearthappy.basic.ext.scrollAnimator
import com.hearthappy.basic.ext.setupInfiniteLoop

class MainActivity: AbsBaseActivity<ActivityMainBinding>() {
    private val bannerAdapter by lazy { MainBannerAdapter() }
    private val bannerItems = listOf(MainBannerItem("Spring Sale", "Duplicate edge items keep ViewPager2 scrolling seamless.", Color.parseColor("#5B8FF9")), MainBannerItem("Hot Topics", "AbsSpecialAdapter still binds normal items while the pager loops.", Color.parseColor("#5AD8A6")), MainBannerItem("New Arrival", "Real positions stay stable for indicators, clicks, and analytics.", Color.parseColor("#5D7092")), MainBannerItem("Flash Event", "When the fake edge is selected, idle state jumps back silently.", Color.parseColor("#F6BD16")), MainBannerItem("Member Day", "Auto scroll keeps moving and wraps back to the first real page.", Color.parseColor("#E8684A")))

    private var bannerCallback: ViewPager2.OnPageChangeCallback? = null
    private val bannerAutoScrollTask = object: Runnable {
        override fun run() {
            if (bannerItems.size <= 1) return
            viewBinding.vpBanner.scrollAnimator(viewBinding.vpBanner.currentItem + 1, 1000)
            viewBinding.vpBanner.postDelayed(this, BANNER_AUTO_SCROLL_DELAY)
        }
    }

    override fun ActivityMainBinding.initData() {
        bindInfiniteBanner()
    }

    override fun ActivityMainBinding.initListener() {
        btnExample1.setOnClickListener {
            startActivity(Example1Activity::class.java)
        }
        btnExample2.setOnClickListener {
            val coordinates = it.findViewCoordinates()
            startActivityCarryCoordinates(Example2Activity::class.java, coordinates)
        }
        bannerAdapter.setOnItemClickListener { _, _, position, _ ->
            val realPosition = getInfiniteLoopRealPosition(position, bannerItems.size)
            Toast.makeText(this@MainActivity, "Clicked banner ${realPosition + 1}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun ActivityMainBinding.initViewModelListener() {

    }

    override fun ActivityMainBinding.initView() {
        vpBanner.adapter = bannerAdapter
        vpBanner.offscreenPageLimit = 3

    }

    override fun onResume() {
        super.onResume()
        startBannerAutoScroll()
    }

    override fun onPause() {
        stopBannerAutoScroll()
        super.onPause()
    }

    override fun onDestroy() {
        stopBannerAutoScroll()
        bannerCallback?.let { viewBinding.vpBanner.unregisterOnPageChangeCallback(it) }
        bannerCallback = null
        super.onDestroy()
    }

    private fun bindInfiniteBanner() {
        bannerCallback?.let { viewBinding.vpBanner.unregisterOnPageChangeCallback(it) }
        bannerCallback = viewBinding.vpBanner.setupInfiniteLoop(adapter = bannerAdapter, data = bannerItems) { realPosition ->
            updateBannerIndicator(realPosition)
        }
        updateBannerIndicator(0)
        startBannerAutoScroll()
    }

    private fun updateBannerIndicator(realPosition: Int) {
        if (bannerItems.isEmpty()) {
            viewBinding.tvBannerIndicator.text = "0 / 0"
            return
        }
        val safePosition = realPosition.coerceIn(0, bannerItems.lastIndex)
        viewBinding.tvBannerIndicator.text = "${safePosition + 1} / ${bannerItems.size}"
    }

    private fun startBannerAutoScroll() {
        stopBannerAutoScroll()
        if (bannerItems.size <= 1) return
        viewBinding.vpBanner.postDelayed(bannerAutoScrollTask, BANNER_AUTO_SCROLL_DELAY)
    }

    private fun stopBannerAutoScroll() {
        viewBinding.vpBanner.removeCallbacks(bannerAutoScrollTask)
    }

    companion object {
        private const val BANNER_AUTO_SCROLL_DELAY = 3000L
    }
}
