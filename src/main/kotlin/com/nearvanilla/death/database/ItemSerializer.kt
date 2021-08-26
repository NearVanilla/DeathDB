package com.nearvanilla.death.database

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.*

object ItemSerializer {

    fun serialize(items : List<ItemStack>) : String {
        val jsonArray = JsonArray()
        items.forEach {
            // Null check needed here probably because of kotlin -> java interop
            if(it != null){
                jsonArray.add(Base64.getEncoder().encodeToString(Bukkit.getServer().unsafe.serializeItem(it)))
            }
        }
        return jsonArray.toString()
    }

    fun deserialize(itemString : String) : List<ItemStack> {
        val parser = JsonParser()
        val element = parser.parse(itemString)
        if(!element.isJsonArray){
            return emptyList()
        }
        val jsonArray = element.asJsonArray
        return jsonArray.map {
            Bukkit.getServer().unsafe.deserializeItem(Base64.getDecoder().decode(it.asString))
        }
    }

}