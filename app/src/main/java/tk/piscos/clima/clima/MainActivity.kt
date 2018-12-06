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

class MainActivity : AppCompatActivity() {

    private val mqttClient= MQTTClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_zones.setHasFixedSize(true)
        rv_zones.layoutManager = LinearLayoutManager(this)
        var zonesAdapter = ZonesAdapter()
        rv_zones.adapter = zonesAdapter
        connectToMQTT(zonesAdapter)
    }

    private fun connectToMQTT(zonesAdapter: ZonesAdapter) {
        GlobalScope.launch(Dispatchers.Main) {
            async { mqttClient.connectAsync(this@MainActivity) }.await()
            val list = async { mqttClient.getZonesSummary() }.await()
            zonesAdapter.updateElements(list)
            async {
                mqttClient.listenForZonesChange {
                    zonesAdapter.updateElement(it)
                }
            }.await()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch(Dispatchers.Main) {
            GlobalScope.async { mqttClient.disconnect() }.await()
        }

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
            val existingZone = elements.filter { it.zoneCode!!.compareTo(updatedZone.zoneCode!!,ignoreCase = true)==1 }.firstOrNull()
            existingZone?.let {
                existingZone.coverage = updatedZone.coverage
                existingZone.humidity = updatedZone.humidity
                existingZone.temperature = updatedZone.temperature
                this.notifyItemChanged(elements.indexOf(existingZone))
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) =
            ZoneItemViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.zones_summary_cell, viewGroup, false))


        override fun onBindViewHolder(holder: ZoneItemViewHolder, position: Int) = holder.bind(elements[position])

        override fun getItemCount()=elements.size
    }

}
