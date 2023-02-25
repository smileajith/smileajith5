package com.ajith.smilestatusaver

import android.app.Dialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.BitSet

open class MainActivity : AppCompatActivity() {

    private lateinit var rvStatusList: RecyclerView
    private lateinit var statusList: ArrayList<ModelClass>
    private lateinit var statusAdapter: StatusAdapter

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.title = "All Statuses"

        rvStatusList = findViewById(R.id.status_list)
        statusList = ArrayList()

        //filepermission here (whenever the user enters the app

        val result = readDataFromPrefs()

        if (result) {
            val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val uriPath = sh.getString("PATH", "")

            contentResolver.takePersistableUriPermission(
                Uri.parse(uriPath),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            if (uriPath != null) {
                val fileDoc = DocumentFile.fromTreeUri(applicationContext, Uri.parse(uriPath))
                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    if (!file.name!!.endsWith("nomedia")) { //condition to check the no media file
                        val modelClass = ModelClass(
                            file.name!!,
                            file.uri.toString()
                        )  //take the file name and URI
                        statusList.add(modelClass)
                    }
                }
                setUpRecyclerView(statusList)
            }

        } else {
            getFolderPermission()
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getFolderPermission() {
        val storageManager = application.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        val targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2Fmedia%2F.Statuses"
      //  val targetDirectory = Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/media/.Statuses"

        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
        var scheme = uri.toString()
        scheme = scheme.replace("/root/", "/tree/")
        scheme += "%3A$targetDirectory"
        uri = Uri.parse(scheme)
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        startActivityForResult(intent, 1234)

    }

    private fun readDataFromPrefs(): Boolean {
        val sh = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
        val uriPath = sh.getString("PATH", "")
        if (uriPath != null) {
            if (uriPath.isEmpty()) {
                return false
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TAG", "onActivityResult: "+requestCode)
        if (resultCode == RESULT_OK) {
            val treeUri = data?.data

            val sharedPreferences = getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("PATH", treeUri.toString())
            myEdit.apply()

            if (treeUri != null) {
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val fileDoc = DocumentFile.fromTreeUri(applicationContext, treeUri)
                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    if (!file.name!!.endsWith("nomedia")) { //condition to check the no media file
                        val modelClass = ModelClass(
                            file.name!!,
                            file.uri.toString()
                        )  //take the file name and URI
                        statusList.add(modelClass)
                    }
                }
                setUpRecyclerView(statusList)
            }
        }
    }

    private fun setUpRecyclerView(statusList: ArrayList<ModelClass>) {

        statusAdapter = applicationContext?.let {
            StatusAdapter(
                it, statusList
            ){
                selectedStatusItem:ModelClass->listItemClicked(selectedStatusItem)
            }
        }!!


        rvStatusList.apply {

            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = statusAdapter

        }
    }

    private fun listItemClicked(status: ModelClass) {

        val dialog=Dialog(this@MainActivity)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.show()
        val btDownload = dialog.findViewById<Button>(R.id.bt_download)
        btDownload.setOnClickListener{
            dialog.dismiss()
            saveFile(status)
        }

        val btShare = dialog.findViewById<Button>(R.id.bt_share)

        btShare.setOnClickListener{

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = if (status.fileUri.endsWith(".mp4")) "video/*" else "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(status.fileUri))
            shareIntent.setPackage("com.whatsapp")
            startActivity(Intent.createChooser(shareIntent, "Share to"))
        }
    }

    private fun saveFile(status: ModelClass) {

        if(status.fileUri.endsWith(".mp4")){

            val inputStream = contentResolver.openInputStream(Uri.parse(status.fileUri))
            val fileName = "${System.currentTimeMillis()}.mp4"
            try{
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                values.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4")
                values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOCUMENTS+"/Videos/")
                val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"),values)
                val outputStream:OutputStream=uri?.let { contentResolver.openOutputStream(it) }!!
                if(inputStream!=null){
                    outputStream.write(inputStream.readBytes())
                }
                outputStream.close()
                Toast.makeText(applicationContext, "Videos saved to Gallery", Toast.LENGTH_SHORT).show()
            }catch (e:IOException){
                Toast.makeText(applicationContext, "Failed with exception", Toast.LENGTH_SHORT).show()
            }

        }else{

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,Uri.parse(status.fileUri))
            val fileName = "${System.currentTimeMillis()}.jpg"
            var fos:OutputStream?=null
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                contentResolver.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri:Uri?=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
                    fos=imageUri?.let { resolver.openOutputStream(it) }
                }
            }else{
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir,fileName)
                fos=FileOutputStream(image)
            }
            fos?.use{
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,it)
                Toast.makeText(applicationContext, "Images saved to Gallery", Toast.LENGTH_SHORT).show()
            }

        }

    }
}

