package com.zybooks.countdowntimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.zybooks.countdowntimer.ui.TimerScreen
import com.zybooks.countdowntimer.ui.TimerViewModel
import com.zybooks.countdowntimer.ui.theme.CountdownTimerTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

   private val permissionRequestLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
         val message = if (isGranted) "Permission granted" else "Permission NOT granted"
         Log.i("MainActivity", message)
      }
   private val timerViewModel = TimerViewModel()

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      setContent {
         CountdownTimerTheme(dynamicColor = false) {
            Surface(
               modifier = Modifier.fillMaxSize(),
               color = MaterialTheme.colorScheme.background
            ) {
               TimerScreen(timerViewModel = timerViewModel)
            }
         }
      }
      // Only need permission to post notifications on Tiramisu and above
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         if (ActivityCompat.checkSelfPermission(this,
               Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
         }
      }
   }

   override fun onStop() {
      super.onStop()

      // Start TimerWorker if the timer is running
      if (timerViewModel.isRunning) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                  Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
               startWorker(timerViewModel.remainingMillis)
            }
         } else {
            startWorker(timerViewModel.remainingMillis)
         }
      }
   }

   private fun startWorker(millisRemain: Long) {
      val timerWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
         .setInputData(
            workDataOf(
               KEY_MILLIS_REMAINING to millisRemain
            )
         ).build()

      WorkManager.getInstance(applicationContext).enqueue(timerWorkRequest)
   }
}
