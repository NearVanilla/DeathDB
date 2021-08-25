package com.nearvanilla.death.database

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class DeadInventoryScreen(sender : Player, player: Player) {

    private val _holder = DeadInventoryHolder(sender, player)

    fun show() {
        _holder.show()
    }

    private class DeadInventoryHolder(val sender : Player, val player: Player) : InventoryHolder {

        private val _inventory : Inventory =
            Bukkit.createInventory(this, 54, "${player.displayName}'s Dead Inventory")
                .also { it.contents = DeathDatabase[player].toTypedArray() }

        fun show() {
            // Has to be called synchronously
            Bukkit.getScheduler().callSyncMethod(PluginLifecycle.INSTANCE){
                sender.openInventory(_inventory)
            }
        }

        override fun getInventory(): Inventory = _inventory

    }

    object DeadInventoryCloseListener : Listener {

        @EventHandler(priority= EventPriority.MONITOR)
        fun onClose(closeEvent: InventoryCloseEvent) {
            // Update dead inventory
            val holder = closeEvent.inventory.holder
            if(holder is DeadInventoryHolder){
                DeathDatabase[holder.player] = closeEvent.inventory.contents.toList()
            }
        }

    }

}