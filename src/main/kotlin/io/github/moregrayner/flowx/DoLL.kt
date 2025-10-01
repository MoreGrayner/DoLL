package io.github.moremmand.doll

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// Entity Type Objects
data object ArmorStand : DollEntityType(EntityType.ARMOR_STAND)
data object Bat : DollEntityType(EntityType.BAT)
data object Bee : DollEntityType(EntityType.BEE)
data object Blaze : DollEntityType(EntityType.BLAZE)
data object Cat : DollEntityType(EntityType.CAT)
data object CaveSpider : DollEntityType(EntityType.CAVE_SPIDER)
data object Chicken : DollEntityType(EntityType.CHICKEN)
data object Cod : DollEntityType(EntityType.COD)
data object Cow : DollEntityType(EntityType.COW)
data object Creeper : DollEntityType(EntityType.CREEPER)
data object Dolphin : DollEntityType(EntityType.DOLPHIN)
data object Donkey : DollEntityType(EntityType.DONKEY)
data object Drowned : DollEntityType(EntityType.DROWNED)
data object ElderGuardian : DollEntityType(EntityType.ELDER_GUARDIAN)
data object EnderDragon : DollEntityType(EntityType.ENDER_DRAGON)
data object Enderman : DollEntityType(EntityType.ENDERMAN)
data object Endermite : DollEntityType(EntityType.ENDERMITE)
data object Evoker : DollEntityType(EntityType.EVOKER)
data object Fox : DollEntityType(EntityType.FOX)
data object Ghast : DollEntityType(EntityType.GHAST)
data object Giant : DollEntityType(EntityType.GIANT)
data object GlowSquid : DollEntityType(EntityType.GLOW_SQUID)
data object Goat : DollEntityType(EntityType.GOAT)
data object Guardian : DollEntityType(EntityType.GUARDIAN)
data object Hoglin : DollEntityType(EntityType.HOGLIN)
data object Horse : DollEntityType(EntityType.HORSE)
data object Husk : DollEntityType(EntityType.HUSK)
data object Illusioner : DollEntityType(EntityType.ILLUSIONER)
data object IronGolem : DollEntityType(EntityType.IRON_GOLEM)
data object Llama : DollEntityType(EntityType.LLAMA)
data object MagmaCube : DollEntityType(EntityType.MAGMA_CUBE)
data object Mooshroom : DollEntityType(EntityType.MOOSHROOM)
data object Mule : DollEntityType(EntityType.MULE)
data object Ocelot : DollEntityType(EntityType.OCELOT)
data object Panda : DollEntityType(EntityType.PANDA)
data object Parrot : DollEntityType(EntityType.PARROT)
data object Phantom : DollEntityType(EntityType.PHANTOM)
data object Pig : DollEntityType(EntityType.PIG)
data object Piglin : DollEntityType(EntityType.PIGLIN)
data object PiglinBrute : DollEntityType(EntityType.PIGLIN_BRUTE)
data object Pillager : DollEntityType(EntityType.PILLAGER)
data object PolarBear : DollEntityType(EntityType.POLAR_BEAR)
data object Pufferfish : DollEntityType(EntityType.PUFFERFISH)
data object Rabbit : DollEntityType(EntityType.RABBIT)
data object Ravager : DollEntityType(EntityType.RAVAGER)
data object Salmon : DollEntityType(EntityType.SALMON)
data object Sheep : DollEntityType(EntityType.SHEEP)
data object Shulker : DollEntityType(EntityType.SHULKER)
data object Silverfish : DollEntityType(EntityType.SILVERFISH)
data object Skeleton : DollEntityType(EntityType.SKELETON)
data object SkeletonHorse : DollEntityType(EntityType.SKELETON_HORSE)
data object Slime : DollEntityType(EntityType.SLIME)
data object SnowGolem : DollEntityType(EntityType.SNOW_GOLEM)
data object Spider : DollEntityType(EntityType.SPIDER)
data object Squid : DollEntityType(EntityType.SQUID)
data object Stray : DollEntityType(EntityType.STRAY)
data object Strider : DollEntityType(EntityType.STRIDER)
data object TraderLlama : DollEntityType(EntityType.TRADER_LLAMA)
data object TropicalFish : DollEntityType(EntityType.TROPICAL_FISH)
data object Turtle : DollEntityType(EntityType.TURTLE)
data object Vex : DollEntityType(EntityType.VEX)
data object Villager : DollEntityType(EntityType.VILLAGER)
data object Vindicator : DollEntityType(EntityType.VINDICATOR)
data object WanderingTrader : DollEntityType(EntityType.WANDERING_TRADER)
data object Witch : DollEntityType(EntityType.WITCH)
data object Wither : DollEntityType(EntityType.WITHER)
data object WitherSkeleton : DollEntityType(EntityType.WITHER_SKELETON)
data object Wolf : DollEntityType(EntityType.WOLF)
data object Zoglin : DollEntityType(EntityType.ZOGLIN)
data object Zombie : DollEntityType(EntityType.ZOMBIE)
data object ZombieHorse : DollEntityType(EntityType.ZOMBIE_HORSE)
data object ZombieVillager : DollEntityType(EntityType.ZOMBIE_VILLAGER)
data object ZombifiedPiglin : DollEntityType(EntityType.ZOMBIFIED_PIGLIN)
data object EntityPlayer : DollEntityType(EntityType.PLAYER)

sealed class DollEntityType(val bukkitType: EntityType)

// Custom Model Support
sealed class DollModel {
    data class CustomModelData(val material: org.bukkit.Material, val customModelData: Int) : DollModel()
    data class PlayerSkin(val texture: String, val signature: String? = null) : DollModel()
    data class ItemModel(val itemStack: org.bukkit.inventory.ItemStack) : DollModel()
}

