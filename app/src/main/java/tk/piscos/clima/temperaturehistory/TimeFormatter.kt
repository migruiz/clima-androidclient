package tk.piscos.clima.temperaturehistory

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


class TimeFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val timestamp = value.toLong() * 1000
        val d = Date(timestamp)
        val format = SimpleDateFormat("h:mm")
        format.timeZone = TimeZone.getDefault()
        return format.format(d)
    }




}