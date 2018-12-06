package tk.piscos.clima.clima

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MQTTClient{
    fun connect(context:Context){
        val serverUri = "tcp://piscos.tk:1883"

        var clientId = "ALEA"
        val subscriptionTopic = "sensor/+"

        val username = "xzxzjowz"
        val password = "ci5ejcSD1YnD"
        clientId += System.currentTimeMillis()
        val mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
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

            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                //throw new RuntimeException(exception);
            }
        })
    }
}