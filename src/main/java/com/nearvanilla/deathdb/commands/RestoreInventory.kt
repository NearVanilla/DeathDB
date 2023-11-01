/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.processing.CommandContainer
import com.nearvanilla.deathdb.DeathDB
import com.nearvanilla.deathdb.libs.Serialization
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandContainer
class RestoreInventory {
    @CommandDescription("Restores the inventory of a given player.")
    @CommandMethod("restoreinventory|ri <player_name> <index>")
    @CommandPermission("deathdb.restoreinventory")
    @Suppress("unused")
    fun restoreInventoryCommand(
        sender: CommandSender,
        @Argument("player_name") playerName: String,
        @Argument("index") index: Int,
    ) {
        // If sender is not player.
        if (sender !is Player) {
            val noPlayerMsg = Component.text("This command can only be ran by players.")
            sender.sendMessage(noPlayerMsg)
            return
        }
        val player: Player = sender
        val targetPlayer = DeathDB.pluginInstance.server.getOfflinePlayer(playerName)
        // If target is offline.
        if (!targetPlayer.isOnline) {
            val playerOfflineMsg = Component.text(
                "This player is offline.",
                NamedTextColor.RED,
                TextDecoration.BOLD,
            )
            player.sendMessage(playerOfflineMsg)
            return
        }
        // If target has never played before.
        if (!targetPlayer.hasPlayedBefore()) {
            val neverPlayedMsg = Component.text(
                "This player has never played on this server before.",
                NamedTextColor.RED,
                TextDecoration.BOLD,
            )
            player.sendMessage(neverPlayedMsg)
            return
        }
        // Restore Inventory.
        val results = DeathDB.dbWrapper.getPlayerInformation(targetPlayer as Player)
        var resultIndex = 1
        var serializedInventory: String? = null
        while (results.next()) {
            if (resultIndex == index) {
                serializedInventory = results.getString("serializedInventory")
                break
            }
            resultIndex += 1
        }
        if (serializedInventory == null) {
            val noInventoryMsg = Component.text(
                "No inventory found.",
                NamedTextColor.RED,
                TextDecoration.BOLD,
            )
            player.sendMessage(noInventoryMsg)
            return
        }
        val targetOnlinePlayer = targetPlayer.player
        if (targetOnlinePlayer == null) {
            val invalidPlayerMsg = Component.text(
                "Failed to find the player.",
                NamedTextColor.RED,
                TextDecoration.BOLD,
            )
            player.sendMessage(invalidPlayerMsg)
            return
        }
        val deserializedInventory = Serialization.Deserialize(serializedInventory)
        val currentInventory = targetOnlinePlayer.inventory.contents
        targetOnlinePlayer.inventory.clear()
        for (item in currentInventory) {
            if (item != null) {
                targetOnlinePlayer.world.dropItem(targetOnlinePlayer.location, item)
            }
        }
        targetOnlinePlayer.inventory.contents = deserializedInventory
        val successMsg = Component.text(
            "Successfully restored inventory.",
            NamedTextColor.GREEN,
            TextDecoration.BOLD,
        )
        player.sendMessage(successMsg)
        return
    }
}