// Model creation helpers
fun customModel(material: org.bukkit.Material, customModelData: Int): DollModel {
    return DollModel.CustomModelData(material, customModelData)
}

fun playerSkin(textureValue: String, signature: String? = null): DollModel {
    return DollModel.PlayerSkin(textureValue, signature)
}

fun itemModel(itemStack: org.bukkit.inventory.ItemStack): DollModel {
    return DollModel.ItemModel(itemStack)
}

// Ticker System for efficient task scheduling
class DollTicker(private val plugin: JavaPlugin) {
    private val tasks = ConcurrentHashMap<Int, TickTask>()
    private var nextId = 0
    private var tickCount = 0L
    private var running = false

    data class TickTask(
        val id: Int,
        val interval: Long,
        val callback: (Long) -> Unit,
        var lastRun: Long = 0,
        var cancelled: Boolean = false
    )

    fun start() {
        if (running) return
        running = true

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            tickCount++
            tasks.values.removeIf { it.cancelled }

            tasks.values.forEach { task ->
                if (tickCount - task.lastRun >= task.interval) {
                    task.callback(tickCount)
                    task.lastRun = tickCount
                }
            }
        }, 0L, 1L)
    }

    fun register(interval: Long, callback: (Long) -> Unit): Int {
        val id = nextId++
        tasks[id] = TickTask(id, interval, callback, tickCount)
        return id
    }

    fun unregister(id: Int) {
        tasks[id]?.cancelled = true
    }

    fun clear() {
        tasks.clear()
    }

    fun getTickCount() = tickCount
}

// Enhanced Fake Entity with ProtocolLib support
class FakeEntity(
    val entityId: Int,
    val uuid: UUID,
    val type: EntityType,
    var location: Location,
    var customName: String? = null,
    var isCustomNameVisible: Boolean = true
) {
    private val viewers = ConcurrentHashMap<UUID, Player>()

    var health: Double = 20.0
        set(value) {
            field = value.coerceIn(0.0, maxHealth)
            updateMetadata()
        }

    var maxHealth: Double = 20.0
        set(value) {
            field = value.coerceAtLeast(1.0)
            health = health.coerceIn(0.0, field)
            updateMetadata()
        }

    var isGlowing: Boolean = false
        set(value) {
            field = value
            updateMetadata()
        }

    var isInvisible: Boolean = false
        set(value) {
            field = value
            updateMetadata()
        }

    var model: DollModel? = null
        set(value) {
            field = value
            updateModel()
        }

    fun addViewer(player: Player) {
        viewers[player.uniqueId] = player
        spawn(player)
    }

    fun removeViewer(player: Player) {
        viewers.remove(player.uniqueId) ?: return
        destroy(player)
    }

    fun hasViewer(player: Player) = viewers.containsKey(player.uniqueId)

    fun getViewers(): Collection<Player> {
        return viewers.values.toList()
    }

    private fun spawn(player: Player) {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        // Spawn entity packet
        val spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY)
        spawnPacket.integers.write(0, entityId)
        spawnPacket.uuiDs.write(0, uuid)
        spawnPacket.entityTypeModifier.write(0, type)
        spawnPacket.doubles
            .write(0, location.x)
            .write(1, location.y)
            .write(2, location.z)
        spawnPacket.integers
            .write(1, (location.pitch * 256.0 / 360.0).toInt())
            .write(2, (location.yaw * 256.0 / 360.0).toInt())

        protocolManager.sendServerPacket(player, spawnPacket)
        updateMetadata(player)
        model?.let { updateModel(player) }
    }

    fun destroy() {
        viewers.values.forEach { destroy(it) }
        viewers.clear()
    }

    private fun destroy(player: Player) {
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        packet.intLists.write(0, listOf(entityId))
        protocolManager.sendServerPacket(player, packet)
    }

    fun teleport(newLocation: Location) {
        location = newLocation
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT)
        packet.integers.write(0, entityId)
        packet.doubles
            .write(0, location.x)
            .write(1, location.y)
            .write(2, location.z)
        packet.bytes
            .write(0, (location.yaw * 256 / 360).toInt().toByte())
            .write(1, (location.pitch * 256 / 360).toInt().toByte())
        packet.booleans.write(0, true)

        viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
    }

    fun move(x: Double, y: Double, z: Double) {
        location.add(x, y, z)
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val packet = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK)
        packet.integers.write(0, entityId)
        packet.shorts
            .write(0, (x * 4096).toInt().toShort())
            .write(1, (y * 4096).toInt().toShort())
            .write(2, (z * 4096).toInt().toShort())
        packet.bytes
            .write(0, (location.yaw * 256 / 360).toInt().toByte())
            .write(1, (location.pitch * 256 / 360).toInt().toByte())
        packet.booleans.write(0, true)

        viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
    }

    fun rotate(yaw: Float, pitch: Float) {
        location.yaw = yaw
        location.pitch = pitch
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val rotPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK)
        rotPacket.integers.write(0, entityId)
        rotPacket.bytes
            .write(0, (yaw * 256 / 360).toInt().toByte())
            .write(1, (pitch * 256 / 360).toInt().toByte())
        rotPacket.booleans.write(0, true)

        val headPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
        headPacket.integers.write(0, entityId)
        headPacket.bytes.write(0, (yaw * 256 / 360).toInt().toByte())

        viewers.values.forEach {
            protocolManager.sendServerPacket(it, rotPacket)
            protocolManager.sendServerPacket(it, headPacket)
        }
    }

    fun setVelocity(velocity: Vector) {
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY)
        packet.integers
            .write(0, entityId)
            .write(1, (velocity.x * 8000).toInt())
            .write(2, (velocity.y * 8000).toInt())
            .write(3, (velocity.z * 8000).toInt())

        viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
    }

    private fun updateMetadata(target: Player? = null) {
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        packet.integers.write(0, entityId)

        val watcher = WrappedDataWatcher()

        customName?.let {
            val serializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true)
            watcher.setObject(WrappedDataWatcher.WrappedDataWatcherObject(2, serializer),
                Optional.of(WrappedChatComponent.fromText(it).handle))
        }

        watcher.setObject(WrappedDataWatcher.WrappedDataWatcherObject(3,
            WrappedDataWatcher.Registry.get(Boolean::class.javaObjectType)), isCustomNameVisible)

        var flags: Byte = 0
        if (isGlowing) flags = (flags.toInt() or 0x40).toByte()
        if (isInvisible) flags = (flags.toInt() or 0x20).toByte()

        watcher.setObject(WrappedDataWatcher.WrappedDataWatcherObject(0,
            WrappedDataWatcher.Registry.get(Byte::class.javaObjectType)), flags)

        packet.watchableCollectionModifier.write(0, watcher.watchableObjects)

        if (target != null) {
            protocolManager.sendServerPacket(target, packet)
        } else {
            viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
        }
    }

    private fun updateModel(target: Player? = null) {
        model?.let { m ->
            when (m) {
                is DollModel.CustomModelData -> applyCustomModelData(m, target)
                is DollModel.PlayerSkin -> applyPlayerSkin(m, target)
                is DollModel.ItemModel -> applyItemModel(m, target)
            }
        }
    }

    private fun applyCustomModelData(model: DollModel.CustomModelData, target: Player? = null) {
        if (type == EntityType.ARMOR_STAND) {
            val itemStack = org.bukkit.inventory.ItemStack(model.material)
            val meta = itemStack.itemMeta
            meta?.setCustomModelData(model.customModelData)
            itemStack.itemMeta = meta

            val protocolManager = ProtocolLibrary.getProtocolManager()
            val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
            packet.integers.write(0, entityId)

            val equipmentList = listOf(
                com.comphenix.protocol.wrappers.Pair(
                    EnumWrappers.ItemSlot.HEAD,
                    itemStack
                )
            )
            packet.slotStackPairLists.write(0, equipmentList)

            if (target != null) {
                protocolManager.sendServerPacket(target, packet)
            } else {
                viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
            }
        }
    }

    private fun applyPlayerSkin(model: DollModel.PlayerSkin, target: Player? = null) {
        if (type == EntityType.PLAYER) {
            val protocolManager = ProtocolLibrary.getProtocolManager()
            val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)
            packet.integers.write(0, entityId)

            val watcher = WrappedDataWatcher()
            watcher.setObject(WrappedDataWatcher.WrappedDataWatcherObject(17,
                WrappedDataWatcher.Registry.get(Byte::class.javaObjectType)), 0x7F.toByte())

            packet.watchableCollectionModifier.write(0, watcher.watchableObjects)

            if (target != null) {
                protocolManager.sendServerPacket(target, packet)
            } else {
                viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
            }
        }
    }

    private fun applyItemModel(model: DollModel.ItemModel, target: Player? = null) {
        if (type == EntityType.ARMOR_STAND) {
            val protocolManager = ProtocolLibrary.getProtocolManager()
            val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
            packet.integers.write(0, entityId)

            val equipmentList = listOf(
                com.comphenix.protocol.wrappers.Pair(
                    EnumWrappers.ItemSlot.HEAD,
                    model.itemStack
                )
            )
            packet.slotStackPairLists.write(0, equipmentList)

            if (target != null) {
                protocolManager.sendServerPacket(target, packet)
            } else {
                viewers.values.forEach { protocolManager.sendServerPacket(it, packet) }
            }
        }
    }
}

