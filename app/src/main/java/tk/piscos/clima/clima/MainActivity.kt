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
import com.crashlytics.android.Crashlytics;
import getViewModel
import io.fabric.sdk.android.Fabric;
import observe

class MainActivity : AppCompatActivity() {

    private val model:ZonesViewModel get() = getViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this,  Crashlytics())
        setContentView(R.layout.activity_main)
        rv_zones.setHasFixedSize(true)
        rv_zones.layoutManager = LinearLayoutManager(this)
        val adapter=ZonesAdapter()
        rv_zones.adapter = adapter
        observe(model.zones){
            adapter.updateElements(it)
        }
        observe(model.updatedZoneClimateData){
            adapter.updateElement(it)
        }

    }



    override fun onStop() {
        super.onStop()
        model.disconnect()
    }

    override fun onStart() {
        super.onStart()
        model.fetchData()
    }



    private inner class ZoneItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item:ZoneCellModel)= with(itemView){
            zonecode.text=item.zoneCode
            temperature.text=item.temperature.toString()
            coverage.text=item.coverage
            humidity.text=item.humidity.toString()
            regulateSwitch.isChecked=item.regulated
            targetTemperature.text=item.targetTemperature.toString()

        }
    }
    private inner class ZonesAdapter:RecyclerView.Adapter<ZoneItemViewHolder>() {
        private var elements: MutableList<ZoneCellModel> = arrayListOf()
        fun updateElements(newDetails: List<ZoneCellModel>) {
            elements.clear()
            elements.addAll(newDetails)
            notifyDataSetChanged()
        }
        fun updateElement(updatedZone:ZoneClimateData){
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
