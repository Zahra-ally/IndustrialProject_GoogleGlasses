
package com.reviling.filamentandroid.ui


import android.Manifest
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModelProvider
import com.reviling.filamentandroid.databinding.FragmentVideoBinding
import com.reviling.filamentandroid.viewmodels.SharedViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.reviling.filamentandroid.R

class VideoFragment : Fragment() {
    companion object {
        fun newInstance() = VideoFragment()
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    private var recordingTimer: CountDownTimer? = null
    private var isRecording = false
    var elapsedSeconds = 0L
    private lateinit var _binding: FragmentVideoBinding
    private lateinit var sharedViewModel: SharedViewModel

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            sharedViewModel = ViewModelProvider(it).get(SharedViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.isPermissionGranted.observe(viewLifecycleOwner) {
            if (it) startCamera()
        }

        _binding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun captureVideo() {
        // Check if the VideoCapture use case has been created: if not, do nothing.
        val videoCapture = this.videoCapture ?: return

        _binding.videoCaptureButton.isEnabled = false

        // If there is an active recording in progress, stop it and release the current recording.
        // We will be notified when the captured video file is ready to be used by our application.
        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            stopRecordingTimer()
            return
        }

        // To start recording, we create a new recording session.
        // First, we create our intended MediaStore video content object,
        // with system timestamp as the display name (so we could capture multiple videos).
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireActivity().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .apply {
                // Enable Audio for recording
                if (
                    PermissionChecker.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        _binding.videoCaptureButton.apply {
                            text = "Stop Recording"
                            isEnabled = true
                        }
                        startRecordingTimer()
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg =
                                "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.container,
                                    VideoViewFragment.newInstance(recordEvent.outputResults.outputUri)
                                )
                                .addToBackStack(null)
                                .commit()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
                        }
                        _binding.videoCaptureButton.apply {
                            text = "Start Recording"
                            isEnabled = true
                        }
                        stopRecordingTimer()
                    }
                }
            }
    }

    private fun startRecordingTimer() {
        isRecording = true
        recordingTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
// Calculate elapsed minutes and seconds
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60

                // Format the elapsed time as "mm:ss"
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                // Update the TextView with the formatted time
                _binding.countdownTimerTextView.text = formattedTime

                // Increment the elapsed time
                elapsedSeconds++      }

            override fun onFinish() {
                // Recording has finished, do any cleanup or actions you need here
                isRecording = false
            }
        }.start()
    }

    private fun stopRecordingTimer() {
        isRecording = false
        recordingTimer?.cancel()
        recordingTimer = null
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(_binding.videoPreviewView.surfaceProvider)
                }

            // Video
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}