// FakeServer for persistent fake entities across reconnects
object FakeServer {
    private val fakeEntities = ConcurrentHashMap<String, FakeEntity>()

    fun register(name: String, entity: FakeEntity) {
        fakeEntities[name] = entity
    }

    fun unregister(name: String) {
        fakeEntities[name]?.destroy()
        fakeEntities.remove(name)
    }

    fun get(name: String) = fakeEntities[name]

    fun getAll() = fakeEntities.values.toList()

    fun onPlayerJoin(player: Player) {
        fakeEntities.values.forEach { entity ->
            if (entity.location.world == player.world) {
                entity.addViewer(player)
            }
        }
    }

    fun onPlayerQuit(player: Player) {
        fakeEntities.values.forEach { entity ->
            entity.removeViewer(player)
        }
    }

    fun clear() {
        fakeEntities.values.forEach { it.destroy() }
        fakeEntities.clear()
    }
}

// Signal Manager for movement control
object SignalManager {
    private val signals = ConcurrentHashMap<String, MutableSet<String>>()

    fun waitFor(signalName: String, dollName: String) {
        signals.getOrPut(signalName) { ConcurrentHashMap.newKeySet() }.add(dollName)
    }

    fun emit(signalName: String) {
        signals[signalName]?.forEach { dollName ->
            DollRegistry.get(dollName)?.resumeMovement()
        }
        signals.remove(signalName)
    }

    fun clear() {
        signals.clear()
    }
}

enum class DollMode {
    FAKE, REALITY
}

