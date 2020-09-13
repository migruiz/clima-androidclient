package tk.piscos.clima.summary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import tk.piscos.clima.mqtt.MQTTClient

class ZonesViewModel(application: Application): AndroidViewModel(application)  {

    val zones = MutableLiveData<List<ZoneCellModel>>()
    val lastUpdatedZone = MutableLiveData<ZoneCellModel>()






    private val mqttClient= MQTTClient("tcp://piscos.ga:1883")



    fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            val  modelList = getModel()
            zones.value = modelList
            subscribeToChanges()
        }
    }

    private suspend fun CoroutineScope.getModel(): List<ZoneCellModel> {
        async { mqttClient.connectAsync(getApplication()) }.await()
        val zonesClimatelist = async {
            mqttClient.getResponse<List<ZoneClimateData>>(
                requestTopic = "AllZonesReadingsRequest",
                responseTopic = "AllZonesReadingResponse"
            )
        }.await()
        return zonesClimatelist
            .sortedByDescending { it.order }
            .map {
            ZoneCellModel(
                    temperature = it.temperature,
                    humidity = it.humidity,
                    coverage = it.coverage,
                    zoneCode = it.zoneCode
            )
        }
    }

    private suspend fun CoroutineScope.subscribeToChanges() {
        async {
            mqttClient.subscribe<ZoneClimateData>("zoneClimateChange") {
                val zoneModel =
                    this@ZonesViewModel.zones.value!!.firstOrNull { a -> a.zoneCode.equals(it.zoneCode, true) }
                if (zoneModel != null) {
                    zoneModel.temperature = it.temperature
                    zoneModel.coverage = it.coverage
                    zoneModel.humidity = it.humidity
                    this@ZonesViewModel.lastUpdatedZone.value = zoneModel
                }
            }
        }.await()

    }

    fun disconnect(){
        mqttClient.disconnect()
    }

}