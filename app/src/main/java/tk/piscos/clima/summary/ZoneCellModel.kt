package tk.piscos.clima.summary

class ZoneCellModel(var temperature:Double,var  humidity:Int,var  coverage:String?=null,val  zoneCode:String, var regulated:Boolean=false, var targetTemperature:Double?=null)