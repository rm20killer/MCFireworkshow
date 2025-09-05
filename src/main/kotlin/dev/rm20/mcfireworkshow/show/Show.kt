
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
data class SavedEffect(
    val name: String,
    val description: String,
    val facing: String, // Default orientation: north, east, south, west
    val frames: Map<String, Frame>
)
data class Show(
    val showName: String,
    val music: Music,
    val stage: Stage,
    val frames: Map<String, Frame>,
    val cameraMovements: List<CameraMovement>? = null, // Optional list of camera movements
    val particlePaths: List<ParticlePath>? = null
)
data class CameraMovement(
    val location: Location,
    val rotation: Rotation,
    val tick: Int = 0, // The tick at which this camera movement should end
    val interpolation: String = "linear" // "linear", "easeIn", "easeOut", "easeInOut" "catmull rom"
)
data class Music(
    val musicID: String,
    val Name: String,
    val Author: String
)
data class Stage(
    val name: String,
    val location: Location,
    val lights: List<Light>,
    val lasers: List<LaserPoint>,
    val effectPoints: List<EffectPoint>
)
data class Light(
    val name: String,
    val location: Location,
    val groups: List<String>
)

data class LaserPoint(
    val name: String,
    val location: Location,
    val groups: List<String>
)

data class EffectPoint(
    val name: String,
    val location: Location,
    val groups: List<String>
)
data class ParticlePath(
    val name: String,
    val particleType: String,
    var points: List<PathPoint>
)

data class PathPoint(
    val location: Location,
    val tick: Int,
    val height: Double = 0.0,
    val particleTypeOverride: String? = null,
    val curved: Int = 0
)
data class Frame(
    val actions: List<Action>
)

data class Action(
    val id: String? = null,
    val type: String,
    val data: ActionData
)

sealed class ActionData {

    data class PlayEffectData(
        val effectName: String, // The name of the file in the /effects/ folder without .json
        val location: Location,   // The world location to play the effect at
        val rotation: String? = null, // Optional: "north", "east", "south", "west" to override the effect's default facing
        val particleOverrides: Map<String, String>? = null
    ) : ActionData()

    data class FireworksData(
        val location: Location,
        val nbt: Nbt,
        val forces: Forces? = null
    ) : ActionData()

    data class CommandData(
        val command: String
    ) : ActionData()

    data class MusicData(
        val musicID: String,
        val Name: String,
        val Author: String
    ): ActionData()

    data class LightData(
        val location: Location,
        val lit: Boolean
    ): ActionData()

    data class EffectMasterData(
        val location: Location,
        val category: String,
        val name: String
    ): ActionData()

    data class ParticleTextData(
        val location: Location,
        val text: String,
        val particleType: String,
        val size: Double = 1.00,
        val duration: Int = 1,
        val rotation: Rotation,
        // Optional end state
        val endLocation: Location? = null,
        val endRotation: Rotation? = null,
        // Timing
        val delay: Int = 0, // Delay before the text starts moving
        val moveTime: Int? = null // Time to move to the end location, if specified
    ): ActionData()

    data class DisplayTextData(
        val location: Location,
        val text: String,
        val size: Double = 1.0,
        val duration: Int = 20,
        val rotation: Rotation,
        val color: String = "#FFFFFF", // Hex color code for the text
        val billboard: String = "FIXED", // Options: FIXED, VERTICAL, HORIZONTAL, CENTER
        val alignment: String = "CENTER", // Options: CENTER, LEFT, RIGHT
        val backgroundColor: String = "#00000000", // RGBA hex color
        val textOpacity: Byte = -1, // -1 to keep default, 0-255 for custom
        val shadowed: Boolean = false,
        val seeThrough: Boolean = false,
        val defaultBackground: Boolean = true,
        val lineWidth: Int = 200,
        // Optional end state
        val endLocation: Location? = null,
        val endRotation: Rotation? = null,
        // Timing
        val delay: Int = 0,
        val moveTime: Int? = null
    ) : ActionData()

    //LaserData class
    data class GuardianLaserData(
        val name: String? = null, // Optional name to identify the laser
        val group: String? = null, // Optional group to filter lasers
        val endPos: Location,
        val duration: Long, // Duration in ticks for the laser to move to the end position
        val distance: Int = 128, // Distance in blocks for the laser
        val state: String = "on", // "on" or "off"
        val relative: Boolean = false
    ) : ActionData()

    data class CrystalLaserData(
        val name: String? = null, // Optional name to identify the laser
        val group: String? = null, // Optional group to filter lasers
        val endPos: Location,
        val duration: Long, // Duration in ticks for the laser to move to the end position
        val distance: Int = 128, // Distance in blocks for the laser
        val state: String = "on", // "on" or "off"
        val relative: Boolean = false
    ) : ActionData()