// Doll Builder
class DollBuilder(
    private val name: String,
    private val plugin: JavaPlugin
) {
    private var entityType: DollEntityType? = null
    private var location: Location? = null
    private var mode: DollMode = DollMode.REALITY
    private var configurator: (DollContext.() -> Unit)? = null
    private var dollModel: DollModel? = null

    infix fun type(type: DollEntityType): DollBuilder {
        this.entityType = type
        return this
    }

    infix fun location(loc: Location): DollBuilder {
        this.location = loc
        return this
    }

    infix fun model(model: DollModel): DollBuilder {
        this.dollModel = model
        return this
    }

    val fake: DollBuilder
        get() {
            this.mode = DollMode.FAKE
            create()
            return this
        }

    fun fake(block: DollContext.() -> Unit): DollBuilder {
        this.mode = DollMode.FAKE
        this.configurator = block
        create()
        return this
    }

    val reality: DollBuilder
        get() {
            this.mode = DollMode.REALITY
            create()
            return this
        }

    fun reality(block: DollContext.() -> Unit): DollBuilder {
        this.mode = DollMode.REALITY
        this.configurator = block
        create()
        return this
    }

    private fun create() {
        val type = entityType ?: throw IllegalStateException("Entity type must be specified")
        val loc = location ?: throw IllegalStateException("Location must be specified")

        val context = when (mode) {
            DollMode.FAKE -> createFakeDoll(name, type, loc)
            DollMode.REALITY -> createRealityDoll(name, type, loc)
        }

        dollModel?.let {
            context.model = it
        }

        configurator?.let {
            it.invoke(context)
            if (context.savedPresetName != null) {
                DollPresets.save(context.savedPresetName!!, context)
            }
        }

        DollRegistry.register(name, context)
    }

    private fun createFakeDoll(name: String, type: DollEntityType, location: Location): DollContext {
        val context = DollContext(name, DollMode.FAKE, plugin)
        val entityId = Random().nextInt(100000) + 10000
        val uuid = UUID.randomUUID()

        val fakeEntity = FakeEntity(entityId, uuid, type.bukkitType, location, name)
        context.fakeEntity = fakeEntity

        FakeServer.register(name, fakeEntity)

        Bukkit.getOnlinePlayers()
            .filter { it.world == location.world }
            .forEach { fakeEntity.addViewer(it) }

        plugin.logger.info("Created fake doll: $name (ID: $entityId) at $location")
        return context
    }

    private fun createRealityDoll(name: String, type: DollEntityType, location: Location): DollContext {
        val context = DollContext(name, DollMode.REALITY, plugin)

        val entity = location.world?.spawnEntity(location, type.bukkitType)
        entity?.let {
            it.customName = name
            it.isCustomNameVisible = true
            context.realEntity = it
            DollRegistry.registerEntity(name, it)
        }

        plugin.logger.info("Created reality doll: $name at $location")
        return context
    }
}

