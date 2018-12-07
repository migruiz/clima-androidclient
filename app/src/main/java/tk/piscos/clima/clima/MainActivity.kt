package tk.piscos.clima.clima

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.zones_summary_cell.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

class MainActivity : AppCompatActivity() {

    private val mqttClient= MQTTClient("tcp://piscos.tk:1883")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this,  Crashlytics());
        setContentView(R.layout.activity_main)
        rv_zones.setHasFixedSize(true)
        rv_zones.layoutManager = LinearLayoutManager(this)
        rv_zones.adapter = ZonesAdapter()

    }

    private fun connectToMQTT(zonesAdapter: ZonesAdapter) {
        GlobalScope.launch(Dispatchers.Main) {
            async { mqttClient.connectAsync(this@MainActivity) }.await()
            val list = async { mqttClient.getResponse<List<ZoneCellModel>>(requestTopic = "AllZonesReadingRequest",responseTopic = "AllZonesReadingResponse") }.await()
            zonesAdapter.updateElements(list)
            async {
                mqttClient.subscribe("ZoneClimateChange") {
                    zonesAdapter.updateElement(it)
                }
            }.await()
        }
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()
        mqttClient.disconnect()
    }

    override fun onStart() {
        // call the superclass method first
        super.onStart()
        connectToMQTT(rv_zones.adapter as ZonesAdapter)
    }
    override fun onDestroy() {
        super.onDestroy()


    }


    private inner class ZoneItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item:ZoneCellModel)= with(itemView){
            zonecode.text=item.zoneCode
            temperature.text=item.temperature.toString()
            coverage.text=item.coverage
            humidity.text=item.humidity.toString()

        }
    }
    private inner class ZonesAdapter:RecyclerView.Adapter<ZoneItemViewHolder>() {
        private var elements: MutableList<ZoneCellModel> = arrayListOf()
        fun updateElements(newDetails: List<ZoneCellModel>) {
            elements.clear()
            elements.addAll(newDetails)
            notifyDataSetChanged()
        }
        fun updateElement(updatedZone:ZoneCellModel){
            val existingZone = elements.firstOrNull { it.zoneCode.equals(updatedZone.zoneCode,ignoreCase = true) }
            existingZone?.let {
                it.coverage = updatedZone.coverage
                it.humidity = updatedZone.humidity
                it.temperature = updatedZone.temperature
                this.notifyItemChanged(elements.indexOf(it))
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) =
            ZoneItemViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.zones_summary_cell, viewGroup, false))


        override fun onBindViewHolder(holder: ZoneItemViewHolder, position: Int) = holder.bind(elements[position])

        override fun getItemCount()=elements.size
    }

}
