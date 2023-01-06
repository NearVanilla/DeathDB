package com.joshdev.deathmanager.libs

import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64

class Serialization{

    companion object{

        fun Serialize(inv: PlayerInventory): String {

            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            dataOutput.writeInt(inv.size)

            for(i in 0 until inv.size){

                dataOutput.writeObject(inv.getItem(i))

            }

            dataOutput.close()

            return Base64Coder.encodeLines(outputStream.toByteArray())

        }

        fun Deserialize(inv: String): List<ItemStack> {

            val dataInput = BukkitObjectInputStream(ByteArrayInputStream(Base64Coder.decodeLines(inv)))
            var listOfItems = mutableListOf<ItemStack>()

            for(i in 0 until dataInput.readInt()){

                var currentObject = dataInput.readObject()

                if(currentObject != null){

                    listOfItems.add(dataInput.readObject() as ItemStack)

                }

            }

            return listOfItems

        }

    }


}