// Doll Context
class DollContext(
    val name: String,
    val mode: DollMode,
    private val plugin: JavaPlugin
) {
    var id: NamespacedKey = NamespacedKey(plugin, name)
    var attackable: Boolean = true
    var knockBackDistance: Double = 0.4
    var health: Double = 20.0
        set(value) {
            field = value
            updateHealth()
        }

    var isGlowing: Boolean = false
        set(value) {
            field = value
            updateGlowing()
        }

    var isInvisible: Boolean = false
        set(value) {
            field = value
            updateInvisible()
        }

    var model: DollModel? = null
        set(value) {
            field = value
            applyModel()
        }

    internal var realEntity: Entity? = null
    internal var fakeEntity: FakeEntity? = null
    internal var savedPresetName: String? = null

    private var rightClickAction: ((Player) -> Unit)? = null
    private var leftClickAction: ((Player) -> Unit)? = null
    private var movementBuilder: MovementBuilder? = null
    private var tickerId: Int? = null
    private var isWaitingForSignal = false
    private var currentSignal: String? = null
    private var signalActions: List<MovementBlock>? = null

    init {
        applySettings()
    }

    private fun applySettings() {
        when (mode) {
            DollMode.REALITY -> {
                realEntity?.let { entity ->
                    if (entity is LivingEntity) {
                        entity.maxHealth = health
                        entity.health = health
                        entity.setAI(!attackable)
                        entity.isGlowing = isGlowing
                        entity.isInvisible = isInvisible
                    }
                }
            }
            DollMode.FAKE -> {
                fakeEntity?.let { entity ->
                    entity.health = health
                    entity.maxHealth = health
                    entity.customName = name
                    entity.isGlowing = isGlowing
                    entity.isInvisible = isInvisible
                }
            }
        }
    }

    private fun updateHealth() {
        when (mode) {
            DollMode.REALITY -> {
                realEntity?.let { entity ->
                    if (entity is LivingEntity) {
                        entity.maxHealth = health
                        entity.health = health
                    }
                }
            }
            DollMode.FAKE -> {
                fakeEntity?.let { entity ->
                    entity.health = health
                    entity.maxHealth = health
                }
            }
        }
    }

    private fun updateGlowing() {
        when (mode) {
            DollMode.REALITY -> {
                realEntity?.isGlowing = isGlowing
            }
            DollMode.FAKE -> {
                fakeEntity?.isGlowing = isGlowing
            }
        }
    }

    private fun updateInvisible() {
        when (mode) {
            DollMode.REALITY -> {
                realEntity?.let {
                    if (it is LivingEntity) {
                        it.isInvisible = isInvisible
                    }
                }
            }
            DollMode.FAKE -> {
                fakeEntity?.isInvisible = isInvisible
            }
        }
    }

    private fun applyModel() {
        model?.let { m ->
            when (mode) {
                DollMode.REALITY -> {
                    realEntity?.let { entity ->
                        when (m) {
                            is DollModel.CustomModelData -> {
                                if (entity is org.bukkit.entity.ArmorStand) {
                                    val item = org.bukkit.inventory.ItemStack(m.material)
                                    val meta = item.itemMeta
                                    meta?.setCustomModelData(m.customModelData)
                                    item.itemMeta = meta
                                    entity.equipment?.helmet = item
                                }
                            }
                            is DollModel.ItemModel -> {
                                if (entity is org.bukkit.entity.ArmorStand) {
                                    entity.equipment?.helmet = m.itemStack
                                }
                            }
                            is DollModel.PlayerSkin -> {}
                        }
                    }
                }
                DollMode.FAKE -> {
                    fakeEntity?.model = m
                }
            }
        }
    }

    fun rightClicked(action: (Player) -> Unit) {
        this.rightClickAction = action
    }

    fun leftClicked(action: (Player) -> Unit) {
        this.leftClickAction = action
    }

    fun movement(builder: MovementBuilder.() -> Unit) {
        when (mode) {
            DollMode.REALITY -> {
                realEntity?.let { entity ->
                    if (entity is LivingEntity) {
                        entity.setAI(false)
                    }
                }
            }
            DollMode.FAKE -> {}
        }

        val movementBuilder = MovementBuilder(this, plugin)
        builder.invoke(movementBuilder)
        this.movementBuilder = movementBuilder
        movementBuilder.start()
    }

    fun stopMovement() {
        movementBuilder?.stop()
        movementBuilder = null

        when (mode) {
            DollMode.REALITY -> {
                realEntity?.let { entity ->
                    if (entity is LivingEntity) {
                        entity.setAI(attackable)
                    }
                }
            }
            DollMode.FAKE -> {}
        }
    }

    fun tick(interval: Long = 1L, action: (Long) -> Unit) {
        tickerId = DollTickerManager.getTicker(plugin).register(interval, action)
    }

    fun signal(signalName: String) {
        SignalManager.emit(signalName)
    }

    fun save(presetName: String) {
        this.savedPresetName = presetName
    }

    internal fun getRightClickAction() = rightClickAction
    internal fun getLeftClickAction() = leftClickAction
    internal fun getMovement() = movementBuilder

    internal fun applyKnockback(attacker: Player) {
        if (knockBackDistance <= 0.0) return

        when (mode) {
            DollMode.REALITY -> {
                realEntity?.let { entity ->
                    val direction = attacker.location.toVector()
                        .subtract(entity.location.toVector()).normalize()
                    attacker.velocity = direction.multiply(knockBackDistance)
                }
            }
            DollMode.FAKE -> {
                fakeEntity?.let { entity ->
                    val direction = attacker.location.toVector()
                        .subtract(entity.location.toVector()).normalize()
                    attacker.velocity = direction.multiply(knockBackDistance)
                }
            }
        }
    }

    internal fun isWaitingSignal() = isWaitingForSignal

    internal fun setWaitingSignal(signal: String, actions: List<MovementBlock>) {
        isWaitingForSignal = true
        currentSignal = signal
        signalActions = actions
    }

    internal fun resumeMovement() {
        isWaitingForSignal = false
        signalActions?.let { actions ->
            executeSignalActions(actions)
        }
        currentSignal = null
        signalActions = null
    }

    private fun executeSignalActions(actions: List<MovementBlock>) {
        val subMovement = MovementBuilder(this, plugin)
        actions.forEach { subMovement.movements.add(it) }
        subMovement.start()
    }

    fun destroy() {
        stopMovement()
        tickerId?.let { DollTickerManager.getTicker(plugin).unregister(it) }

        when (mode) {
            DollMode.REALITY -> {
                realEntity?.remove()
            }
            DollMode.FAKE -> {
                fakeEntity?.destroy()
                FakeServer.unregister(name)
            }
        }
    }
}

