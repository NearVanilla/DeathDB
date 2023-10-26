/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathmanager.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.processing.CommandContainer
import com.nearvanilla.deathmanager.DeathManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@CommandContainer
class ShowDeaths {
    @CommandDescription("Show the most recent deaths of a player.")
    @CommandPermission("deathmanager.showdeaths")
    @CommandMethod("showdeaths|sd <player_name>")
    @Suppress("unused")
    fun showDeathsCommand(
        sender: CommandSender,
        @Argument("player_name") playerName: String,
    ) {
        if (sender !is Player) {
            val noPlayerMsg = Component.text("This command can only be ran by players.")
            sender.sendMessage(noPlayerMsg)
            return
        }
        val player: Player = sender
        val targetPlayer: OfflinePlayer = DeathManager.pluginInstance.server.getOfflinePlayer(playerName)
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline) {
            val neverPlayedMsg = Component.text(
                "This player has never been on this server before.",
                NamedTextColor.RED,
                TextDecoration.BOLD,
            )
            player.sendMessage(neverPlayedMsg)
            return
        } else {
            val results = DeathManager.dbWrapper.getPlayerInformation(targetPlayer as Player)
            var deathListMsg = Component.text(
                "List of Deaths\n===============\n",
                NamedTextColor.GRAY,
                TextDecoration.BOLD,
            )
            var index = 1
            while (results.next()) {
                val dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(results.getInt("timeOfDeath").toLong()), ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy @ HH:mm")
                val formattedDateTime = dateTime.format(formatter)
                val posX = String.format("%.3f", results.getDouble("posX"))
                val posY = String.format("%.3f", results.getDouble("posY"))
                val posZ = String.format("%.3f", results.getDouble("posZ"))
                val formattedPosition = "$posX, $posY, $posZ"
                val entryComponent = Component.text(
                    "$index) $formattedDateTime | $formattedPosition\n", // TODO Make clickable button to go to inventory.
                    NamedTextColor.GRAY,
                    TextDecoration.BOLD,
                )
                index += 1
                deathListMsg = deathListMsg.append(entryComponent)
            }
            player.sendMessage(deathListMsg)
            return
        }
    }
}
