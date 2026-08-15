package com.example.weatherapp.monitor

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherapp.model.City
import java.util.concurrent.TimeUnit

class ForecastMonitor(context: Context) {
    private val wm = WorkManager.getInstance(context)
    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun updateCity(city: City) {
        Log.d("ForecastMonitor", "updateCity chamado: ${city.name}, monitored=${city.isMonitored}")
        cancelCity(city)

        if (!city.isMonitored) return

        val inputData = Data.Builder().putString("city", city.name).build()
        val request = PeriodicWorkRequestBuilder<ForecastWorker>(
            repeatInterval = 15, repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).setInitialDelay(
            duration = 10, timeUnit = TimeUnit.SECONDS
        ).setInputData(inputData).build()

        wm.enqueueUniquePeriodicWork(
            city.name,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request
        )
        Log.d("ForecastMonitor", "Worker agendado para ${city.name}")
    }

    fun cancelCity(city: City) {
        wm.cancelUniqueWork(city.name)
        nm.cancel(city.name.hashCode())
    }

    fun cancelAll() {
        wm.cancelAllWork()
        nm.cancelAll()
    }
}