// Movement Builder
class MovementBuilder(
    private val context: DollContext,
    private val plugin: JavaPlugin
) {
    internal val movements = mutableListOf<MovementBlock>()
    private var currentIndex = 0
    private var tickerId: Int? = null

    fun to(location: Location, duration: Long = 20L): MovementBuilder {
        movements.add(MovementBlock.ToLocation(location, duration))
        return this
    }

    fun forward(distance: Double): MovementBuilder {
        movements.add(MovementBlock.Forward(distance))
        return this
    }

    fun rotate(yaw: Float, pitch: Float, duration: Long = 10L): MovementBuilder {
        movements.add(MovementBlock.Rotate(yaw, pitch, duration))
        return this
    }

    fun wait(ticks: Long): MovementBuilder {
        movements.add(MovementBlock.Wait(ticks))
        return this
    }

    fun waitFor(signalName: String, block: MovementBuilder.() -> Unit): MovementBuilder {
        val subBuilder = MovementBuilder(context, plugin)
        block.invoke(subBuilder)
        movements.add(MovementBlock.WaitForSignal(signalName, subBuilder.movements.toList()))
        return this
    }

    fun loop(): MovementBuilder {
        movements.add(MovementBlock.Loop)
        return this
    }

    internal fun start() {
        if (movements.isEmpty()) return

        val ticker = DollTickerManager.getTicker(plugin)
        var waitTicks = 0L
        var animationTicks = 0L
        var isWaiting = false
        var isAnimating = false
        var animationStartLoc: Location? = null
        var animationEndLoc: Location? = null
        var animationStartYaw = 0f
        var animationEndYaw = 0f
        var animationStartPitch = 0f
        var animationEndPitch = 0f

        tickerId = ticker.register(1L) { tick ->
            if (context.isWaitingSignal()) {
                if (!context.isWaitingSignal()) {
                    currentIndex++
                }
                return@register
            }

            if (currentIndex >= movements.size) return@register

            val movement = movements[currentIndex]

            when {
                isWaiting -> {
                    waitTicks++
                    if (waitTicks >= (movement as MovementBlock.Wait).ticks) {
                        isWaiting = false
                        waitTicks = 0
                        currentIndex++
                    }
                }
                isAnimating -> {
                    animationTicks++

                    when (val currentMovement = movements[currentIndex]) {
                        is MovementBlock.ToLocation -> {
                            val progress = animationTicks.toDouble() / currentMovement.duration
                            if (progress >= 1.0) {
                                executeMovement(movement)
                                isAnimating = false
                                animationTicks = 0
                                currentIndex++
                            } else {
                                val currentLoc = interpolateLocation(
                                    animationStartLoc!!,
                                    animationEndLoc!!,
                                    progress
                                )
                                when (context.mode) {
                                    DollMode.REALITY -> context.realEntity?.teleport(currentLoc)
                                    DollMode.FAKE -> context.fakeEntity?.teleport(currentLoc)
                                }
                            }
                        }
                        is MovementBlock.Rotate -> {
                            val progress = animationTicks.toDouble() / currentMovement.duration
                            if (progress >= 1.0) {
                                executeMovement(movement)
                                isAnimating = false
                                animationTicks = 0
                                currentIndex++
                            } else {
                                val yaw = animationStartYaw + (animationEndYaw - animationStartYaw) * progress.toFloat()
                                val pitch = animationStartPitch + (animationEndPitch - animationStartPitch) * progress.toFloat()

                                when (context.mode) {
                                    DollMode.REALITY -> {
                                        context.realEntity?.let {
                                            val loc = it.location
                                            loc.yaw = yaw
                                            loc.pitch = pitch
                                            it.teleport(loc)
                                        }
                                    }
                                    DollMode.FAKE -> {
                                        context.fakeEntity?.rotate(yaw, pitch)
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
                else -> {
                    when (movement) {
                        is MovementBlock.Wait -> {
                            isWaiting = true
                        }
                        is MovementBlock.ToLocation -> {
                            isAnimating = true
                            animationStartLoc = when (context.mode) {
                                DollMode.REALITY -> context.realEntity?.location?.clone()
                                DollMode.FAKE -> context.fakeEntity?.location?.clone()
                            }
                            animationEndLoc = movement.location
                        }
                        is MovementBlock.Rotate -> {
                            isAnimating = true
                            when (context.mode) {
                                DollMode.REALITY -> {
                                    context.realEntity?.let {
                                        animationStartYaw = it.location.yaw
                                        animationStartPitch = it.location.pitch
                                    }
                                }
                                DollMode.FAKE -> {
                                    context.fakeEntity?.let {
                                        animationStartYaw = it.location.yaw
                                        animationStartPitch = it.location.pitch
                                    }
                                }
                            }
                            animationEndYaw = movement.yaw
                            animationEndPitch = movement.pitch
                        }
                        is MovementBlock.Forward -> {
                            executeMovement(movement)
                            currentIndex++
                        }
                        is MovementBlock.WaitForSignal -> {
                            context.setWaitingSignal(movement.signalName, movement.actions)
                            SignalManager.waitFor(movement.signalName, context.name)
                        }
                        is MovementBlock.Loop -> {
                            currentIndex = 0
                        }
                    }
                }
            }
        }
    }

    private fun executeMovement(movement: MovementBlock) {
        when (movement) {
            is MovementBlock.ToLocation -> {
                when (context.mode) {
                    DollMode.REALITY -> context.realEntity?.teleport(movement.location)
                    DollMode.FAKE -> context.fakeEntity?.teleport(movement.location)
                }
            }
            is MovementBlock.Forward -> {
                when (context.mode) {
                    DollMode.REALITY -> {
                        context.realEntity?.let { entity ->
                            val direction = entity.location.direction.normalize()
                            entity.teleport(entity.location.add(direction.multiply(movement.distance)))
                        }
                    }
                    DollMode.FAKE -> {
                        context.fakeEntity?.let { entity ->
                            val direction = entity.location.direction.normalize()
                            val newLoc = entity.location.clone().add(direction.multiply(movement.distance))
                            entity.teleport(newLoc)
                        }
                    }
                }
            }
            is MovementBlock.Rotate -> {
                when (context.mode) {
                    DollMode.REALITY -> {
                        context.realEntity?.let { entity ->
                            val loc = entity.location
                            loc.yaw = movement.yaw
                            loc.pitch = movement.pitch
                            entity.teleport(loc)
                        }
                    }
                    DollMode.FAKE -> {
                        context.fakeEntity?.rotate(movement.yaw, movement.pitch)
                    }
                }
            }
            else -> {}
        }
    }

    private fun interpolateLocation(start: Location, end: Location, progress: Double): Location {
        val x = start.x + (end.x - start.x) * progress
        val y = start.y + (end.y - start.y) * progress
        val z = start.z + (end.z - start.z) * progress
        return Location(start.world, x, y, z, start.yaw, start.pitch)
    }

    fun stop() {
        tickerId?.let { DollTickerManager.getTicker(plugin).unregister(it) }
        tickerId = null
        currentIndex = 0
        movements.clear()
    }
}

sealed class MovementBlock {
    data class ToLocation(val location: Location, val duration: Long) : MovementBlock()
    data class Forward(val distance: Double) : MovementBlock()
    data class Rotate(val yaw: Float, val pitch: Float, val duration: Long) : MovementBlock()
    data class Wait(val ticks: Long) : MovementBlock()
    data class WaitForSignal(val signalName: String, val actions: List<MovementBlock>) : MovementBlock()
    data object Loop : MovementBlock()
}

// Composite Doll for multiple entities
class CompositeDoll(
    val name: String,
    private val dolls: List<DollContext>,
    private val plugin: JavaPlugin
) {
    var id: NamespacedKey = NamespacedKey(plugin, name)

    var attackAble: Boolean = true
        set(value) {
            field = value
            dolls.forEach { it.attackable = value }
        }

    var knockBackDistance: Double = 0.4
        set(value) {
            field = value
            dolls.forEach { it.knockBackDistance = value }
        }

    var health: Double = 20.0
        set(value) {
            field = value
            dolls.forEach { it.health = value }
        }

    var isGlowing: Boolean = false
        set(value) {
            field = value
            dolls.forEach { it.isGlowing = value }
        }

    var isInvisible: Boolean = false
        set(value) {
            field = value
            dolls.forEach { it.isInvisible = value }
        }

    private var rightClickAction: ((Player, DollContext) -> Unit)? = null
    private var leftClickAction: ((Player, DollContext) -> Unit)? = null

    init {
        dolls.forEach { doll ->
            doll.rightClicked { player ->
                rightClickAction?.invoke(player, doll)
            }
            doll.leftClicked { player ->
                leftClickAction?.invoke(player, doll)
            }
        }
    }

    fun rightClicked(action: (Player, DollContext) -> Unit) {
        this.rightClickAction = action
        dolls.forEach { doll ->
            doll.rightClicked { player ->
                action(player, doll)
            }
        }
    }

    fun leftClicked(action: (Player, DollContext) -> Unit) {
        this.leftClickAction = action
        dolls.forEach { doll ->
            doll.leftClicked { player ->
                action(player, doll)
            }
        }
    }

    fun movement(builder: CompositeMovementBuilder.() -> Unit) {
        val compositeBuilder = CompositeMovementBuilder(dolls, plugin)
        builder.invoke(compositeBuilder)
        compositeBuilder.start()
    }

    fun stopMovement() {
        dolls.forEach { it.stopMovement() }
    }

    fun tick(interval: Long = 1L, action: (Long) -> Unit) {
        DollTickerManager.getTicker(plugin).register(interval, action)
    }

    fun signal(signalName: String) {
        SignalManager.emit(signalName)
    }

    fun destroy() {
        dolls.forEach { it.destroy() }
        CompositeDollRegistry.unregister(name)
    }

    fun getDoll(dollName: String): DollContext? {
        return dolls.find { it.name == dollName }
    }

    fun getAllDolls(): List<DollContext> = dolls.toList()
}

// Composite Movement Builder
class CompositeMovementBuilder(
    private val dolls: List<DollContext>,
    private val plugin: JavaPlugin
) {
    private val dollMovements = mutableMapOf<String, MovementBuilder>()

    fun id(dollName: String, builder: MovementBuilder.() -> Unit) {
        val doll = dolls.find { it.name == dollName } ?: return
        val movementBuilder = MovementBuilder(doll, plugin)
        builder.invoke(movementBuilder)
        dollMovements[dollName] = movementBuilder
    }

    fun all(builder: MovementBuilder.() -> Unit) {
        dolls.forEach { doll ->
            val movementBuilder = MovementBuilder(doll, plugin)
            builder.invoke(movementBuilder)
            dollMovements[doll.name] = movementBuilder
        }
    }

    fun group(dollNames: List<String>, builder: MovementBuilder.() -> Unit) {
        dollNames.forEach { dollName ->
            val doll = dolls.find { it.name == dollName } ?: return@forEach
            val movementBuilder = MovementBuilder(doll, plugin)
            builder.invoke(movementBuilder)
            dollMovements[dollName] = movementBuilder
        }
    }

    internal fun start() {
        dollMovements.values.forEach { it.start() }
    }
}

// Composite Doll Builder
class CompositeDollBuilder(
    private val name: String,
    private val plugin: JavaPlugin
) {
    private val dolls = mutableListOf<DollContext>()
    private var configurator: (CompositeDoll.() -> Unit)? = null

    fun add(dollName: String, type: DollEntityType, location: Location, mode: DollMode = DollMode.FAKE): CompositeDollBuilder {
        val builder = DollBuilder(dollName, plugin)
        builder.type(type).location(location)

        when (mode) {
            DollMode.FAKE -> builder.fake
            DollMode.REALITY -> builder.reality
        }

        DollRegistry.get(dollName)?.let { dolls.add(it) }
        return this
    }

    fun addExisting(doll: DollContext): CompositeDollBuilder {
        dolls.add(doll)
        return this
    }

    fun configure(block: CompositeDoll.() -> Unit): CompositeDollBuilder {
        this.configurator = block
        return this
    }

    internal fun build(): CompositeDoll {
        val composite = CompositeDoll(name, dolls, plugin)
        configurator?.invoke(composite)
        CompositeDollRegistry.register(name, composite)
        return composite
    }
}

// Doll Registry
object DollRegistry {
    private val dolls = ConcurrentHashMap<String, DollContext>()
    private val entities = ConcurrentHashMap<String, Entity>()
    private val entityToDoll = ConcurrentHashMap<UUID, String>()
    private val fakeEntityToDoll = ConcurrentHashMap<Int, String>()

    fun register(name: String, context: DollContext) {
        dolls[name] = context
        context.realEntity?.let {
            entityToDoll[it.uniqueId] = name
        }
        context.fakeEntity?.let {
            fakeEntityToDoll[it.entityId] = name
        }
    }

    fun registerEntity(name: String, entity: Entity) {
        entities[name] = entity
        entityToDoll[entity.uniqueId] = name
    }

    fun get(name: String): DollContext? = dolls[name]

    fun getEntity(name: String): Entity? = entities[name]

    fun getByUUID(uuid: UUID): String? = entityToDoll[uuid]

    fun getByEntityId(entityId: Int): String? = fakeEntityToDoll[entityId]

    fun handleRightClick(entity: Entity, player: Player) {
        val name = entityToDoll[entity.uniqueId] ?: entity.customName ?: return
        dolls[name]?.getRightClickAction()?.invoke(player)
    }

    fun handleRightClickFake(entityId: Int, player: Player) {
        val name = fakeEntityToDoll[entityId] ?: return
        dolls[name]?.getRightClickAction()?.invoke(player)
    }

    fun handleLeftClick(entity: Entity, player: Player) {
        val name = entityToDoll[entity.uniqueId] ?: entity.customName ?: return
        val context = dolls[name] ?: return

        context.getLeftClickAction()?.invoke(player)

        if (context.attackable) {
            context.applyKnockback(player)
        }
    }

    fun handleLeftClickFake(entityId: Int, player: Player) {
        val name = fakeEntityToDoll[entityId] ?: return
        val context = dolls[name] ?: return

        context.getLeftClickAction()?.invoke(player)

        if (context.attackable) {
            context.applyKnockback(player)
        }
    }

    fun clear() {
        dolls.values.forEach { it.destroy() }
        dolls.clear()
        entities.clear()
        entityToDoll.clear()
        fakeEntityToDoll.clear()
    }
}

// Composite Doll Registry
object CompositeDollRegistry {
    private val compositeDolls = ConcurrentHashMap<String, CompositeDoll>()

    fun register(name: String, composite: CompositeDoll) {
        compositeDolls[name] = composite
    }

    fun unregister(name: String) {
        compositeDolls.remove(name)
    }

    fun get(name: String): CompositeDoll? = compositeDolls[name]

    fun clear() {
        compositeDolls.values.forEach { it.destroy() }
        compositeDolls.clear()
    }
}

// Preset Management
object DollPresets {
    private val presets = ConcurrentHashMap<String, DollContext>()

    fun save(name: String, context: DollContext) {
        presets[name] = context
    }

    fun load(name: String): DollContext? = presets[name]

    fun clear() {
        presets.clear()
    }
}

// Ticker Manager
object DollTickerManager {
    private val tickers = ConcurrentHashMap<String, DollTicker>()

    fun getTicker(plugin: JavaPlugin): DollTicker {
        return tickers.getOrPut(plugin.name) {
            DollTicker(plugin).apply { start() }
        }
    }

    fun clear() {
        tickers.values.forEach { it.clear() }
        tickers.clear()
    }
}

// Extension functions
fun JavaPlugin.set(presetName: String, block: DollContext.() -> Unit) {
    val context = DollContext(presetName, DollMode.REALITY, this)
    block.invoke(context)
    DollPresets.save(presetName, context)
}

fun JavaPlugin.loadPreset(presetName: String): DollContext? {
    return DollPresets.load(presetName)
}

fun JavaPlugin.doll(name: String): DollBuilder {
    return DollBuilder(name, this)
}

fun JavaPlugin.doll(name: String, dolls: List<DollContext>): CompositeDoll {
    val composite = CompositeDoll(name, dolls, this)
    CompositeDollRegistry.register(name, composite)
    return composite
}

fun JavaPlugin.doll(name: String, vararg dolls: DollContext): CompositeDoll {
    return doll(name, dolls.toList())
}

fun JavaPlugin.compositeDoll(name: String, builder: CompositeDollBuilder.() -> Unit): CompositeDoll {
    val compositBuilder = CompositeDollBuilder(name, this)
    builder.invoke(compositBuilder)
    return compositBuilder.build()
}

// Event Listeners
class DollEventListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onEntityRightClick(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        DollRegistry.handleRightClick(entity, event.player)
    }

    @EventHandler
    fun onEntityLeftClick(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val entity = event.entity

        DollRegistry.handleLeftClick(entity, damager)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        FakeServer.onPlayerJoin(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        FakeServer.onPlayerQuit(event.player)
    }
}

// ProtocolLib Packet Listener for fake entity interactions
class FakeEntityInteractionListener(private val plugin: JavaPlugin) {

    fun register() {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        protocolManager.addPacketListener(object : PacketAdapter(
            plugin,
            PacketType.Play.Client.USE_ENTITY
        ) {
            override fun onPacketReceiving(event: PacketEvent) {
                val packet = event.packet
                val player = event.player

                val entityId = packet.integers.read(0)
                val action = packet.entityUseActions.read(0)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    when (action) {
                        EnumWrappers.EntityUseAction.INTERACT,
                        EnumWrappers.EntityUseAction.INTERACT_AT -> {
                            DollRegistry.handleRightClickFake(entityId, player)
                        }
                        EnumWrappers.EntityUseAction.ATTACK -> {
                            DollRegistry.handleLeftClickFake(entityId, player)
                        }
                    }
                })
            }
        })

        plugin.logger.info("ProtocolLib Enabled.")
    }
}

// Plugin initialization helper
fun JavaPlugin.initDoLL() {
    server.pluginManager.registerEvents(DollEventListener(this), this)
    FakeEntityInteractionListener(this).register()
    DollTickerManager.getTicker(this)

    logger.info("[DoLL] System Enabled.")
}

// Cleanup helper
fun JavaPlugin.shutDownDoLL() {
    CompositeDollRegistry.clear()
    DollRegistry.clear()
    FakeServer.clear()
    DollPresets.clear()
    DollTickerManager.clear()
    SignalManager.clear()
    logger.info("§c[DoLL] System Disabled.")
}
