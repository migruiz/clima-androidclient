package tk.piscos.clima.temperaturehistory

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tk.piscos.clima.mqtt.MQTTClient
import tk.piscos.clima.summary.BoilerValvesData

class AllZonesTemperatureHistoryViewModel (application: Application): AndroidViewModel(application)  {

    val zones = MutableLiveData<List<ZoneHistoryData>>()
    private val mqttClient= MQTTClient("tcp://piscos.tk:1883")
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
}