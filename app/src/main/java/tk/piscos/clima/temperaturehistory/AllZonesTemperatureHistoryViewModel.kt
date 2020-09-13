package tk.piscos.clima.temperaturehistory

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tk.piscos.clima.mqtt.MQTTClient
import com.github.mikephil.charting.utils.ColorTemplate



class AllZonesTemperatureHistoryViewModel (application: Application): AndroidViewModel(application)  {

    companion object{
        fun getColors():List<Int>{
            val colors = mutableListOf<Int>()
            for (c in ColorTemplate.COLORFUL_COLORS)
                colors.add(c)
            for (c in ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c)

            for (c in ColorTemplate.JOYFUL_COLORS)
                colors.add(c)



            for (c in ColorTemplate.LIBERTY_COLORS)
                colors.add(c)

            for (c in ColorTemplate.PASTEL_COLORS)
                colors.add(c)

            colors.add(ColorTemplate.getHoloBlue())
            return colors
        }
    }
    val zones = MutableLiveData<List<ZoneHistoryData>>()
    private val mqttClient= MQTTClient("tcp://piscos.ga:1883")
    fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            async { mqttClient.connectAsync(getApplication()) }.await()
            val history = async {
                mqttClient.getResponse<List<ZoneHistoryData>>(
                    requestTopic = "AllZonesTemperatureHistoryRequest",
                    responseTopic = "AllZonesTemperatureHistoryResponse"
                )
            }.await()
            zones.value=history
        }
    }
    fun disconnect(){
        mqttClient.disconnect()
    }


}