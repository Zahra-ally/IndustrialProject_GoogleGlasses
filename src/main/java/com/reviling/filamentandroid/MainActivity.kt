package com.reviling.filamentandroid

//import CustomViewerActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
//import com.reviling.filamentandroid.ui.EntryFragment
import com.reviling.filamentandroid.viewmodels.SharedViewModel
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private lateinit var viewModel: SharedViewModel

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val count = supportFragmentManager.backStackEntryCount
            if (count != 0) {
                supportFragmentManager.popBackStack()
            }
        }
    }


    private var rotationAngle = 0f
    private val rotationSpeed = 0.5f // Adjust the rotation speed as needed
//    val assetLoader = AssetLoader(engine);
//    val assetUri = "models/astronaut/human.glb"
//    val renderable =loadGlb(this@MainActivity, "astronaut", "human")


    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null

    //    private lateinit var customViewer: CustomViewer
    private var startTime: Long = 0
    private lateinit var timerTextView: TextView
    private lateinit var viewModel2: CustomViewerActivity
    private val handler = Handler()
    //    private var surfaceView: SurfaceView? = null
    private var customViewer: CustomViewerActivity = CustomViewerActivity()
    //    private lateinit var chronometer: Chronometer
    private var isTimerStarted = false
//    val model:CustomViewerActivity by viewModels()

    private val updateTimerTask = object : Runnable {
        override fun run() {
            val timeElapsed = System.currentTimeMillis() - startTime
            timerTextView.text = formatTime(timeElapsed)
            handler.postDelayed(this, 1000) // Update every 1 second
        }
    }
    val sampleAngles = arrayOf(
        10.3f, 9.7f, 9.0f, 8.3f, 7.6f, 7.0f, 6.4f, 5.9f, 5.6f, 5.1f,
        4.4f, 3.6f, 2.9f, 2.4f, 2.2f, 2.1f, 2.0f, 1.9f, 2.2f, 3.0f,
        4.4f, 5.8f, 7.1f, 8.3f, 9.4f, 10.3f, 0.0f, 15.3f, 16.3f, 17.0f,
        17.5f, 18.5f, 19.1f, 20.3f, 21.4f, 22.4f, 23.6f, 24.7f, 26.1f,
        27.4f, 28.8f, 30.1f, 31.5f, 33.1f, 34.5f, 35.7f, 37.1f, 38.4f,
        39.8f, 41.2f, 42.8f, 44.4f, 45.9f, 47.4f, 48.8f, 50.3f, 51.9f,
        53.4f, 54.6f, 55.8f, 56.8f, 57.8f, 58.7f, 59.5f, 60.2f, 60.8f,
        61.2f, 61.7f, 62.0f, 62.2f, 62.5f, 62.9f, 63.0f, 62.8f, 62.6f,
        62.3f, 61.9f, 61.3f, 60.4f, 59.1f, 57.6f, 55.5f, 53.5f, 51.4f,
        49.0f, 46.6f, 44.0f, 41.4f, 38.8f, 36.3f, 34.0f, 32.0f, 29.9f,
        27.9f, 26.0f, 24.1f, 22.1f, 19.9f, 17.7f, 15.5f, 13.3f, 10.9f,
        8.7f, 6.4f, 4.4f, 2.5f, 1.3f, 2.0f, 3.8f, 5.5f, 7.2f, 8.8f,
        10.5f, 12.1f, 13.8f, 15.2f, 16.4f, 17.3f, 18.0f, 18.4f, 18.3f,
        18.4f, 18.4f, 18.5f, 18.5f, 18.5f
    )
//    private fun processLiveData(liveData : ByteArray){
//        livedata = liveData
//        model.uploadData(livedata)
//    }
val database = FirebaseDatabase.getInstance("https://neuroflex-2023-default-rtdb.firebaseio.com")

