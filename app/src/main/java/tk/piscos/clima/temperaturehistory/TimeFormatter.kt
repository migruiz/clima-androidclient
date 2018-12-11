package tk.piscos.clima.temperaturehistory

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*


class TimeFormatter : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        // "value" represents the position of the label on the axis (x or y)
        val timestamp = value.toLong() * 1000
        val d = Date(timestamp)
        val format = SimpleDateFormat("h:mm")
        format.timeZone = TimeZone.getDefault()
        return format.format(d)
    }


}