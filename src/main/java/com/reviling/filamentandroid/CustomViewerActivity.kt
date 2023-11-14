package com.reviling.filamentandroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.TextureView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.filament.*
import com.google.android.filament.utils.KTXLoader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.greenrobot.eventbus.EventBus
import java.nio.ByteBuffer
import java.util.concurrent.Executors


class CustomViewerActivity : AppCompatActivity() {
    private lateinit var modelViewer: ModelViewer
    private lateinit var textView:TextView
    private var value:Long = 0L
    private var harmfulAngle:Long = 0L

    private var isStarted:Int=1
    lateinit var choreographer: Choreographer
    private  var floatValue=0.0f

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_viewer)
        val lineGraphView: GraphView = findViewById(R.id.idGraphView)
        textView=findViewById(R.id.textView)
        val series: LineGraphSeries<DataPoint> = LineGraphSeries()
        val series2: LineGraphSeries<DataPoint> = LineGraphSeries()

        lineGraphView.apply {
            addSeries(series)
            addSeries(series2)
            viewport.isScrollable = true
            viewport.isScalable = true
            viewport.setScalableY(true)
            viewport.setScrollableY(true)
            title = "Analytics"
            gridLabelRenderer.horizontalAxisTitle = "Time"
            gridLabelRenderer.verticalAxisTitle = "Angle"
            series.thickness = 2
            series2.thickness = 1

            series2.color=Color.RED
            gridLabelRenderer.numHorizontalLabels = 9
            gridLabelRenderer.numVerticalLabels = 9
            gridLabelRenderer.reloadStyles()
        }
        choreographer = Choreographer.getInstance()
        val texView:TextureView = findViewById(R.id.texture_view)
        loadEntity()

        setSurfaceView(texView)

        loadGltf(this@CustomViewerActivity, "warcraft","scene");
        loadIndirectLight(this@CustomViewerActivity, "venetian_crossroads_2k")
        val timeIntervals = DoubleArray(100000) { it * 0.02 }
        val updateDelayMillis = 20 // Adjust the update delay in milliseconds
        val handler = Handler()
        var dataIndex = 0



        FirebaseApp.initializeApp(this)
        val reference=database.getReference("bendAngle")
        val referenceSession=database.getReference("sessionActive")
        val referenceHarmfulAngle=database.getReference("harmfulAngle")
        referenceHarmfulAngle.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                harmfulAngle = (dataSnapshot.getValue(Long::class.java)!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
        reference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                value = (dataSnapshot.getValue(Long::class.java)!!)
                if (value != null ) {
                    try {
                        textView.text = value.toString()

                        floatValue = value.toFloat()
                    } catch (e: NumberFormatException) {
throw(e)                    }
                }

                textView.text = "Value: $value"

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })

        referenceSession.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isStarted = (dataSnapshot.getValue(Int::class.java)!!)

                if(isStarted==0){
                    val intent = Intent(this@CustomViewerActivity, WelcomeActivity::class.java)
                    startActivity(intent)        }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
        connectFirebase()

        val simulateGraphRunnable = object : Runnable {
            override fun run() {
                val time = timeIntervals[dataIndex]

                val dataPoint = DataPoint(time, value.toDouble())
                val dataPoint2 = DataPoint(time, harmfulAngle.toDouble())


                series.appendData(dataPoint, true, timeIntervals.size)
                series2.appendData(dataPoint2,true,timeIntervals.size)
                dataIndex++
                handler.postDelayed(this, updateDelayMillis.toLong())
            }


        }
         textView = findViewById<TextView>(R.id.textView)


        val delayMillis = 20

        Handler().postDelayed({
            handler.post(simulateGraphRunnable)

        }, delayMillis.toLong())
    }


    public override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }


    companion object {
        init {
            Utils.init()
        }
    }

    fun loadEntity() {
        choreographer = Choreographer.getInstance()
    }

    fun setSurfaceView(mTextureView: TextureView) {
        modelViewer = ModelViewer(mTextureView)
        mTextureView.setOnTouchListener(modelViewer)

        modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)

        modelViewer.scene.skybox?.setColor(0.0f, 0.0f, 0.0f, 1.0f) // Black color

    }

    fun loadGlb(context: Context, name: String) {
        val buffer = readAsset(context, "models/${name}.glb")
        modelViewer.apply {
            loadModelGlb(buffer)
            transformToUnitCube()
        }
    }

    fun loadGlb(context: Context, dirName: String, name: String) {
        val buffer = readAsset(context, "models/${dirName}/${name}.glb")
        modelViewer.apply {
            loadModelGlb(buffer)
            transformToUnitCube()
        }
    }

    fun loadGltf(context: Context, name: String) {
        val buffer = context.assets.open("models/${name}.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        }
        modelViewer.apply {
            loadModelGltf(buffer) { uri -> readAsset(context, "models/$uri") }
            transformToUnitCube()
        }
    }

    fun loadGltf(context: Context, dirName: String, name: String) {
        val buffer = context.assets.open("models/${dirName}/${name}.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        }
        modelViewer.apply {
            loadModelGltf(buffer) { uri -> readAsset(context, "models/${dirName}/$uri") }
            transformToUnitCube()
        }
    }

    fun loadIndirectLight(context: Context, ibl: String) {
        // Create the indirect light source and add it to the scene.
        val buffer = readAsset(context, "environments/venetian_crossroads_2k/${ibl}_ibl.ktx")
        KTXLoader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 50_000f
            modelViewer.scene.indirectLight = this
        }
    }

    fun loadEnviroment(context: Context, ibl: String) {
        // Create the sky box and add it to the scene.
        val buffer = readAsset(context, "environments/venetian_crossroads_2k/${ibl}_skybox.ktx")
        KTXLoader.createSkybox(modelViewer.engine, buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(context: Context, assetName: String): ByteBuffer {
        val input = context.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    // Function to display a toast notification
    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()




    private val executor = Executors.newSingleThreadExecutor()
private var currentSampleAngle:Float= 0.0F

    private val frameCallback = object : Choreographer.FrameCallback {
        private var startTime = System.nanoTime()
        private val handler = Handler(Looper.getMainLooper())
        private var i=0
        @SuppressLint("SuspiciousIndentation")
        override fun doFrame(currentTime: Long) {
            choreographer.postFrameCallback(this)

            executor.execute {
                try {

                    handler.post {
                        modelViewer.render(currentTime)

                        currentSampleAngle= value.toFloat()
                        if(currentSampleAngle>harmfulAngle){
//                            toast("Bend angle is harmful :(")
                            modelViewer.scene.skybox?.setColor(1.0f, 0.0f, 0.0f, 1.0f) // Red color

                        }
                        else{
                            modelViewer.scene.skybox?.setColor(0.0f, 0.0f, 0.0f, 1.0f) // Black color

                        }
                        val rotationMatrix = FloatArray(16)
                        Matrix.setRotateM(rotationMatrix, 0, currentSampleAngle, 1f, 0f, 0f) // Rotate around the X axis


                        modelViewer.asset?.getFirstEntityByName("Spine_53")?.setTransform(rotationMatrix)

                        modelViewer.animator?.updateBoneMatrices()
                        modelViewer.render(currentTime)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
    val database = FirebaseDatabase.getInstance()



private fun connectFirebase() {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
database.getReference("bendAngle")
    println("Connected with Firebase...")
}

    private fun Int.setTransform(mat: FloatArray) {
        val tm = modelViewer.engine.transformManager
        tm.setTransform(tm.getInstance(this), mat)
    }




    @SuppressLint("MissingSuperCall")
    public override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)    }

    @SuppressLint("MissingSuperCall")
    public override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)    }

}


