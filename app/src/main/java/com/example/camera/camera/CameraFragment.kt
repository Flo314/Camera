package com.example.camera.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer

import com.example.camera.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_camera.*
import timber.log.Timber
import training.sensor.strangercam.camera.AutoFitPreviewBuilder

// id de token
private const val REQUEST_PERMISSION_CAMERA = 1

class CameraFragment : Fragment() {

    companion object {
        fun newInstance() = CameraFragment()
    }

    private lateinit var viewModel: CameraViewModel
    private lateinit var viewModelState: CameraViewModelState

    private var oldSystemUiVisibility: Int = 0

    // cameraX
    private var preview: Preview? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)
        // s'abonner aux modifications du state
        viewModel.getState().observe(this, Observer {
            updateUi(it!!)
        })
    }

    override fun onResume() {
        super.onResume()
        // on stocke l'ancienne version de l'Ui
        oldSystemUiVisibility = activity!!.window.decorView.systemUiVisibility
        activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // cacher la status bar et l'actionbar quand on rentre dans le fragment
        (activity as AppCompatActivity).supportActionBar?.hide()
        bindCameraUseCases()
    }

    override fun onPause() {
        super.onPause()
        activity!!.window.decorView.systemUiVisibility = oldSystemUiVisibility
        // montrer la status bar et l'actionbar quand on rentre dans le fragment
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    // mise à jour de l'interface
    private fun updateUi(state: CameraViewModelState) {
        Timber.i("Calling updateUi(), switch stete=${state::class.java}")
        viewModelState = state
        val res = when (state) {
            is CameraViewModelState.setupCamera -> TODO()
            is CameraViewModelState.Error -> handleStateError(state)
        }

        with(state) {
            captureButton.isEnabled = buttonsEnabled
            galleryButton.isEnabled = buttonsEnabled
            switchCameraButton.isEnabled = buttonsEnabled
            switchCameraButton.visibility = if (switchCameraVisible) View.VISIBLE else View.GONE
        }
        // récup la color du button
        val capturColor = ContextCompat.getColor(
            context!!,
            if (captureButton.isEnabled) R.color.colorAccent else R.color.colorDisabledButton
        )
        // l'affecter au captureButton
        captureButton.backgroundTintList = ColorStateList.valueOf(capturColor)
    }

    // gérer le cas d'erreur
    private fun handleStateError(state: CameraViewModelState.Error) {
        Snackbar.make(coordinatorLayout, "Error: ${state.errorMessage}", Snackbar.LENGTH_LONG).show()
    }

    /*demande de permission pour la caméra
    s'attacher au lifecycle du fragment
    s'attacher aux use case de la camera*/
    @SuppressLint("RestrictedApi")
    private fun bindCameraUseCases() {
        // permission
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                // id de token pour pouvoir retrouver le token
            REQUEST_PERMISSION_CAMERA
            )
            return
        }

        // récupérer la résolution de la surface qu'on souhaite (aspect Ration -> rapport entre la hauteur et la largeur)
        val metrics = DisplayMetrics().apply {
            previewTextureView.display.getRealMetrics(this)
        }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        val resolution = Size(metrics.widthPixels, metrics.heightPixels)
        Timber.d("Screen Metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        // preview
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(viewModelState.cameraLensDirection)
            .setTargetAspectRatioCustom(screenAspectRatio)
            .setTargetRotation(previewTextureView.display.rotation)
            .setTargetResolution(resolution)
            .build()

        preview = AutoFitPreviewBuilder.build(previewConfig, previewTextureView)

        // attacher la preview au lifecycle du fragment
        CameraX.bindToLifecycle(this, preview)

    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray  ) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA -> {
                if(grantResults.size != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    viewModel.errorPermissionDenied()
                    return
                }
                bindCameraUseCases()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

}
