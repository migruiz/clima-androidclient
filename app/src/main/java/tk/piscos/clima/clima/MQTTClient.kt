package tk.piscos.clima.clima

import android.content.Context
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import toJson
import toJsonObject
import java.util.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MQTTClient{
    lateinit var mqttAndroidClient:MqttAndroidClient
    suspend fun  connectAsync(context: Context):Unit = suspendCoroutine { cont ->

        val serverUri = "tcp://piscos.tk:1883"

        var clientId = "ALEA"
        val subscriptionTopic = "sensor/+"

        val username = "xzxzjowz"
        val password = "ci5ejcSD1YnD"
        clientId += System.currentTimeMillis()
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.userName = username
        mqttConnectOptions.password = password.toCharArray()
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

    private val topicListeners = HashMap<String, (String)->Unit>()

    suspend fun getZonesSummary():List<ZoneCellModel> = suspendCoroutine { cont ->
        val responseTopicName="AllZonesReadingResponse"
        var requestTopicName="AllZonesReadingsRequest"
        val message = MqttMessage()
        message.payload = "request".toJson().toByteArray()
        mqttAndroidClient.subscribe(responseTopicName, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                topicListeners[responseTopicName] = {
                    val result:List<ZoneCellModel> = it.toJsonObject()
                    mqttAndroidClient.unsubscribe(responseTopicName)
                    topicListeners.remove(responseTopicName)
                    cont.resume(result)
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {

                cont.resumeWithException(exception)
            }
        })

        mqttAndroidClient.publish(requestTopicName, message)
    }
}