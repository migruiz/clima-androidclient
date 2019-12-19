package tk.piscos.clima.summary
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.NumberPicker
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.zones_summary_cell.view.*
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessaging
import getViewModel
import io.fabric.sdk.android.Fabric;
import observe
import tk.piscos.clima.clima.R
import tk.piscos.clima.temperaturehistory.AllZonesTemperatureHistoryViewModel
import tk.piscos.clima.temperaturehistory.HistoryActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private val model: ZonesViewModel get() = getViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this,  Crashlytics())
        setContentView(R.layout.activity_main)
        rv_zones.setHasFixedSize(true)
        rv_zones.layoutManager = GridLayoutManager(this, 2)



        val adapter=ZonesAdapter()
        rv_zones.adapter = adapter
        observe(model.zones){
            adapter.updateElements(it)
        }
        observe(model.lastUpdatedZone){
            adapter.updateElement(it)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("zonesalerts")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val msg = "failed subscription"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
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


        fun bind(item: ZoneCellModel,colorResId: Int)= with(itemView){

            setOnLongClickListener {
                val myIntent = Intent(context, HistoryActivity::class.java)
                context.startActivity(myIntent)
                return@setOnLongClickListener true
            }


            zonecode.text=item.zoneCode
            zonecode.setTextColor(colorResId)
            temperature.text=item.temperature.toString()
            coverage.text=item.coverage
            humidity.text=item.humidity.toString()



        }
    }
    private inner class ZonesAdapter:RecyclerView.Adapter<ZoneItemViewHolder>() {
        private var elements: MutableList<ZoneCellModel> = arrayListOf()
        private val colors = AllZonesTemperatureHistoryViewModel.getColors()
        fun updateElements(newDetails: List<ZoneCellModel>) {
            elements.clear()
            elements.addAll(newDetails)
            notifyDataSetChanged()
        }
        fun updateElement(updatedZone: ZoneCellModel){
            val existingZone = elements.first { it.zoneCode.equals(updatedZone.zoneCode,ignoreCase = true) }
            this.notifyItemChanged(elements.indexOf(existingZone))
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) =
            ZoneItemViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.zones_summary_cell, viewGroup, false))


        override fun onBindViewHolder(holder: ZoneItemViewHolder, position: Int) = holder.bind(elements[position],colors[position])

        override fun getItemCount()=elements.size
    }

}