    //Data class for the Item Display Model action
    data class ModelData(
        val material: String,
        val itemModel: String? = null, // Optional custom model data
        val duration: Int,
        val glowing: Boolean = false, // Whether the model should glow
        // Start state
        val location: Location,
        val scale: Location, // Using Location for x, y, z scale
        val rotation: Rotation,
        // End state (optional)
        val endLocation: Location? = null,
        val endScale: Location? = null,
        val endRotation: Rotation? = null,
        // Timing
        val delay: Int = 0,
        val moveTime: Int? = null
    ) : ActionData()

    //Data class for all particle shape actions
    data class ParticleShapeData(
        val shape: Shape,
        val particleType: String,
        val duration: Int,
        val density: Double = 1.0,
    ): ActionData()

    //Data class for particle effects
    data class ParticleEffectActionData(
        val effect: ParticleEffectData
    ) : ActionData()
    data class PlaceSchematicData(
        val schematicName: String,
        val location: Location,
        val animation: SchematicAnimation? = null // Animation is optional
    ) : ActionData()

}
data class SchematicAnimation(
    val mode: String, // e.g., "bottom_up", "top_down", "sweep_x", "random", "none"
    val duration: Int // The total time in ticks for the animation to complete
)

sealed interface ParticleEffectData {
    val particleType: String // Common property for all effects

    data class SpawnData(
        override val particleType: String,
        val location: Location,
        val amount: Int,
        val delta: Location = Location(0.0,0.0,0.0),
        val speed: Double
    ): ParticleEffectData

    data class BustData(
        override val particleType: String,
        val location: Location,
        val amount: Int = 50,
        val radius: Double = 1.0, // The radius of the cuboid area for the burst
        val scale: Location = Location(1.0, 1.0, 1.0) // Scale for the burst effect
    ) : ParticleEffectData

    data class LaserData(
        override val particleType: String,
        val start: Location,
        val end: Location,
        val duration: Int, // Duration of the widening effect in ticks
        val startWidth: Double = 0.1,
        val endWidth: Double = 1.0,
        val density: Double = 2.0 // Particles per block along the laser's length
    ) : ParticleEffectData


    data class AreaData(
        override val particleType: String,
        val pos1: Location, // First corner of the cuboid
        val pos2: Location, // Second corner of the cuboid
        val density: Double = 5.0 // Number of particles per cubic block
    ) : ParticleEffectData

    data class FountainData(
        override val particleType: String,
        val location: Location,
        val material: String, // The item material, e.g., "DIAMOND"
        val itemModel: String? = null, // Optional custom model data
        val velocity: Location, // The base velocity vector for the items
        val duration: Int = 20, // How long the fountain spawns items (in ticks)
        val amount: Int = 1, // Number of items to spawn each tick
        val lifetime: Int = 40, // How long each item exists before removal (in ticks)
        val randomizer: Double = 0.0 // Adds randomness to the velocity
    ): ParticleEffectData
}
sealed interface Shape{
    data class Line(
        val start: Location,
        val end: Location,
        val width: Double = 0.1,
        //optional properties for timing
        val delay: Int = 0,
        val moveTime: Int? = null, // Time to move to the end location, if specified
        val keepTrail: Boolean = true // If false, the drawing animation will not leave a trail
    ) : Shape
    data class Arch(
        val start: Location,
        val end: Location,
        val height: Double,
        // Optional properties for timing
        val delay: Int = 0,
        val moveTime: Int? = null, // Time to move to the end location, if specified
        val keepTrail: Boolean = true // If false, the drawing animation will not leave a trail
    ) : Shape
    data class Circle(
        val centre: Location,
        val radius: Double,
        val rotation: Rotation,
        // Optional properties for timing
        val endCentre: Location? = null,
        val endRadius: Double? = null,
        val endRotation: Rotation? = null,
        val delay: Int = 0,
        val moveTime: Int? = null, // Time to animate radius/rotation
        val drawTime: Int? = null,  // Time to animate the circumference drawing
        val keepTrail: Boolean = true // If false, the drawing animation will not leave a trail
    ) : Shape
    data class Sphere(
        val centre: Location,
        val radius: Double,
        // Optional properties for timing
        val endCentre: Location? = null,
        val delay: Int = 0,
        val endRadius: Double? = null,
        val moveTime: Int? = null
    ) : Shape

    data class Hemisphere(
        val centre: Location,
        val radius: Double,
        val rotation: Rotation,
        // Optional properties for timing
        val delay: Int = 0,
        val endRadius: Double? = null,
        val moveTime: Int? = null
    ) : Shape

    data class Cube(
        val centre: Location,
        val scale: Location, // Using Location for x, y, z scale
        val rotation: Rotation,
        // Optional properties for timing
        val endCentre: Location? = null,
        val delay: Int = 0,
        val endScale: Location? = null,
        val moveTime: Int? = null
    ) : Shape
}

