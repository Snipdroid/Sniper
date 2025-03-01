package ren.imyan.sniper.common

import android.content.Context
import org.xmlpull.v1.XmlPullParser
import ren.imyan.sniper.R
import ren.imyan.sniper.entity.DrawableItem
import ren.imyan.sniper.entity.IconInfo
import ren.imyan.sniper.ktx.get

object DrawableParser {
    fun getDrawable(): List<IconInfo> {
        val drawableList = mutableListOf<IconInfo>()
        val xml = get<Context>().resources.getXml(R.xml.drawable)
        val default = IconInfo(null, mutableSetOf())
        var eventType = xml.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (xml.name == "category") {
                        val name = xml.getAttributeValue(null, "title")
                        drawableList.add(IconInfo(category = name, mutableSetOf()))
                    }
                    if (xml.name == "item") {
                        val drawableId = xml.getAttributeValue(null, "drawable")
                        if (drawableList.isEmpty()) {
                            default.items.add(DrawableItem(drawable = drawableId))
                        } else {
                            drawableList[drawableList.size - 1].items.add(DrawableItem(drawable = drawableId))
                        }
                    }
                }
            }
            eventType = xml.next()
        }
        if (drawableList.isNotEmpty()) {
            drawableList.add(default)
        }

        return drawableList.toList()
    }
}