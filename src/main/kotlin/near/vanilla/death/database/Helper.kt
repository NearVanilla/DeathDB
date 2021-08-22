package near.vanilla.death.database

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.leangen.geantyref.TypeToken
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.inventory.ItemStack

// Simple wrappers for easier on eyes coding
fun warn(message : String, throwable : Throwable) =
        Bukkit.getLogger().warning("${PluginLifecycle.TAG}: $message - ${throwable.message}")

fun warn(message : String) =
        Bukkit.getLogger().warning("${PluginLifecycle.TAG}: $message")

fun info(message : String) =
        Bukkit.getLogger().info("${PluginLifecycle.TAG}: $message")

val STRING_TO_ANY = object : TypeToken<Map<String, Any>>(){}.type!!

const val META_KEY = "meta"

const val SERIALIZED_TYPE_KEY = "serialized_type"

fun toJSON(items : List<ItemStack>) : String {
    val jsonArray = JsonArray()
    items.forEach {
        // Null check needed here probably because of kotlin -> java interop
        if(it != null){
            val itemStackMap = it.serialize()
            val metaItem = itemStackMap.remove(META_KEY)
            val itemStackJSON = Gson().toJson(itemStackMap, STRING_TO_ANY)
            val itemStackJSONObject = JsonParser().parse(itemStackJSON).asJsonObject
            if(it.hasItemMeta() && it.itemMeta != null){
                // Have to serialize meta separately
                val itemMetaMap = it.itemMeta!!.serialize()
                // Weird reflection needed here for built in metadata
                itemMetaMap[SERIALIZED_TYPE_KEY] = ConfigurationSerialization.getAlias(it.itemMeta!!.javaClass)
                val itemMetaJSON = Gson().toJson(itemMetaMap, STRING_TO_ANY)
                itemStackJSONObject.add(META_KEY, JsonParser().parse(itemMetaJSON).asJsonObject)
            }
            jsonArray.add(itemStackJSONObject)
        }
    }
    return jsonArray.toString()
}

fun fromJSON(itemString : String) : List<ItemStack> {
    val parser = JsonParser()
    val element = parser.parse(itemString)
    if(!element.isJsonArray){
        return emptyList()
    }
    val jsonArray = element.asJsonArray
    return jsonArray.map {
        val itemStackMapRep = Gson().fromJson<Map<String, Any>>(it, STRING_TO_ANY).toMutableMap()
        if(itemStackMapRep.containsKey(META_KEY)){
            val itemMetaMap = Gson().fromJson<Map<String, Any>>(it.asJsonObject.get(META_KEY), STRING_TO_ANY).toMutableMap()
            if(itemMetaMap.containsKey(SERIALIZED_TYPE_KEY)){
                itemMetaMap[ConfigurationSerialization.SERIALIZED_TYPE_KEY] = itemMetaMap[SERIALIZED_TYPE_KEY]!!
                val itemMeta = ConfigurationSerialization.deserializeObject(itemMetaMap)
                if(itemMeta != null){
                    itemStackMapRep[META_KEY] = itemMeta
                }
            }
        }
        ItemStack.deserialize(itemStackMapRep)
    }
}
