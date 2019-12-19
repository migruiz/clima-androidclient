package tk.piscos.clima.temperaturehistory

data class ZoneHistoryData (val zoneCode:String,val order:Int, val history:List<HistoryEntryData>)