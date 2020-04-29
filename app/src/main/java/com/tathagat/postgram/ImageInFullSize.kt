package com.tathagat.postgram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso

class ImageInFullSize : AppCompatActivity() {
var image:PhotoView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_in_full_size)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        image=findViewById(R.id.image_fullsize)
        var link=intent.getStringExtra("image link")
        Picasso.get().load(link).into(image)
    }
}
