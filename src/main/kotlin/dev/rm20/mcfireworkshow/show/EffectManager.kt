package dev.rm20.mcfireworkshow.show

import Action
import ActionData
import ActionDeserializer
import Location
import ParticleEffectData
import Rotation
import SavedEffect
import Shape
import com.google.gson.GsonBuilder
import dev.rm20.mcfireworkshow.MCFireworkShow
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

class EffectManager(private val plugin: MCFireworkShow, private val showManager: ShowManager) {

    private val gson = GsonBuilder().registerTypeAdapter(Action::class.java, ActionDeserializer()).create()

    fun playEffect(data: ActionData.PlayEffectData) {
        val effectFile = File(plugin.dataFolder, "effects/${data.effectName}.json")
        if (!effectFile.exists()) {
            plugin.logger.warning("Effect file not found: ${effectFile.path}")
            return
        }

        try {
            val jsonString = effectFile.readText()
            val savedEffect = gson.fromJson(jsonString, SavedEffect::class.java)

            val baseLocation = data.location
            val facing = data.rotation ?: savedEffect.facing

            savedEffect.frames.forEach { (tickString, frame) ->
                val tick = tickString.toLongOrNull() ?: return@forEach
                object : BukkitRunnable() {
                    override fun run() {
                        frame.actions.forEach { action ->
                            val transformedAction = transformAction(action, baseLocation, facing, data.particleOverrides)
                            showManager.executeAction(transformedAction)
                        }
                    }
                }.runTaskLater(plugin, tick)
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load or play effect '${data.effectName}': ${e.message}")
            e.printStackTrace()
        }
    }


    private fun transformAction(action: Action, offset: Location, facing: String, overrides: Map<String, String>?): Action {
        // First, apply coordinate transformations
        var transformedData = transformActionData(action.data, offset, facing)

        // Then, check for and apply particle overrides
        val actionId = action.id
        if (overrides != null && actionId != null && overrides.containsKey(actionId)) {
            val newParticle = overrides[actionId]!!
            transformedData = applyParticleOverride(transformedData, newParticle)
        }

        return action.copy(data = transformedData)
    }

    private fun applyParticleOverride(data: ActionData, newParticle: String): ActionData {
        return when (data) {
            is ActionData.ParticleShapeData -> data.copy(particleType = newParticle)
            is ActionData.ParticleTextData -> data.copy(particleType = newParticle)
            is ActionData.ParticleEffectActionData -> {
                val newEffectData = when (val effect = data.effect) {
                    is ParticleEffectData.SpawnData -> effect.copy(particleType = newParticle)
                    is ParticleEffectData.BustData -> effect.copy(particleType = newParticle)
                    is ParticleEffectData.LaserData -> effect.copy(particleType = newParticle)
                    is ParticleEffectData.AreaData -> effect.copy(particleType = newParticle)
                    is ParticleEffectData.FountainData -> effect.copy(particleType = newParticle)
                }
                data.copy(effect = newEffectData)
            }
            else -> data
        }
    }

    private fun transformVector(vec: Location, facing: String): Location {
        return when (facing.lowercase()) {
            "east" -> Location(x = -vec.z, y = vec.y, z = vec.x)
            "south" -> Location(x = -vec.x, y = vec.y, z = -vec.z)
            "west" -> Location(x = vec.z, y = vec.y, z = -vec.x)
            else -> vec // north
        }
    }


    private fun transformLocation(loc: Location?, offset: Location, facing: String): Location? {
        if (loc == null) return null
        val rotated = transformVector(loc, facing)
        return Location(
            x = rotated.x + offset.x,
            y = rotated.y + offset.y,
            z = rotated.z + offset.z
        )
    }


    private fun transformRotation(rot: Rotation?, facing: String): Rotation? {
        if (rot == null) return null
        val yawOffset = when (facing.lowercase()) {
            "east" -> -90.0
            "south" -> -180.0
            "west" -> 90.0
            else -> 0.0
        }
        return rot.copy(yaw = rot.yaw + yawOffset)
    }

    private fun transformActionData(data: ActionData, offset: Location, facing: String): ActionData {
        return when (data) {
            is ActionData.FireworksData -> data.copy(location = transformLocation(data.location, offset, facing)!!)
            is ActionData.LightData -> data.copy(location = transformLocation(data.location, offset, facing)!!)
            is ActionData.EffectMasterData -> data.copy(location = transformLocation(data.location, offset, facing)!!)
            is ActionData.ParticleTextData -> data.copy(
                location = transformLocation(data.location, offset, facing)!!,
                rotation = transformRotation(data.rotation, facing)!!,
                endLocation = transformLocation(data.endLocation, offset, facing),
                endRotation = transformRotation(data.endRotation, facing)
            )
            is ActionData.DisplayTextData -> data.copy(
                location = transformLocation(data.location, offset, facing)!!,
                rotation = transformRotation(data.rotation, facing)!!,
                endLocation = transformLocation(data.endLocation, offset, facing),
                endRotation = transformRotation(data.endRotation, facing)
            )
            is ActionData.GuardianLaserData -> data.copy(endPos = transformLocation(data.endPos, offset, facing)!!)
            is ActionData.CrystalLaserData -> data.copy(endPos = transformLocation(data.endPos, offset, facing)!!)
            is ActionData.ModelData -> data.copy(
                location = transformLocation(data.location, offset, facing)!!,
                rotation = transformRotation(data.rotation, facing)!!,
                endLocation = transformLocation(data.endLocation, offset, facing),
                endRotation = transformRotation(data.endRotation, facing)
            )
            is ActionData.ParticleShapeData -> {
                val transformedShape = when (val shape = data.shape) {
                    is Shape.Line -> shape.copy(start = transformLocation(shape.start, offset, facing)!!, end = transformLocation(shape.end, offset, facing)!!)
                    is Shape.Arch -> shape.copy(start = transformLocation(shape.start, offset, facing)!!, end = transformLocation(shape.end, offset, facing)!!)
                    is Shape.Circle -> shape.copy(centre = transformLocation(shape.centre, offset, facing)!!, rotation = transformRotation(shape.rotation, facing)!!, endCentre = transformLocation(shape.endCentre, offset, facing), endRotation = transformRotation(shape.endRotation, facing))
                    is Shape.Sphere -> shape.copy(centre = transformLocation(shape.centre, offset, facing)!!, endCentre = transformLocation(shape.endCentre, offset, facing))
                    is Shape.Hemisphere -> shape.copy(centre = transformLocation(shape.centre, offset, facing)!!, rotation = transformRotation(shape.rotation, facing)!!)
                    is Shape.Cube -> shape.copy(centre = transformLocation(shape.centre, offset, facing)!!, rotation = transformRotation(shape.rotation, facing)!!, endCentre = transformLocation(shape.endCentre, offset, facing))
                }
                data.copy(shape = transformedShape)
            }
            is ActionData.ParticleEffectActionData -> {
                val transformedEffectData = when (val effect = data.effect) {
                    is ParticleEffectData.SpawnData -> effect.copy(location = transformLocation(effect.location, offset, facing)!!, delta = transformVector(effect.delta, facing))
                    is ParticleEffectData.BustData -> effect.copy(location = transformLocation(effect.location, offset, facing)!!)
                    is ParticleEffectData.LaserData -> effect.copy(start = transformLocation(effect.start, offset, facing)!!, end = transformLocation(effect.end, offset, facing)!!)
                    is ParticleEffectData.AreaData -> effect.copy(pos1 = transformLocation(effect.pos1, offset, facing)!!, pos2 = transformLocation(effect.pos2, offset, facing)!!)
                    is ParticleEffectData.FountainData -> {
                        effect.copy(
                            location = transformLocation(effect.location, offset, facing)!!,
                            velocity = transformVector(effect.velocity, facing) // Use transformVector for velocity
                        )
                    }
                }
                data.copy(effect = transformedEffectData)
            }
            is ActionData.CommandData, is ActionData.MusicData -> data
            is ActionData.PlayEffectData -> data
            else -> data
        }
    }
}