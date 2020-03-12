package com.example.cameraxtestapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.cameraxtestapp.R
import kotlinx.android.synthetic.main.camera_interaction_layout.view.*
import java.util.concurrent.TimeUnit

class CameraInteractionFragment(context:Context,attributeSet: AttributeSet):ConstraintLayout(context,attributeSet){
    private lateinit var interactor: Interactor

    fun addInteractor(interactor:Interactor){
        this.interactor = interactor
    }

    fun off_Torch(){
        flash_iv.setImageDrawable(context?.getDrawable(R.drawable.ic_flash_off))
    }

    fun on_Torch(){
        flash_iv.setImageDrawable(context?.getDrawable(R.drawable.ic_flash_on))
    }

    init {
        View.inflate(context, R.layout.camera_interaction_layout,this)
        fab_camera.setOnClickListener {
            interactor.onClick()
        }
        flash_iv.setOnClickListener {
            interactor.onFlashClicked()
        }
        gallery_iv.setOnClickListener {
            interactor.onGalleryClicked()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    interface Interactor{
        fun onClick()
        fun onFlashClicked()
        fun onGalleryClicked()
    }
}