package ren.imyan.sniper.ui.request

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.base.BaseLoad
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.getThemeAttrColor
import ren.imyan.sniper.common.observeState
import ren.imyan.sniper.databinding.FragmentRequestBinding
import ren.imyan.sniper.databinding.ItemIconRequestBinding
import ren.imyan.sniper.model.AppInfo
import ren.imyan.sniper.ui.IconRequestAction
import ren.imyan.sniper.ui.IconRequestData
import ren.imyan.sniper.ui.IconRequestEvent
import ren.imyan.sniper.ui.IconRequestViewModel

class RequestFragment : BaseFragment(R.layout.fragment_request) {
    private val binding by binding(FragmentRequestBinding::bind)
    private val viewModel by activityViewModels<IconRequestViewModel>()
    private val requestListAdapter = RequestListAdapter(mutableListOf())
    private val appInfoList = mutableListOf<AppInfo>()
    private val requestDialog by lazy {
        RequestDialog(requireActivity())
    }

    override fun initView(root: View) {
        super.initView(root)
        binding?.requestList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = requestListAdapter
        }

        binding?.floatingActionButton?.setOnClickListener {
            viewModel.dispatch(IconRequestAction.RequestApp(appInfoList.filter { it.isCheck }))
            requestDialog.show()
        }
    }

    override fun initViewModel(viewLifecycleOwner: LifecycleOwner) {
        super.initViewModel(viewLifecycleOwner)
        viewModel.uiData.observeState(viewLifecycleOwner, IconRequestData::appInfoList) {
            when (it) {
                is BaseLoad.Loading -> {
                    binding?.apply {
                        loading.visibility = View.VISIBLE
                        floatingActionButton.visibility = View.GONE
                    }
                }

                is BaseLoad.Success -> {
                    binding?.apply {
                        loading.visibility = View.GONE
                        floatingActionButton.visibility = View.VISIBLE
                    }
                    requestListAdapter.updateData(it.data)
                    appInfoList.clear()
                    appInfoList.addAll(it.data)
                }

                else -> {

                }
            }
        }
        viewModel.uiEvent.onEach {
            when (it) {
                is IconRequestEvent.UpdateProgress -> {
                    requestDialog.updateProgress(it.progress, it.max)
                }

                IconRequestEvent.UploadFail -> {
                    requestDialog.dismiss()
                }

                IconRequestEvent.UploadFinish -> {
                    requestDialog.dismiss()
                    viewModel.dispatch(IconRequestAction.Refresh)
                }
            }
        }.launchIn(lifecycleScope)
    }

    inner class RequestListAdapter(private var data: List<AppInfo>) :
        RecyclerView.Adapter<RequestListAdapter.RequestViewHolder>() {
        inner class RequestViewHolder(val binding: ItemIconRequestBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
            val binding = ItemIconRequestBinding.inflate(layoutInflater, parent, false)
            return RequestViewHolder(binding)
        }

        override fun getItemCount(): Int = data.size ?: 0

        override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
            val dataItem = data[position]
            holder.binding.apply {
                icon.load(dataItem.icon)
                appName.text = dataItem.appName
                request.text =
                    if (dataItem.isRequest) resources.getString(R.string.request) else resources.getString(
                        R.string.not_request
                    )
                val color = if (dataItem.isRequest) context?.getThemeAttrColor(
                    R.style.Theme_Sniper, com.google.android.material.R.attr.colorError
                ) else context?.getThemeAttrColor(
                    R.style.Theme_Sniper, com.google.android.material.R.attr.colorSecondary
                )

                request.setTextColor(color!!.toInt())

                root.setOnClickListener {
                    if (check.isChecked) {
                        check.checkedState = MaterialCheckBox.STATE_UNCHECKED
                    } else {
                        check.checkedState = MaterialCheckBox.STATE_CHECKED
                    }
                }

                check.addOnCheckedStateChangedListener { _, _ ->
                    val selectApp = appInfoList.find {
                        it.packageName == dataItem.packageName
                    }
                    selectApp?.isCheck = !selectApp?.isCheck!!
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: List<AppInfo>) {
            data = newData
            notifyDataSetChanged()
        }
    }
}