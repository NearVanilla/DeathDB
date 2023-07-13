/* Licensed under GNU General Public License v3.0 */
package com.joshdev.deathmanager.libs

import com.joshdev.deathmanager.DeathManager
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.Arrays

class Serialization {
    companion object {
        fun Serialize(inv: PlayerInventory): String { // TODO Account for Armor and Shield slots.
            val serializedItems = JSONArray()
            for (item in inv.contents.filterIsInstance<ItemStack>()) {
                serializedItems.add(Arrays.toString(item.serializeAsBytes()))
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
                    val itemBytes = item
                        .removeSurrounding("[", "]")
                        .split(", ")
                        .map { it.toByte() }
                        .toByteArray()
                    listOfItems = listOfItems.plus(ItemStack.deserializeBytes(itemBytes))
                } catch (e: Exception) {
                    DeathManager.pluginLogger.info("Exception caught when processing an item during deserialization.\n${e.message}")
                }
            }
            return listOfItems
        }
    }
}
