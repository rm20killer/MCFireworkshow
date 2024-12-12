import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.*
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
}

data class Location(
    val x: Int,
    val y: Int,
    val z: Int
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
    val x: Int,
    val y: Int,
    val z: Int
)

data class CommandData(
    val command: String
)


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
            else -> throw JsonParseException("Unknown action type: $type")
        }

        return Action(type, actionData)
    }
}