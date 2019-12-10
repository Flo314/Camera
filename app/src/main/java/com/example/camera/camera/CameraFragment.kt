package com.example.camera.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer

import com.example.camera.R

// id de token
private const val REQUEST_PERMISSION_CAMERA = 1

class CameraFragment : Fragment() {

    companion object {
        fun newInstance() = CameraFragment()
    }

    private lateinit var viewModel: CameraViewModel

    private var oldSystemUiVisibility: Int = 0

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

    }

    // demande de permission pour la caméra
    private fun bindCameraUseCases() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                // id de token pour pouvoir retrouver le token
            REQUEST_PERMISSION_CAMERA
            )
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray  ) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA -> {
                if(grantResults.size != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                bindCameraUseCases()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

}
