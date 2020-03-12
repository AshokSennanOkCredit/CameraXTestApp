package com.example.cameraxtestapp


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cameraxtestapp.views.Constants
import kotlinx.android.synthetic.main.fragment_preview.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class PreviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onStart() {
        super.onStart()
        setImageAccordingToSize(Constants.imagePath)
    }
    fun setImageAccordingToSize(imgPath:String){
        try{
            if(File(imgPath).exists()){

                val index=imgPath.lastIndexOf('/')
                val isFromGallary = imgPath.substring(index+1,imgPath.length-1).startsWith("offerImage_")
                if(!isFromGallary){
                    val imageStream = activity?.contentResolver?.openInputStream(Uri.fromFile( File(imgPath)))
                    val selectedImagebitMap = BitmapFactory.decodeStream(imageStream);

                    val ei =  ExifInterface(imgPath);
                    val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                    var rotatedBitmap: Bitmap? = null;

                    when(orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 ->
                            rotatedBitmap = rotateImage(selectedImagebitMap, 90.toFloat())

                        ExifInterface.ORIENTATION_ROTATE_180 ->
                            rotatedBitmap = rotateImage(selectedImagebitMap, 180.toFloat())

                        ExifInterface.ORIENTATION_ROTATE_270 ->
                            rotatedBitmap = rotateImage(selectedImagebitMap, 270.toFloat())

                        ExifInterface.ORIENTATION_NORMAL ->
                            rotatedBitmap = selectedImagebitMap

                    }


                    iv_product_image.setImageBitmap(rotatedBitmap);
                }else{
                    val imageStream = activity?.contentResolver?.openInputStream(Uri.fromFile( File(imgPath)))
                    val selectedImagebitMap = BitmapFactory.decodeStream(imageStream);
                    iv_product_image.setImageBitmap(selectedImagebitMap);
                }
                iv_product_image.visibility = View.VISIBLE
            }
        }catch (n:NullPointerException) {
            n.printStackTrace();
        }


    }

    fun rotateImage(source: Bitmap, angle:Float): Bitmap {
        val matrix =  Matrix()
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
            matrix, true);
    }


}
