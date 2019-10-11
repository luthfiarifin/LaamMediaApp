package com.laam.laamarticle.ui.activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.laam.laamarticle.R
import com.laam.laamarticle.models.Category
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.api.UserService
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.toolbar_activity_post.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    private lateinit var listCategory: List<Category>
    private var categoryId: Int = 0
    private lateinit var imageUri: Uri
    private var encodedImage: String = ""

    private val IMAGE_CAPTURE_KEY = 0
    private val IMAGE_GALLERY_KEY = 1

    private val MY_CAMERA_REQUEST_CODE = 100
    private val MY_EXTERNAL_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        toolbar_activity_title.text = "Register User"
        toolbar_activity_share.text = "Done"
        toolbar_activity_back.setOnClickListener {
            onBackPressed()
        }
        toolbar_activity_share.setOnClickListener {
            onDonePressed()
        }

        showSpItem()

        register_img_add.setOnClickListener {
            onAddPhotoPressed()
        }
    }

    private fun onAddPhotoPressed() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_CAMERA_REQUEST_CODE
                    )
                } else if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    )
                    == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_CAMERA_REQUEST_CODE
                    )
                } else if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_EXTERNAL_REQUEST_CODE
                    )
                } else {
                    val timeStamp =
                        "profile_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString()
                    val cv = ContentValues()
                    cv.put(MediaStore.Images.Media.TITLE, timeStamp)
                    cv.put(
                        MediaStore.Images.Media.DESCRIPTION,
                        resources.getString(R.string.app_name)
                    )
                    imageUri =
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(intent, IMAGE_CAPTURE_KEY)
                }

            } else if (options[item] == "Choose from Gallery") {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    IMAGE_GALLERY_KEY
                )
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            IMAGE_CAPTURE_KEY -> {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver, imageUri
                )
                Glide.with(this@RegisterActivity)
                    .load(imageBitmap)
                    .circleCrop()
                    .into(register_img_profile)
                encodedImage = encodeImage(imageBitmap)
            }
            IMAGE_GALLERY_KEY -> {
                val imageUri = data!!.data
                contentResolver.notifyChange(imageUri!!, null)
                val cr = contentResolver
                val bitmap: Bitmap
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri)
                    Glide.with(this@RegisterActivity)
                        .load(bitmap)
                        .circleCrop()
                        .into(register_img_profile)
                    encodedImage = encodeImage(bitmap)
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()

        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onAddPhotoPressed()
            } else {
                return
            }
        }

        if (requestCode == MY_EXTERNAL_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onAddPhotoPressed()
            } else {
                return
            }
        }
    }

    private fun showSpItem() {
        register_sp_job.setTitle("Select Your Job")

        ServiceBuilder.buildService(UserService::class.java).getJobCategory()
            .enqueue(object : Callback<List<Category>> {
                override fun onResponse(
                    call: Call<List<Category>>,
                    response: Response<List<Category>>
                ) {
                    listCategory = response.body()!!
                    val servicesName = arrayOfNulls<String>(listCategory.size)
                    for (i in listCategory.indices) {
                        servicesName[i] = listCategory[i].name
                    }

                    val adapter = ArrayAdapter<String>(
                        this@RegisterActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        servicesName
                    )
                    register_sp_job.adapter = adapter
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    Log.e("onFailure", t.message)
                }

            })
        register_sp_job.onItemSelectedListener = this
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        categoryId = 0
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        categoryId = listCategory[p2].id
    }

    private fun onDonePressed() {

    }
}
