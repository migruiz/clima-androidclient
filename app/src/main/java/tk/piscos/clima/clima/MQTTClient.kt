package tk.piscos.clima.clima

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import toJson
import toJsonObject
import java.util.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MQTTClient(val serverURI:String){
    lateinit var mqttAndroidClient:MqttAndroidClient
    suspend fun  connectAsync(context: Context):Unit = suspendCoroutine { cont ->

        var clientId = "ALEJANDRO"
        clientId += System.currentTimeMillis()
        mqttAndroidClient = MqttAndroidClient(context, serverURI, clientId)
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {


            }

            override fun connectionLost(cause: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                 topicListeners[topic]!!(String(message.payload))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {

            }
        })
        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                cont.resume(Unit)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                cont.resumeWithException(exception)
            }
        })
    }

    fun disconnect(){
        topicListeners.clear()
        val callback= object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {

            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {

            }
        }

        mqttAndroidClient.unregisterResources()
        mqttAndroidClient.close()
        mqttAndroidClient.disconnect(null,callback)

    }

    val topicListeners = HashMap<String, (String)->Unit>()

    suspend inline  fun  <reified T>subscribe(topic:String, crossinline lambda:(T)->Unit):Unit = suspendCoroutine { cont ->
        mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                topicListeners[topic] = {
                    val result:T = it.toJsonObject()
                    lambda(result)
                }
                cont.resume(Unit)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {

                cont.resumeWithException(exception)
            }
        })
    }

    fun <T> publish(topic: String,data:T){
        val message = MqttMessage()
        message.payload = data.toJson().toByteArray()
        mqttAndroidClient.publish(topic, message)
    }

    suspend inline fun <reified T>getResponse(requestTopic: String, responseTopic:String)=getResponse<T,String>(requestTopic,responseTopic,"request")

    suspend inline fun <reified U, V >getResponse(requestTopic: String, responseTopic:String,request:V):U = suspendCoroutine { cont ->
        val message = MqttMessage()
        message.payload = request.toJson().toByteArray()
        mqttAndroidClient.subscribe(responseTopic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                topicListeners[responseTopic] = {
                    val result:U= it.toJsonObject()
                    mqttAndroidClient.unsubscribe(responseTopic)
                    topicListeners.remove(responseTopic)
                    cont.resume(result)
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {

                cont.resumeWithException(exception)
            }
        })

        mqttAndroidClient.publish(requestTopic, message)
    }
}