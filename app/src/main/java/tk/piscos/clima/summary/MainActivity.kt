package tk.piscos.clima.summary
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
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
import tk.piscos.clima.temperaturehistory.HistoryActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private val model: ZonesViewModel get() = getViewModel()
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
        observe(model.lastUpdatedZone){
            adapter.updateElement(it)
        }
        observe(model.boilerValves){
            upValve.setBackgroundColor(if (it.upstairs) Color.RED else Color.WHITE)
            downValve.setBackgroundColor(if (it.downstairs) Color.RED else Color.WHITE)
            hotwaterValve.setBackgroundColor(if (it.hotwater) Color.RED else Color.WHITE)
            testValve.setBackgroundColor(if (it.test) Color.RED else Color.WHITE)
        }
        hotwaterValve.setOnClickListener {
            model.turnOnHotwater()
        }
        testValve.setOnClickListener {
            model.turnOnOffTestValve()
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


        fun bind(item: ZoneCellModel)= with(itemView){

            setOnLongClickListener {
                val myIntent = Intent(context, HistoryActivity::class.java)
                context.startActivity(myIntent)
                return@setOnLongClickListener true
            }

            val checkListener ={_:CompoundButton,value:Boolean -> model.regulateZone(item.zoneCode,value)}
            zonecode.text=item.zoneCode
            temperature.text=item.temperature.toString()
            coverage.text=item.coverage
            humidity.text=item.humidity.toString()
            regulateSwitch.setOnCheckedChangeListener(null)
            regulateSwitch.isChecked=item.regulated
            regulateSwitch.setOnCheckedChangeListener(null)
            targetTemperature.text=item.targetTemperature.toString()
            regulateSwitch.setOnCheckedChangeListener(checkListener)
            targetTemperature.setOnClickListener {
                val builder = AlertDialog.Builder(it.context)
                val inflater = it.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val theView = inflater.inflate(R.layout.temperature_dialog_picker_layout, null)

                val intTemp = theView.findViewById(R.id.int_picker) as NumberPicker
                val decTemp = theView.findViewById(R.id.decimal_picker) as NumberPicker
                builder.setView(theView).setPositiveButton("Target") { _, _ ->
                    var temperature = (intTemp.value + decTemp.value * 0.1f).toDouble()
                    temperature=Math.round(temperature*10.0)/10.0
                    model.setTargetTemperature(item.zoneCode,temperature)
                }.setNegativeButton("Cancel", null)
                intTemp.minValue = 0
                intTemp.maxValue = 25
                decTemp.minValue = 0
                decTemp.maxValue = 9

                var targetTemperature=if(item.targetTemperature==null)20.0 else item.targetTemperature
                val twoDForm = DecimalFormat("#.#")
                targetTemperature = java.lang.Double.valueOf(twoDForm.format(targetTemperature))

                val intTempPart = targetTemperature.toInt()
                val delta = java.lang.Double.valueOf(twoDForm.format(targetTemperature - targetTemperature.toInt()))
                val intPartDec = (delta * 10).toInt()
                intTemp.value = intTempPart
                decTemp.value = intPartDec


                builder.show()

            }


        }
    }
    private inner class ZonesAdapter:RecyclerView.Adapter<ZoneItemViewHolder>() {
        private var elements: MutableList<ZoneCellModel> = arrayListOf()
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


        override fun onBindViewHolder(holder: ZoneItemViewHolder, position: Int) = holder.bind(elements[position])

        override fun getItemCount()=elements.size
    }

}
