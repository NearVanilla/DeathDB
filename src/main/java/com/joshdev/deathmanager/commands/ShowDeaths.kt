package com.joshdev.deathmanager.commands

import com.joshdev.deathmanager.DeathManager
import com.joshdev.deathmanager.libs.Database
import com.joshdev.deathmanager.libs.Serialization
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShowDeaths : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        if(sender is Player){

            val player: Player = sender

            if(player.hasPermission("deathmanager.admin")){

                // Sender is player and has right permissions.

                val playerName = args?.get(0)

                if(playerName == null || playerName == ""){

                    val invalidArgumentsComponent = Component.text(

                        "You did not provide the right arguments to run this command!",
                        NamedTextColor.RED,
                        TextDecoration.BOLD

                    )

                    player.sendMessage(invalidArgumentsComponent)
                    return true

                }

                val target = Bukkit.getPlayer(playerName)

                if(target == null){

                    val invalidPlayerNameComponent = Component.text(

                        "Either the player is offline or you provided an invalid player name.",
                        NamedTextColor.RED,
                        TextDecoration.BOLD

                    )

                    player.sendMessage(invalidPlayerNameComponent)
                    return true

                }

                Database.SetupDatabaseIfNotExist(target.uniqueId)
                val result = Database.GetRecords(target.uniqueId)
                var rowCount = 1

                if(result == null){

                    val noRecordComponent = Component.text(

                        "There are no records of this player present in the database.",
                        NamedTextColor.RED,
                        TextDecoration.BOLD

                    )

                    player.sendMessage(noRecordComponent)
                    return true

                }

                val listComponent = Component.text(
                    "$playerName's Deaths (Most Recent 5):",
                    NamedTextColor.DARK_GRAY,
                    TextDecoration.BOLD
                )

                player.sendMessage(listComponent)

                while(result.next()){

                    // Row Count.
                    var resultComponent = Component.text(

                        "$rowCount | ",
                        NamedTextColor.AQUA,
                        TextDecoration.BOLD

                    )

                    // Row Location

                    val x = result.getString(2)
                    val y = result.getString(3)
                    val z = result.getString(4)

                    val locationComponent = Component.text(

                        "$x, $y, $z | ",
                        NamedTextColor.GREEN,
                        TextDecoration.BOLD

                    )

                    resultComponent = resultComponent.append(locationComponent)

                    // Row World
                    val worldComponent = Component.text(

                        result.getString(5) + " | ",
                        NamedTextColor.LIGHT_PURPLE,
                        TextDecoration.BOLD

                    )

                    resultComponent = resultComponent.append(worldComponent)

                    // Row Inventory
                    val inventoryComponent = Component.text(

                        "Restore Inventory",
                        NamedTextColor.GOLD,
                        TextDecoration.BOLD,

                    )

                    val invID = target.uniqueId.toString().replace("-", "") + "_" + rowCount

                    RestoreInv.AddInventory("${invID}_${rowCount}", Serialization.Deserialize(result.getString(6)))

                    val restoreInvClickEvent = ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "restoreinv ${invID}_${rowCount}")
                    val hoverComponent = Component.text("Restore inventory 1 of ${target.name}", NamedTextColor.GOLD, TextDecoration.BOLD)
                    val restoreInvHoverEvent = HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent)

                    inventoryComponent.hoverEvent(restoreInvHoverEvent)
                    inventoryComponent.clickEvent(restoreInvClickEvent)

                    player.sendMessage(resultComponent)
                    player.sendMessage(inventoryComponent)

                    DeathManager.pluginLogger?.info("${invID}_${rowCount}")

                    rowCount++

                }

                return true

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

                "Only players can execute commands.",
                NamedTextColor.RED,
                TextDecoration.BOLD

            )

            sender.sendMessage(invalidSenderComponent)
            return true

        }

    }

}