package com.joshdev.deathmanager.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.*
import kotlin.collections.HashMap

class RestoreInv : CommandExecutor {

    companion object{

        private var invMap = HashMap<HashMap<String, UUID>, List<ItemStack>>()

        fun AddInventory(name: String, uuid: UUID, items: List<ItemStack>){

            val idHashMap = hashMapOf<String, UUID>()
            idHashMap[name] = uuid
            invMap[idHashMap] = items

        }

        fun GetInventory(name: String){

            for(record in invMap){



            }

        }

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        if(sender is Player){

            val player: Player = sender

            if(player.hasPermission("deathmanager.admin")){

                val id = args?.get(0)

                if(id == null){

                    val invalidArgsComponent = Component.text(

                        "You have provided invalid arguments for this command.",
                        NamedTextColor.RED,
                        TextDecoration.BOLD

                    )

                    player.sendMessage(invalidArgsComponent)
                    return true

                }else{

                    val inventory = invMap[id]

                    if(inventory == null){

                        val noInventoryComponent = Component.text(

                            "There is no inventory going by that ID.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD

                        )

                        player.sendMessage(noInventoryComponent)

                    }else{

                        val target = Bukkit.getPlayer(UUID.fromString(id.substring(0, id.length - 2)))

                        if(target == null){

                            val invalidTargetComponent = Component.text(

                                "Couldn't find the target for that inventory.",
                                NamedTextColor.RED,
                                TextDecoration.BOLD

                            )

                            player.sendMessage(invalidTargetComponent)

                        }else{

                            val targetInv = target.inventory

                            targetInv.clear()

                            for(item in inventory){

                                targetInv.addItem(item)

                            }

                            val successComponent = Component.text(

                                "Player's inventory successfully restored.",
                                NamedTextColor.GREEN,
                                TextDecoration.BOLD

                            )

                            player.sendMessage(successComponent)

                        }

                    }

                }

            }else{

                val invalidPermissionComponent = Component.text(

                    "You do not have the correct permissions to run this command!",
                    NamedTextColor.RED,
                    TextDecoration.BOLD

                )

                player.sendMessage(invalidPermissionComponent)
                return true

            }

        }else{

            val invalidSenderComponent = Component.text(

                "Only players can execute this command!",
                NamedTextColor.RED,
                TextDecoration.BOLD

            )

            sender.sendMessage(invalidSenderComponent)
            return true

        }

        return true

    }

}