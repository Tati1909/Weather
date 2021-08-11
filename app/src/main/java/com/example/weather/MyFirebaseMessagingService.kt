package com.example.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //Наш главный метод — onMessageReceived вызывается каждый раз, когда приходит уведомление
    //и приложение открыто. С его помощью мы проверяем, пришли ли какие-то данные в уведомлении.
    //Они приходят в строковом формате по аналогии с JSON. Однако мы, как и в случае с погодным
    //сервером, не знаем заранее, что может прийти. Поэтому всё нужно проверять на null.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val remoteMessageData = remoteMessage.data
        if (remoteMessageData.isNotEmpty()) {
            handleDataMessage(remoteMessageData.toMap())
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data[PUSH_KEY_TITLE]
        val message = data[PUSH_KEY_MESSAGE]
        if (!title.isNullOrBlank() && !message.isNullOrBlank()) {
            showNotification(title, message)
        }
    }

    //Чтобы сформировать уведомление, которое мы хотим отобразить, нужно
    //воспользоваться NotificationBuilder. При создании уведомления мы передаём в
    //NotificationBuilder иконку, заголовок, текст и приоритет (нужен для устройств версии 7.1 и ниже)
    //или важность (для устройств версии 8 и выше).
    private fun showNotification(title: String, message: String) {
        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
                //Обратите внимание, что помимо контекста
                //    //NotificationCompat.Builder принимает ID канала. Это нужно для устройств версии 8 и выше.
                //    //Более старыми девайсами этот параметр игнорируется. С этой же целью мы проверяем версию
                //    //операционной системы. Если она выше 26 (версия O), создаём канал. Проверка необходима, потому
                //    //что в старых версиях SDK нет такого класса.
                setSmallIcon(R.drawable.ic_firebase_logo)
                setContentTitle(title)
                setContentText(message)
                priority = NotificationCompat.PRIORITY_DEFAULT
            }
        //Чтобы показать уведомление, нам нужно воспользоваться системным сервисом NotificationManager.
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        notificationManager:
        NotificationManager
    ) {
        val name = "Channel name"
        val descriptionText = "Channel description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    //Метод onNewToken получает токен, который
    //нужен серверу для рассылки индивидуальных уведомлений. Этот метод вызывается один раз в
    //начале работы приложения. Его можно вызвать повторно только при переустановке приложения,
    //потому что токен у смартфона всегда только один.

    override fun onNewToken(token: String) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        Log.wtf("@@@", token)
        super.onNewToken(token)
    }

    companion object {
        //эти ключи вводим в firebase
        private const val PUSH_KEY_TITLE = "title"
        private const val PUSH_KEY_MESSAGE = "message"
        private const val CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_ID = 37
    }
}