package ren.imyan.sniper.ui.icons

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.lollipop.iconcore.ui.IconHelper
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.pxToDp
import ren.imyan.sniper.databinding.FragmentIconsBinding


class IconsFragment : BaseFragment(R.layout.fragment_icons) {

    private val binding by binding(FragmentIconsBinding::bind)

    override fun loadSingleData() {
        super.loadSingleData()
        context?.let {
            val xmlMap = IconHelper.DefaultXmlMap.readFromResource(it, R.xml.drawable)
            val categoryList = mutableListOf<String>()
            val iconMap = mutableMapOf<String, Array<IconHelper.IconInfo>>()

            for (i in 0 until xmlMap.categoryCount) {
                categoryList.add(xmlMap.getCategory(i))

                val cate = xmlMap.getCategory(i)
                val iconCount = xmlMap.iconCountByCategory(cate)
                val iconList = mutableListOf<IconHelper.IconInfo>()

                for (j in 0 until iconCount) {
                    iconList.add(xmlMap.getIcon(cate, j))
                }
                iconMap[cate] = iconList.toTypedArray()
            }

            initTab(categoryList)
            try {
                initIconView(iconMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initTab(categoryList: List<String>) {
        categoryList.forEachIndexed { index, cate ->
            binding?.apply {
                val tabItem = this.tablayout.newTab().apply {
                    this.text = cate
                }
                tablayout.addTab(tabItem, index)

            }
        }
    }

    private fun initIconView(iconMap: Map<String, Array<IconHelper.IconInfo>>) {
        binding?.pager?.adapter = IconPagerAdapter(iconMap)
        binding?.apply {
            TabLayoutMediator(
                tablayout,
                pager
            ) { tab, position ->
                tab.text = iconMap.keys.toTypedArray()[position]
            }.attach()
        }
    }

    inner class IconPagerAdapter(private val iconMap: Map<String, Array<IconHelper.IconInfo>>) :
        RecyclerView.Adapter<IconPagerAdapter.ViewHolder>() {

        inner class ViewHolder(view: RecyclerView) : RecyclerView.ViewHolder(view) {
            val list = view
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = RecyclerView(parent.context).apply {
                layoutParams = parent.layoutParams
                addItemDecoration(MarginItemDecoration(16.pxToDp(parent.context)))
            }
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = iconMap.keys.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            iconMap[iconMap.keys.toTypedArray()[position]]?.let {
                holder.list.apply {
                    layoutManager = GridLayoutManager(requireContext(), 4)
                    adapter = IconListAdapter(it)
                }
            }
        }

    }

    inner class MarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect[margin, margin, margin] = margin
        }
    }

    inner class IconListAdapter(private val data: Array<IconHelper.IconInfo>) :
        RecyclerView.Adapter<IconListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: ImageView) : RecyclerView.ViewHolder(view)


        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): IconListAdapter.ViewHolder {
            val view = ImageView(parent.context).apply {
                setPadding(16.pxToDp(parent.context))
            }
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: IconListAdapter.ViewHolder, position: Int) {
            holder.view.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    data[position].resId
                )
            )
        }

    }
}