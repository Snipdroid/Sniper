package ren.imyan.sniper.ui.request

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.checkbox.MaterialCheckBox
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseFragment
import ren.imyan.sniper.base.BaseLoad
import ren.imyan.sniper.common.binding
import ren.imyan.sniper.common.getThemeAttrColor
import ren.imyan.sniper.common.observeState
import ren.imyan.sniper.databinding.FragmentRequestBinding
import ren.imyan.sniper.databinding.ItemIconRequestBinding
import ren.imyan.sniper.model.AppInfo
import ren.imyan.sniper.ui.IconRequestData
import ren.imyan.sniper.ui.IconRequestViewModel

class RequestFragment : BaseFragment(R.layout.fragment_request) {
    private val binding by binding(FragmentRequestBinding::bind)
    private val viewModel by activityViewModels<IconRequestViewModel>()
    private val requestListAdapter = RequestListAdapter(mutableListOf())

    override fun initView(root: View) {
        super.initView(root)
        binding?.requestList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = requestListAdapter
        }
    }

    override fun initViewModel(viewLifecycleOwner: LifecycleOwner) {
        super.initViewModel(viewLifecycleOwner)
        viewModel.uiData.observeState(viewLifecycleOwner, IconRequestData::appInfoList) {
            when (it) {
                is BaseLoad.Success -> {
                    requestListAdapter.updateData(it.data)
                }

                else -> {

                }
            }
        }
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
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: List<AppInfo>) {
            data = newData
            notifyDataSetChanged()
        }
    }
}