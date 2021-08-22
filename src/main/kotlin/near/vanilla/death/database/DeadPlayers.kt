package near.vanilla.death.database

import org.jetbrains.exposed.sql.*

object DeadPlayers : Table() {
    val uuid = varchar("uuid", 36)
    val username = varchar("username", 36)
    val items = text("items")
    override val primaryKey = PrimaryKey(uuid, name = "PK_User_ID")
}