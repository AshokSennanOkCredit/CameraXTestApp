package com.example.cameraxtestapp


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.cameraxtestapp.utils.LuminosityAnalyzer
import com.example.cameraxtestapp.views.Constants
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.camera_interaction_layout.*
import kotlinx.android.synthetic.main.fragment_camera_x.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
/**
 * A simple [Fragment] subclass.
 */
class CameraXFragment : Fragment() {

    companion object{
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private var cameraX : CameraXFragment?  = null
        fun getInstance(): CameraXFragment {

            return      CameraXFragment()

        }

        private const val TAG = "MainActivity"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        fun getOutPutDirectory(context: Context):File{
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it,"testImage").apply {
                    mkdirs()
                }
            }
            return if(mediaDir!= null && mediaDir.exists())mediaDir else appContext.filesDir
        }

        fun createFile(baseFolder:File,format:String,extension:String):File{
            return File(baseFolder,SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis())+extension)
        }
    }

    private lateinit var cameraProviderFuture:ListenableFuture<ProcessCameraProvider>
    lateinit var imagePreview: Preview



    private lateinit var imageCapture:ImageCapture
    private val imageCaptureExecutor = Executors.newSingleThreadExecutor()
    private lateinit var outPutDirectory: File


    private lateinit var imageAnalysis: ImageAnalysis
    private val imageAnalysisexecutor = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outPutDirectory = getOutPutDirectory(requireContext())

    }

    private fun takePicture() {
        val file = createFile(
            outPutDirectory,
            FILENAME,
            PHOTO_EXTENSION
        )

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(outputFileOptions,imageCaptureExecutor,object:ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${file.absolutePath}"
                preview_view.post {
                    Constants.imagePath = file.absolutePath
                    Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.container, PreviewFragment())
                        .commitAllowingStateLoss()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                val msg = "Photo capture failed: ${exception.message}"
                preview_view.post {
                    Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()
                }
            }

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera_x, container, false)
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        return view
    }

    override fun onStart() {
        super.onStart()
        preview_view.post {
            startCamera()
        }
        fab_camera.setOnClickListener {
            takePicture()
        }
    }

    fun startCamera(){

        //image capture set up
        imageCapture = ImageCapture.Builder().apply {
            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        }.build()

        //image preview set up
        imagePreview = Preview.Builder().apply {
            setTargetAspectRatio(aspectRatio(1,1))
            setTargetRotation(preview_view.display.rotation)
        }.build()
        imagePreview.setSurfaceProvider(preview_view.previewSurfaceProvider)

        //image analysis setup
        imageAnalysis = ImageAnalysis.Builder().apply {
            setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        }.build()
        imageAnalysis.setAnalyzer(imageAnalysisexecutor, LuminosityAnalyzer())

        //camera selector setup
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture.addListener(
            Runnable {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.bindToLifecycle(requireContext() as LifecycleOwner,cameraSelector,imagePreview,imageAnalysis,imageCapture)
            },ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun aspectRatio(widthPixels: Int, heightPixels: Int): Int {
        val previewRatio = Math.max(widthPixels, heightPixels).toDouble()/ Math.min(
            widthPixels,
            heightPixels
        )
        if (kotlin.math.abs(previewRatio - RATIO_4_3_VALUE) <= kotlin.math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }



}
