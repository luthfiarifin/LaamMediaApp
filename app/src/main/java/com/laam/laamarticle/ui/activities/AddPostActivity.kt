package com.laam.laamarticle.ui.activities

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.toolbar_activity_post.*
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.TagRecyclerViewAdapter
import com.laam.laamarticle.models.Category
import com.laam.laamarticle.models.response.ResponseDB
import com.laam.laamarticle.services.SharedPrefHelper
import com.laam.laamarticle.services.api.PostService
import com.laam.laamarticle.services.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class AddPostActivity : AppCompatActivity() {
    private val TAKE_PHOTO_CODE = 0
    private val TAKE_LIBRARY_CODE = 1
    private val MY_CAMERA_REQUEST_CODE = 100
    private val MY_EXTERNAL_REQUEST_CODE = 101

    private var pref_id = 0
    private var encodedImage = ""
    private var id_tag = -1

    private var progressDialog: ProgressDialog? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        toolbar_activity_title.text = "New Post"
        toolbar_activity_back.setOnClickListener {
            onBackPressed()
        }

        pref_id = SharedPrefHelper(this@AddPostActivity).getAccount().id

        showRecyclerviewTag()
        toolbarClick()
        btnClick()
    }

    private fun showRecyclerviewTag() {
        val service =
            ServiceBuilder.buildService(PostService::class.java).getCategory()

        service.enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful) {
                    add_post_rv_tag.layoutManager =
                        LinearLayoutManager(
                            this@AddPostActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    val categoryList: MutableList<Category> = mutableListOf()
                    categoryList.add(Category(0, "Choose TAG"))
                    categoryList.addAll(response.body()!!)
                    val adapter =
                        TagRecyclerViewAdapter(categoryList, this@AddPostActivity)
                    add_post_rv_tag.adapter = adapter
                    adapter.setOnItemClickCallback(object :
                        TagRecyclerViewAdapter.OnItemClickCallback {
                        override fun onItemClicked(data: Category) {
                            id_tag = data.id
                        }
                    })
                } else {
                    Toast.makeText(this@AddPostActivity, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@AddPostActivity, "Error : ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun toolbarClick() {
        toolbar_activity_share.setOnClickListener {
            if (TextUtils.isEmpty(add_post_et_title.text.toString())) {
                add_post_et_title.error = "The item title cannot be empty"
            } else if (TextUtils.isEmpty(add_post_et_description.text.toString())) {
                add_post_et_description.error = "The item content cannot be empty"
            } else if (encodedImage == "") {
                Toast.makeText(this, "Image cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (id_tag == -1) {
                Toast.makeText(this, "Tag must be choose", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog = ProgressDialog(this)
                progressDialog!!.setTitle("Please wait...")
                progressDialog!!.show()

                val service = ServiceBuilder.buildService(PostService::class.java)
                service.addPost(
                    pref_id,
                    id_tag,
                    add_post_et_title.text.toString(),
                    add_post_et_description.text.toString(),
                    encodedImage
                )
                    .enqueue(object :
                        Callback<ResponseDB> {
                        override fun onResponse(
                            call: Call<ResponseDB>,
                            response: Response<ResponseDB>
                        ) {
                            Toast.makeText(
                                this@AddPostActivity,
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            if (response.body()!!.success) {
                                onBackPressed()
                            }

                            progressDialog!!.hide()
                        }

                        override fun onFailure(call: Call<ResponseDB>, t: Throwable) {
                            Log.e("onFailure", t.message)
                            progressDialog!!.hide()
                        }
                    })
            }
        }
    }

    private fun btnClick() {
        add_post_img_photo.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
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
                    "post_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).toString()
                val cv = ContentValues()
                cv.put(MediaStore.Images.Media.TITLE, timeStamp)
                cv.put(MediaStore.Images.Media.DESCRIPTION, resources.getString(R.string.app_name))
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(intent, TAKE_PHOTO_CODE)
            }
        }

        add_post_img_library.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                TAKE_LIBRARY_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            TAKE_PHOTO_CODE -> {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver, imageUri
                )
                encodedImage = encodeImage(imageBitmap)
            }
            TAKE_LIBRARY_CODE -> {
                val imageUri = data!!.data
                contentResolver.notifyChange(imageUri!!, null)
                val cr = contentResolver
                val bitmap: Bitmap
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri)
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
                btnClick()
            } else {
                return
            }
        }

        if (requestCode == MY_EXTERNAL_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnClick()
            } else {
                return
            }
        }
    }
}
