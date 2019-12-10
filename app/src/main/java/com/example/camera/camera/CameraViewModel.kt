package com.example.camera.camera

import androidx.camera.core.CameraX
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Indique l'état de l'interface graphique de CameraFragment
 */
sealed class CameraViewModelState(
    // est ce que les 3 boutons sont activés ou non
    val buttonsEnabled: Boolean = false,
    // est que le bouton de bascule de camera avant à arrière est visible (en haut à droite sur le layout)
    val switchCameraVisible: Boolean = false,
    /* quel est la camera activé par defaut -> arrière
     si on clique sur le bouton on changera d'état et on activera la caméra frontale*/
    val cameraLensDirection: CameraX.LensFacing = CameraX.LensFacing.BACK
) {
    // états 1 de la configuration de la camera
    class setupCamera(switchCameraVisible: Boolean, cameraLensDirection: CameraX.LensFacing) : CameraViewModelState(
        // la variable membre de (CameraViewModelState) est égale au paramètre passé au constructeur de (setupCamera)
        switchCameraVisible = switchCameraVisible,
        buttonsEnabled = false,
        cameraLensDirection = cameraLensDirection
    )

    // états 2
    class Error(
        val errorMessage: String,
        switchCameraVisible: Boolean,
        cameraLensDirection: CameraX.LensFacing) : CameraViewModelState(
        switchCameraVisible = switchCameraVisible,
        buttonsEnabled = false,
        cameraLensDirection = cameraLensDirection
    )
}



class CameraViewModel : ViewModel() {

    private val isSwitchCameraVisible: Boolean = true
    private val cameraLensDirection = CameraX.LensFacing.BACK

    // LiveData associé à cet état
    private val state = MutableLiveData<CameraViewModelState>()
    fun getState(): LiveData<CameraViewModelState> = state

    // refus de la permission
    fun errorPermissionDenied() {
        // remonter l'état d'erreur
        state.value = CameraViewModelState.Error(
            errorMessage = "Permission denied: Cannot takes pictures!",
            switchCameraVisible = isSwitchCameraVisible,
            cameraLensDirection = cameraLensDirection
        )
    }
}
