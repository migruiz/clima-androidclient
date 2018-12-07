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
    val lastUpdatedZone = MutableLiveData<ZoneCellModel>()




    private val mqttClient= MQTTClient("tcp://piscos.tk:1883")
    fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            async { mqttClient.connectAsync(getApplication()) }.await()
            val zonesClimatelist = async {
                mqttClient.getResponse<List<ZoneClimateData>>(
                    requestTopic = "AllZonesReadingsRequest",
                    responseTopic = "AllZonesReadingResponse"
                )
            }.await()
            val zonesBoilerlist = async {
                mqttClient.getResponse<List<ZoneBoilerData>>(
                    requestTopic = "AllZonesConfigRequest",
                    responseTopic = "AllZonesConfigResponse"
                )
            }.await()
            val modelList = zonesClimatelist.map {
                ZoneCellModel(
                    temperature = it.temperature,
                    humidity = it.humidity,
                    coverage = it.coverage,
                    zoneCode = it.zoneCode
                )
            }
            zonesBoilerlist.forEach {
                val zoneCellModel=modelList.first{a-> a.zoneCode.equals(it.zoneCode,true)}
                zoneCellModel.regulated=it.regulated
                zoneCellModel.targetTemperature=it.targetTemperature
            }

            zones.value = modelList
            async {
                mqttClient.subscribe<ZoneClimateData>("zoneClimateChange") {
                    val zoneModel=this@ZonesViewModel.zones.value!!.firstOrNull{a-> a.zoneCode.equals(it.zoneCode,true)}
                    if (zoneModel!=null) {
                        zoneModel.temperature = it.temperature
                        zoneModel.coverage = it.coverage
                        zoneModel.humidity = it.humidity
                        this@ZonesViewModel.lastUpdatedZone.value = zoneModel
                    }
                }
            }.await()
        }
    }
    fun disconnect(){
        mqttClient.disconnect()
    }

}