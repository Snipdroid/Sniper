package ren.imyan.sniper.ui.icons

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.lollipop.iconcore.ui.IconHelper
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.base.BaseLoad
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.observeState
import ren.imyan.sniper.common.pxToDp
import ren.imyan.sniper.databinding.FragmentIconsBinding
import ren.imyan.sniper.databinding.IconPreviewBinding
import ren.imyan.sniper.ui.IconPackData
import ren.imyan.sniper.ui.IconPackViewModel


class IconsFragment : BaseFragment(R.layout.fragment_icons) {

    private val binding by binding(FragmentIconsBinding::bind)
    private val viewModel by activityViewModels<IconPackViewModel>()

    override fun initViewModel(viewLifecycleOwner: LifecycleOwner) {
        super.initViewModel(viewLifecycleOwner)
        viewModel.uiData.observeState(viewLifecycleOwner, IconPackData::iconMap) {
            when (it) {
                is BaseLoad.Success -> {
                    initTab(it.data.keys.toList())
                    initIconView(it.data)
                }

                else -> {}
            }
        }
    }

    private fun initTab(categoryList: List<String>) {
        binding?.tablayout?.removeAllTabs()
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
                tablayout, pager
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
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect[margin, margin, margin] = margin
        }
    }

    inner class IconListAdapter(private val data: Array<IconHelper.IconInfo>) :
        RecyclerView.Adapter<IconListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: ImageView) : RecyclerView.ViewHolder(view)


        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): IconListAdapter.ViewHolder {
            val view = ImageView(parent.context).apply {
                setPadding(16.pxToDp(parent.context))
            }
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: IconListAdapter.ViewHolder, position: Int) {
            try {
                holder.view.load(data[position].resId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.view.setOnClickListener {
                IconPreviewBottomSheet(
                    data[position].resId,
                    data[position].iconName
                ).show(
                    requireActivity().supportFragmentManager, IconPreviewBottomSheet.TAG
                )
            }
        }

    }
}

class IconPreviewBottomSheet(@DrawableRes private val icon: Int, private val appName: String) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.icon_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = IconPreviewBinding.bind(view)

        binding.icon.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), icon
            )
        )

        binding.appName.text = appName
    }

    companion object {
        const val TAG = "IconPreviewBottomSheet"
    }
}