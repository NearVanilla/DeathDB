/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.libs

import com.nearvanilla.deathdb.DeathDB
import org.bukkit.inventory.ItemStack
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.*

class Serialization {
    companion object {
        fun Serialize(inv: Array<ItemStack?>): String { // TODO Account for Armor and Shield slots.
            val serializedItems = JSONArray()
            for (item in inv) {
                if (item != null) {
                    val serializedItem = item.serializeAsBytes()
                    val encodedItem = Base64.getEncoder().encodeToString(serializedItem)
                    serializedItems.add(encodedItem)
                }
            }
            val mainObject = JSONObject()
            mainObject["items"] = serializedItems
            return mainObject.toString()
        }
        fun Deserialize(inv: String): Array<ItemStack> {
            val parser = JSONParser()
            val parsedInventory = parser.parse(inv) as JSONObject
            val serializedItems = parsedInventory["items"] as JSONArray
            var listOfItems = arrayOf<ItemStack>()
            for (i in 0 until serializedItems.count()) {
                try {
                    val item = serializedItems[i] as String
                    val decodedItem = Base64.getDecoder().decode(item)
                    listOfItems = listOfItems.plus(ItemStack.deserializeBytes(decodedItem))
                } catch (e: Exception) {
                    DeathDB.pluginLogger.info("Exception caught when processing an item during deserialization.\n${e.message}")
                }
            }
            return listOfItems
        }
    }
}
