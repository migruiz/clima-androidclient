package tk.piscos.clima.clima

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ZonesViewModel(application: Application): AndroidViewModel(application)  {

    val zones = MutableLiveData<List<ZoneCellModel>>()
    val updatedZone = MutableLiveData<ZoneCellModel>()




    private val mqttClient= MQTTClient("tcp://piscos.tk:1883")
    fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            async { mqttClient.connectAsync(getApplication()) }.await()
            val list = async {
                mqttClient.getResponse<List<ZoneCellModel>>(
                    requestTopic = "AllZonesReadingsRequest",
                    responseTopic = "AllZonesReadingResponse"
                )
            }.await()
            zones.value = list
            async {
                mqttClient.subscribe("zoneClimateChange") {
                    updatedZone.value = it
                }
            }.await()
        }
    }
    fun disconnect(){
        mqttClient.disconnect()
    }

}