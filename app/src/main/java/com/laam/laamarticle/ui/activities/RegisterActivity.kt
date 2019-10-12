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
import com.laam.laamarticle.models.response.ResponseDB
import com.laam.laamarticle.services.SharedPrefHelper
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var listCategory: List<Category>

    private var categoryId: Int = 0
    private lateinit var imageUri: Uri
    private var encodedImage: String? = null
    private var isEdit = false

    private val IMAGE_CAPTURE_KEY = 0
    private val IMAGE_GALLERY_KEY = 1

    private val MY_CAMERA_REQUEST_CODE = 100
    private val MY_EXTERNAL_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        isEdit = intent.getBooleanExtra("isEdit", false)

        toolbar_activity_title.text = if (!isEdit) {
            "Register User"
        } else {
            "Edit Profile"
        }
        toolbar_activity_share.text = "Done"
        toolbar_activity_back.setOnClickListener {
            onBackPressed()
        }
        toolbar_activity_share.setOnClickListener {
            if (register_et_email.text.toString().trim() == "") {
                register_et_email.error = "Email required"
            } else if (register_et_password.text.toString().trim() == "") {
                register_et_password.error = "Password required"
            } else if (register_et_name.text.toString().trim() == "") {
                register_et_name.error = "Namr required"
            } else if (register_et_bio.text.toString().trim() == "") {
                register_et_bio.error = "Bio required"
            } else if (categoryId == 0) {
                Toast.makeText(this@RegisterActivity, "Job category required", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (!isEdit) {
                    onDoneRegisterPressed()
                } else {
                    onDoneEditPressed()
                }
            }
        }

        showSpItem()

        register_img_add.setOnClickListener {
            onAddPhotoPressed()
        }

        if (isEdit) {
            initProfile()
        }
    }

    private fun initProfile() {
        val pref = SharedPrefHelper(this@RegisterActivity).getAccount()
        register_et_email.setText(pref.email)
        register_et_name.setText(pref.name)
        register_et_password.setText(pref.password)
        register_et_bio.setText(pref.bio)
        Glide.with(this@RegisterActivity)
            .load(ServiceBuilder.BASE_URL + pref.imageUrl)
            .circleCrop()
            .into(register_img_profile)
    }

    private fun onAddPhotoPressed() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
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

                    if (isEdit) {
                        val pref = SharedPrefHelper(this@RegisterActivity).getAccount()
                        for (i in listCategory.indices) {
                            if (listCategory[i].id == pref.jobId) {
                                register_sp_job.setSelection(i)
                            }
                        }
                    }
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

    private fun onDoneRegisterPressed() {
        if (encodedImage == null) {
            Toast.makeText(this@RegisterActivity, "Picture required", Toast.LENGTH_SHORT).show()
            return
        }

        ServiceBuilder.buildService(UserService::class.java).postRegister(
            register_et_email.text.toString(),
            register_et_password.text.toString(),
            categoryId,
            register_et_name.text.toString(),
            register_et_bio.text.toString(),
            encodedImage!!
        ).enqueue(object : Callback<ResponseDB> {
            override fun onResponse(call: Call<ResponseDB>, response: Response<ResponseDB>) {
                Toast.makeText(this@RegisterActivity, response.body()!!.message, Toast.LENGTH_SHORT)
                    .show()
                if (response.body()!!.success) {
                    onBackPressed()
                }
            }

            override fun onFailure(call: Call<ResponseDB>, t: Throwable) {
                Log.e("onFailure", t.message)
                Toast.makeText(this@RegisterActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onDoneEditPressed() {
        val pref = SharedPrefHelper(this@RegisterActivity).getAccount()
        ServiceBuilder.buildService(UserService::class.java).putProfile(
            pref.id,
            register_et_email.text.toString(),
            register_et_password.text.toString(),
            categoryId,
            register_et_name.text.toString(),
            register_et_bio.text.toString(),
            encodedImage
        ).enqueue(object : Callback<ResponseDB> {
            override fun onResponse(call: Call<ResponseDB>, response: Response<ResponseDB>) {
                Toast.makeText(this@RegisterActivity, response.body()!!.message, Toast.LENGTH_SHORT)
                    .show()
                if (response.body()!!.success) {
                    finish()
                }
            }

            override fun onFailure(call: Call<ResponseDB>, t: Throwable) {
                Log.e("onFailure", t.message)
                Toast.makeText(this@RegisterActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