//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        var textView = findViewById<TextView>(R.id.textView)
////        viewModel2 = ViewModelProvider(this)[CustomViewerActivity::class.java]
//
//        // Observe LiveData for updating the TextView
////        viewModel2.getData().observe(this) { data ->
////            textView.text = data
////            Log.d("myTag*************", "Received data: $data")
////
////        }
//
//        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
//        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
//        EventBus.builder().installDefaultEventBus()
//
//        if (!allPermissionsGranted()) {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        } else {
//            viewModel.setPermission(allPermissionsGranted())
//        }
//
////        if (savedInstanceState == null) {
////            supportFragmentManager.beginTransaction()
////                .replace(R.id.container, EntryFragment.newInstance())
////                .commitNow()
////        }
//        // Get references to views
////        surfaceView = findViewById(R.id.surface_view)
//        textureView = findViewById(R.id.texture_view)
//
//
////        chronometer = findViewById(R.id.chronometer)
//
////    recordVideoBtn = findViewById(R.id.idBtnRecordVideo);
////    videoView = findViewById(R.id.videoView);
//        val lineGraphView: GraphView = findViewById(R.id.idGraphView)
//
//        val series: LineGraphSeries<DataPoint> = LineGraphSeries()
////        val valueTextView: TextView = findViewById(R.id.valueTextView)
////        val value= CustomViewer.getAngle();
////        EventBus.getDefault().post(UpdateValueEvent(CustomViewer.getAngle().toString()))
////        cva.handleUpdateValueEvent(UpdateValueEvent((angle(value!!)).toString()))
//
//
////        valueTextView.text="Value {${cva.updatedValue}}"
//
//        lineGraphView.apply {
//            addSeries(series)
//            viewport.isScrollable = true
//            viewport.isScalable = true
//            viewport.setScalableY(true)
//            viewport.setScrollableY(true)
//            title = "Analytics"
//            gridLabelRenderer.horizontalAxisTitle = "Time"
//            gridLabelRenderer.verticalAxisTitle = "Angle"
//            series.thickness = 5
//            gridLabelRenderer.numHorizontalLabels = 9
//            gridLabelRenderer.numVerticalLabels = 9
//            gridLabelRenderer.reloadStyles()
//        }
//
//
//
//        val timeIntervals = DoubleArray(10000) { it * 0.02 } // Generating time intervals from 0 to 5 with 0.05 increments
//
//        val ecgWaveform = DoubleArray(1000) {
//            val t = timeIntervals[it]
//            135.0 + 15.0 * (Math.sin(2 * Math.PI * t) + Math.sin(6 * Math.PI * t) / 3)
//        } // Generating a sinusoidal ECG waveform within the range of 120-150
//
//        val updateDelayMillis = 20 // Adjust the update delay in milliseconds
//        // Adjust the update delay in milliseconds
//
//        val handler = Handler()
//        var dataIndex = 0
//         var i=0
//
//        var startTime = System.nanoTime()
//    var voltage:Double=0.0
//// Define a Runnable to mimic ECG waveform with peaks and troughs
//        val simulateECGRunnable = object : Runnable {
//            override fun run() {
//                textView.text=String.format("%.2f", voltage)
//
//                val currentTime: Long = System.nanoTime()
//
//                var elapsedTime = (currentTime - startTime).toDouble() / 1_000_000_000
//                val sampleIndex = ((i ) % sampleAngles.size)
//                i++
//                if (dataIndex < timeIntervals.size) {
//                    if (sampleIndex < 1) {
//                        startTime = currentTime
//                        elapsedTime = 0.0
//                    }
//                    if (sampleIndex.toInt() == sampleAngles.size) {
//                        i=0
//                    }
//                    val time = timeIntervals[dataIndex]
//                     voltage = sampleAngles[sampleIndex.toInt()].toDouble()
//
//                    val dataPoint = DataPoint(time, voltage)
//
//                    series.appendData(dataPoint, true, timeIntervals.size)
////                    lineGraphView.onDataChanged(true, true)
//
//                    dataIndex++
//                    handler.postDelayed(this, updateDelayMillis.toLong())
//                } else {
//                    // Loop the simulation
//                    dataIndex = 0
//                    handler.postDelayed(this, updateDelayMillis.toLong())
//                }
//            }
//        }
//
//// Start the ECG simulation
//
//
////         Initialize the customViewer
//        customViewer = CustomViewerActivity()
////        runOnUiThread {
////            textView.text = customViewer.getCurrentSampleAngle().toString()
////        }
////        customViewer.run {
////            loadEntity()
////            setSurfaceView(requireNotNull(textureView))
//////            textView.setText(getCurrentSampleAngle().toString())
////
////            //directory and model each as param
//////            loadGlb(this@MainActivity, "astronaut", "human")
////
////            loadGltf(this@MainActivity, "warcraft","scene");
////
////            //directory and model as one
////            //loadGlb(this@MainActivity, "grogu/grogu");
////
////            //Environments and Lighting (OPTIONAL)
////
////            loadIndirectLight(this@MainActivity, "venetian_crossroads_2k")
////
////            val delayMillis = 0
////
////            Handler().postDelayed({
////                handler.post(simulateECGRunnable)
////
////                // Code to be executed after the delay
////                // This code will run after 2 seconds (2000 milliseconds)
////            }, delayMillis.toLong())
////
//////            loadEnviroment(this@MainActivity, "venetian_crossroads_2k");
////        }
////        val delayMillis = 20
////
////        Handler().postDelayed({
////            handler.post(simulateECGRunnable)
////
////            // Code to be executed after the delay
////            // This code will run after 2 seconds (2000 milliseconds)
////        }, delayMillis.toLong())
//
//    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            viewModel.setPermission(allPermissionsGranted())
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onResume() {
        super.onResume()
//        customViewer.onResume()

        // Start the timer when the user enters the page
        if (!isTimerStarted) {
//            chronometer.base = SystemClock.elapsedRealtime()
//            chronometer.start()
            isTimerStarted = true
        }
        // Start the rotation
//        handler.postDelayed(rotationTask, 16) // Update every 16 milliseconds (60 FPS)
    }

    override fun onPause() {
        super.onPause()
//        customViewer.onPause()
        // Stop the rotation
//        handler.removeCallbacks(rotationTask)
    }

    override fun onDestroy() {
        super.onDestroy()
        customViewer.onDestroy()
    }
//    public val rotationTask = object : Runnable {
//        override fun run() {
//            // Update the rotation angle
//            rotationAngle += rotationSpeed
//            if (rotationAngle > 360f) {
//                rotationAngle -= 360f
//            }
//
//            // Apply the rotation to the model
//            val rotationMatrix = FloatArray(16)
//            Matrix.setIdentityM(rotationMatrix, 0)
//            Matrix.rotateM(rotationMatrix, 0, rotationAngle, 0f, 1f, 0f)
////            customViewer.setRotationMatrix(rotationMatrix)
//
//            // Schedule the next rotation update
//            handler.postDelayed(this, 16)
//        }
//    }


    // Function to format the time in HH:mm:ss format
    private fun formatTime(timeInMillis: Long): String {
        val seconds = (timeInMillis / 1000) % 60
        val minutes = (timeInMillis / (1000 * 60)) % 60
        val hours = timeInMillis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


//    private fun setupCamera() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
//            return
//        }





//}
}

