package com.alvimatruck.custom


import android.annotation.SuppressLint
import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.alvimatruck.R
import com.alvimatruck.databinding.DialogScannerBinding
import com.alvimatruck.interfaces.ScannerResultListener
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ScannerDialogFragment : DialogFragment() {
    private lateinit var cameraExecutor: ExecutorService
    private var scannerResultListener: ScannerResultListener? = null

    private var processingBarcode = AtomicBoolean(false)

    private lateinit var binding: DialogScannerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // return inflater.inflate(R.layout.dialog_scanner, container, false)
        binding = DialogScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        val mAnimation: Animation = TranslateAnimation(
            TranslateAnimation.ABSOLUTE, 0f,
            TranslateAnimation.ABSOLUTE, 0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 1.0f
        )
        mAnimation.duration = 2000
        mAnimation.repeatCount = Animation.INFINITE
        mAnimation.repeatMode = Animation.REVERSE
        mAnimation.interpolator = LinearInterpolator()
        binding.mScanner.animation = mAnimation


    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            if (!isAdded) return@addListener // avoid running if fragment is gone

            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodes ->

                        if (processingBarcode.compareAndSet(false, true)) {
                            Log.d("TAG", "Scanned barcode: ${barcodes}")
                            val mp: MediaPlayer =
                                MediaPlayer.create(requireContext(), R.raw.sucess)
                            mp.setOnCompletionListener { player -> player.release() }
                            mp.start()
                            scannerResultListener?.onScanResult(barcodes)
                            dismiss()

                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                println("Use case binding failed: $exc")
            }

        }, ContextCompat.getMainExecutor(requireContext()))


    }

    private class BarcodeAnalyzer(
        private val barcodeListener: (barcode: String) -> Unit
    ) :
        ImageAnalysis.Analyzer {
        private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        @OptIn(ExperimentalGetImage::class)
        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcodeListener(barcode.rawValue ?: "")
                            //imageProxy.close()
                        }
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                    }
                    .addOnCompleteListener {
                        // It's important to close the imageProxy
                        imageProxy.close()
                    }
            }


        }
    }

    fun setScannerResultListener(listener: ScannerResultListener) {
        scannerResultListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.DialogTheme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        processingBarcode.set(false)
    }

}