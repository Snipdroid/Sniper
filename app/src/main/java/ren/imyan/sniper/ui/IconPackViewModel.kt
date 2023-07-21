package ren.imyan.sniper.ui

import androidx.lifecycle.viewModelScope
import com.lollipop.iconcore.ui.IconHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ren.imyan.sniper.R
import ren.imyan.sniper.base.BaseLoad
import ren.imyan.sniper.base.BaseViewModel
import ren.imyan.sniper.base.UiAction
import ren.imyan.sniper.base.UiData
import ren.imyan.sniper.base.UiEvent
import ren.imyan.sniper.common.get

sealed class IconPackEvent : UiEvent

sealed class IconPackAction : UiAction

data class IconPackData(val iconMap: BaseLoad<Map<String, Array<IconHelper.IconInfo>>>? = null) :
    UiData

class IconPackViewModel : BaseViewModel<IconPackData, IconPackEvent, IconPackAction>() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getIconPackInfo()
        }
    }

    override fun createInitialState(): IconPackData = IconPackData()

    override fun dispatch(action: IconPackAction) {
        TODO("Not yet implemented")
    }

    private fun getIconPackInfo() {
        emitData {
            copy(iconMap = BaseLoad.Loading)
        }

        val xmlMap = IconHelper.DefaultXmlMap.readFromResource(get(), R.xml.drawable)
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

        emitData {
            copy(iconMap = BaseLoad.Success(iconMap))
        }
    }
}