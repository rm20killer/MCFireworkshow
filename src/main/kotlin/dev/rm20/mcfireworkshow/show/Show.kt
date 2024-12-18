
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class Show(
    val showName: String,
    val music: Music,
    val frames: Map<String, Frame>
)

data class Music(
    val musicID: String,
    val Name: String,
    val Author: String
)
data class Frame(
    val actions: List<Action>
)

data class Action(
    val type: String,
    val data: ActionData
)

sealed class ActionData {

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
}

data class Location(
    val x: Double,
    val y: Double,
    val z: Double
)

data class Nbt(
    val Fireworks: Fireworks
)

data class Fireworks(
    val Explosions: List<Explosion>
)

data class Explosion(
    val Colors: List<Int>,
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

        val actionData: ActionData = when (type) {
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
            else -> throw JsonParseException("Unknown action type: $type")

        }

        return Action(type, actionData)
    }
}