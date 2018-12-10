package tk.piscos.clima.summary

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class ZonesViewModel(application: Application): AndroidViewModel(application)  {

    val zones = MutableLiveData<List<ZoneCellModel>>()
    val lastUpdatedZone = MutableLiveData<ZoneCellModel>()
    val boilerValves=MutableLiveData<BoilerValvesData>()






    private val mqttClient= MQTTClient("tcp://piscos.tk:1883")
    fun regulateZone(zoneCode:String,value:Boolean) {
        val request = hashMapOf("Monitored" to value)
        mqttClient.publish("zoneIsMonitored/$zoneCode", request)
    }
    fun setTargetTemperature(zoneCode:String,value:Double) {
        val request = hashMapOf("temperature" to value)
        mqttClient.publish("zoneLowestAllowedTemperature/$zoneCode", request)
    }
    fun turnOnHotwater() {
        mqttClient.publish("HotWaterValve/turn", !boilerValves.value!!.hotwater)
    }
    fun turnOnOffTestValve() {
        mqttClient.publish("testValve/turn", !boilerValves.value!!.test)
    }

    fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            val  modelList = getModel()
            zones.value = modelList
            val boilerValvesData = async {
                mqttClient.getResponse<BoilerValvesData>(
                    requestTopic = "AllBoilerValvesStateRequest",
                    responseTopic = "AllBoilerValvesStateResponse"
                )
            }.await()
            boilerValves.value=boilerValvesData
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
        return  modelList
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
        async {
            mqttClient.subscribe<ZoneBoilerData>("zoneBoilerChange") {
                val zoneModel =
                    this@ZonesViewModel.zones.value!!.firstOrNull { a -> a.zoneCode.equals(it.zoneCode, true) }
                if (zoneModel != null) {
                    zoneModel.targetTemperature = it.targetTemperature
                    zoneModel.regulated = it.regulated
                    this@ZonesViewModel.lastUpdatedZone.value = zoneModel
                }
            }
        }.await()

        async {
            mqttClient.subscribe<BoilerValvesData>("AllBoilerValvesStateResponse") {
                    this@ZonesViewModel.boilerValves.value = it
            }
        }.await()

    }

    fun disconnect(){
        mqttClient.disconnect()
    }

}