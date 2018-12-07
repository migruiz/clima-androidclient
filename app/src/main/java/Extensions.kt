import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.FragmentActivity
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
inline  fun <reified T : ViewModel> FragmentActivity.getViewModel():T{
   return ViewModelProviders.of(this)[T::class.java]
}
fun <T> LifecycleOwner.observe(data: LiveData<T>, onChange:(T)->Unit){
    data.observe(this, Observer{onChange(it!!)})
}