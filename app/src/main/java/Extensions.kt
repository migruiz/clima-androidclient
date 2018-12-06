import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun <T> T.toJson() : String {
    val gson= Gson()
    return gson.toJson(this)
}
inline fun <reified T> String.toJsonObject():T{
    val gson= Gson()
    return gson.fromJson(this,object: TypeToken<T>() {}.type)
}