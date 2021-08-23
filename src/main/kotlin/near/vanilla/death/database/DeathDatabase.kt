package near.vanilla.death.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


object DeathDatabase {

    private val dataSource : HikariDataSource

    private object TAGS {
        val driverClassName = "driverClassName"
        val jdbcUrl = "jdbcUrl"
        val user = "username"
        val password = "password"
        val cachePrepStmts = "dataSource.cachePrepStmts"
        val prepStmtCacheSize = "dataSource.prepStmtCacheSize"
        val prepStmtCacheSqlLimit = "dataSource.prepStmtCacheSqlLimit"
    }

    init {
        val config = HikariConfig(getProperties().absolutePath)
        transaction(
                Database.connect(url = config.jdbcUrl,
                        driver = config.driverClassName,
                        user = config.username,
                        password = config.password)) {
            SchemaUtils.createDatabase("deathdatabase")
        }
        dataSource = HikariDataSource(config)
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(DeadPlayers)
        }
    }

    operator fun get(player: Player) : List<ItemStack> {
        // Thread safe by setting val
        val jsonItems = this[player.uniqueId] ?: return emptyList()
        return ItemSerializer.deserialize(jsonItems)
    }

    fun set(playerDeathEvent: PlayerDeathEvent) = set(playerDeathEvent.entity, playerDeathEvent.drops)

    operator fun set(player: Player, items : List<ItemStack>) = set(player.uniqueId, ItemSerializer.serialize(items))

    private operator fun get(uuid : UUID) : String? {
        var result : String? = ""
        transaction(Database.connect(dataSource)) {
            result = DeadPlayers.select{DeadPlayers.uuid eq uuid.toString()}
                    .map { it[DeadPlayers.items] }
                    .firstOrNull()
        }
        return result
    }

    private operator fun contains(_uuid : UUID) : Boolean {
        return DeadPlayers
                .select{DeadPlayers.uuid eq _uuid.toString()}.count() > 0
    }

    fun getOfflinePlayer(username : String) : Player? {
        var result : Player? = null
        var uuid : String? = null
        transaction(Database.connect(dataSource)) {
            uuid = DeadPlayers.select{DeadPlayers.username eq username}
                    .map { it[DeadPlayers.uuid] }
                    .firstOrNull()
        }
        if(uuid != null){
            result = Bukkit.getPlayer(UUID.fromString(uuid))
        }
        return result
    }

    private operator fun set(_uuid : UUID, _items : String) {
        val offlinePlayer = Bukkit.getServer().getOfflinePlayer(_uuid)
        val name = offlinePlayer.name ?: "Player"
        transaction(Database.connect(dataSource)) {
            // upsert isn't supported by exposed sadly :/
            if(_uuid in this@DeathDatabase){
                DeadPlayers.update {
                    it[uuid] = _uuid.toString()
                    it[username] = name
                    it[items] = _items
                }
            }else{
                DeadPlayers.insert {
                    it[uuid] = _uuid.toString()
                    it[username] = name
                    it[items] = _items
                }
            }
        }
    }

    /**
     * Probably a little jank here, but this creates the properties file if one doesn't exist.
     * The jdbcUrl needs to be a file:// URI and can't be pulled from res, same with password.
     *
     * */
    private fun getProperties() : File {
        val propFile = DeathDatabaseFileSystem["deathdatabase.properties"]
        if(!propFile.exists()){
            warn("PropFile doesn't exist.", IOException())
            try {
                FileOutputStream(propFile).use { output ->
                    val prop = Properties()
                    prop[TAGS.driverClassName] = "org.sqlite.JDBC"
                    prop[TAGS.jdbcUrl] = "jdbc:sqlite://${DeathDatabaseFileSystem["deathdatabase.db"].absolutePath}"
                    prop[TAGS.user] = "admin"
                    prop[TAGS.password] = UUID.randomUUID().toString()
                    prop[TAGS.cachePrepStmts] = "true"
                    prop[TAGS.prepStmtCacheSize] = "250"
                    prop[TAGS.prepStmtCacheSqlLimit] = "2048"
                    prop.store(output, null)
                }
            } catch (io: IOException) {
                warn("Cannot save properties file!", io)
            }
        }
        info("Properties absolute location: ${propFile.absolutePath}")
        return propFile
    }


}