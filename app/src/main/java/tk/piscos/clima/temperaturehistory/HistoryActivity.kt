package tk.piscos.clima.temperaturehistory

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import getViewModel
import kotlinx.android.synthetic.main.activity_history.*
import tk.piscos.clima.clima.R
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import observe
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import tk.piscos.clima.clima.R.id.temperature
import java.nio.file.Files.size






class HistoryActivity : AppCompatActivity() {

    private val model: AllZonesTemperatureHistoryViewModel get() = getViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        val chart=chartHistory
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.isHighlightFullBarEnabled = false

        // draw bars behind lines
        chart.drawOrder = arrayOf(
            CombinedChart.DrawOrder.BAR,
            CombinedChart.DrawOrder.BUBBLE,
            CombinedChart.DrawOrder.CANDLE,
            CombinedChart.DrawOrder.LINE,
            CombinedChart.DrawOrder.SCATTER
        )

        val l = chart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.textColor = Color.WHITE
        chart.xAxis.setDrawGridLines(true)
        val rightAxis = chart.getAxisRight()
        rightAxis.setDrawLabels(false)
        rightAxis.setDrawAxisLine(false)
        rightAxis.textColor = Color.WHITE
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis = chart.getAxisLeft()
        leftAxis.textColor = Color.WHITE
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        //xAxis.setAxisMinimum(0f);

        xAxis.axisMaximum = (System.currentTimeMillis() / 1000 + 60 * 10 * 2).toFloat()
        xAxis.valueFormatter = TimeFormatter()
        xAxis.granularity = 1f
        xAxis.textColor = Color.WHITE
        //_chart.setViewPortOffsets(System.currentTimeMillis()/1000-60*60*5,23,System.currentTimeMillis()/1000+60*10*2,18);
        chart.zoom(4f, 5f, (System.currentTimeMillis() / 1000 - 60 * 60 * 2).toFloat(), 21F, YAxis.AxisDependency.LEFT)

        val combinedData = CombinedData()
        val lineData = LineData()
        var colors=AllZonesTemperatureHistoryViewModel.getColors()
        observe(model.zones){li->
            val ordered = li.sortedByDescending { it.order }
            ordered.forEach {
                lineData.addDataSet(CreateLineData(it.zoneCode,it.history,colors[ordered.indexOf(it)]))
            }
            combinedData.setData(lineData)
            chart.data = combinedData
            chart.zoom(1.05f,1.01f, (System.currentTimeMillis()/1000-60*60*2).toFloat(), 21F, YAxis.AxisDependency.LEFT);
            chart.invalidate()
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


    private fun CreateLineData(name: String, historyEntries: List<HistoryEntryData>, color: Int): LineDataSet {
        val entries = if (historyEntries.any())  getLineEntries(historyEntries) else listOf<Entry>()
        val dataSet = LineDataSet(entries, name)

        dataSet.setDrawValues(true)
        //dataSet.valueFormatter = TemperatureFormatter()
        dataSet.setDrawCircleHole(false)
        dataSet.circleRadius = 1.5f
        dataSet.setCircleColor(color)
        dataSet.setCircleColorHole(color)
        dataSet.mode = LineDataSet.Mode.LINEAR
        dataSet.valueTextSize = 9f
        dataSet.valueTextColor = color
        dataSet.color = color
        dataSet.lineWidth = 1f


        return dataSet

    }

    private fun getLineEntries(historyEntries: List<HistoryEntryData>): MutableList<Entry> {
        val entries = mutableListOf<Entry>()
        val firstEntry = historyEntries[0]
        val lastEntry = historyEntries[historyEntries.size - 1]
        var maxEntry = historyEntries[0]
        var minEntry = historyEntries[0]
        for (historyEntry in historyEntries) {
            if (historyEntry.temperature > maxEntry.temperature) {
                maxEntry = historyEntry
            }
            if (historyEntry.temperature < minEntry.temperature) {
                minEntry = historyEntry
            }
        }
        for (historyEntry in historyEntries) {
            val entry = Entry(historyEntry.timestamp.toFloat(), historyEntry.temperature)
            entry.data = true
            entries.add(entry)
        }
        return entries
    }

}