data class Location(
    val x: Double,
    val y: Double,
    val z: Double
)

data class Rotation(
    val yaw: Double,
    val pitch: Double,
    val roll: Double
)

data class Nbt(
    val Fireworks: Fireworks
)

data class Fireworks(
    val Explosions: List<Explosion>
)

data class Explosion(
    val Colors: List<Int>,
    val FadeColors: List<Int>? = null,
    val Type: String,
    @SerializedName("Flicker") val flicker: Int? = null, // Optional properties
    @SerializedName("Trail") val trail: Int? = null
)

data class Forces(
    val x: Double,
    val y: Double,
    val z: Double,
    val i: Double
)


/**
 * Custom deserializer for Action objects.
 * This class handles the deserialization of JSON data into Action objects,
 * specifically addressing the different types of ActionData based on the "type" field.
 */
class ActionDeserializer : JsonDeserializer<Action> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Action {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString
        val data = jsonObject.get("data").asJsonObject
        val id = jsonObject.get("id")?.asString

        val actionData: ActionData = when (type) {
            "play_effect" -> {
                context!!.deserialize(data, ActionData.PlayEffectData::class.java)
            }
            "firework" -> {
                context!!.deserialize(data, ActionData.FireworksData::class.java)
            }
            "command" -> {
                context!!.deserialize(data, ActionData.CommandData::class.java)
            }
            "music" -> {
                context!!.deserialize(data, ActionData.MusicData::class.java)
            }
            "light" ->{
                context!!.deserialize(data, ActionData.LightData::class.java)
            }
            "effect" ->{
                context!!.deserialize(data, ActionData.EffectMasterData::class.java)
            }
            "particle_text" -> {
                context!!.deserialize(data, ActionData.ParticleTextData::class.java)
            }
            "display_text" -> {
                context!!.deserialize(data, ActionData.DisplayTextData::class.java)
            }
            "guardian_laser" -> {
                context!!.deserialize(data, ActionData.GuardianLaserData::class.java)
            }
            "crystal_laser" -> {
                context!!.deserialize(data, ActionData.CrystalLaserData::class.java)
            }
            //particle shapes!
            "particle_shape" -> {


                // Manually parse the common properties
                val particleType = data.get("particleType").asString
                val duration = data.get("duration").asInt
                val density = data.get("density")?.asDouble ?: 1.0

                // Manually parse the nested 'shape' object
                val shapeObject = data.get("shape").asJsonObject
                val shapeType = shapeObject.get("type").asString

                // Deserialize the specific shape implementation (e.g., Line)
                val shape: Shape = when (shapeType.lowercase()) {
                    "line" -> context!!.deserialize(shapeObject, Shape.Line::class.java)
                    "arch" -> context!!.deserialize(shapeObject, Shape.Arch::class.java)
                    "circle" -> context!!.deserialize(shapeObject, Shape.Circle::class.java)
                    "sphere" -> context!!.deserialize(shapeObject, Shape.Sphere::class.java)
                    "hemisphere" -> context!!.deserialize(shapeObject, Shape.Hemisphere::class.java)
                    "cube" -> context!!.deserialize(shapeObject, Shape.Cube::class.java)
                    else -> throw JsonParseException("Unknown shape type: $shapeType")
                }

                // Manually construct the final ParticleShapeData object
                ActionData.ParticleShapeData(
                    shape = shape,
                    particleType = particleType,
                    duration = duration,
                    density = density
                )

            }
            //particle effects
            "particle_effect" -> {
                val effectObject = data.get("effect").asJsonObject
                val effectType = effectObject.get("type").asString

                val effectData: ParticleEffectData = when (effectType.lowercase()) {
                    "spawn" -> context!!.deserialize(effectObject, ParticleEffectData.SpawnData::class.java)
                    "bust" -> context!!.deserialize(effectObject, ParticleEffectData.BustData::class.java)
                    "laser" -> context!!.deserialize(effectObject, ParticleEffectData.LaserData::class.java)
                    "area" -> context!!.deserialize(effectObject, ParticleEffectData.AreaData::class.java)
                    "fountain" -> context!!.deserialize(effectObject, ParticleEffectData.FountainData::class.java)
                    else -> throw JsonParseException("Unknown particle effect type: $effectType")
                }
                ActionData.ParticleEffectActionData(effectData)
            }
            "model" ->
            {
                context!!.deserialize(data, ActionData.ModelData::class.java)
            }

            "place_schematic" -> {
                context!!.deserialize(data, ActionData.PlaceSchematicData::class.java)
            }
            else -> throw JsonParseException("Unknown action type: $type")

        }

        return Action(id,type, actionData)
    }
}