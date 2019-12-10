package com.iorlova.diploma.UI

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iorlova.diploma.R
import com.iorlova.diploma.Repository.Book
import com.iorlova.diploma.ViewModel.BookViewModel
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.codec.digest.DigestUtils

class MainActivity : AppCompatActivity() {

    companion object {
        internal const val REQUEST_PERMISSION = 1
        internal const val FILE_REQUEST_CODE = 1
    }

    private lateinit var bookViewModel: BookViewModel

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onResume() {
        super.onResume()
        indeterminateBar.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        val adapter = BookListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookViewModel = ViewModelProviders.of(this).get(BookViewModel::class.java)
        bookViewModel.books.observe(this, Observer { books ->
            books!!.let { adapter.setBooks(it) }
        })

        recyclerView.addOnItemTouchListener(
            RecyclerItemListener(this, recyclerView,
                object : RecyclerItemListener.RecyclerTouchListener {
                    override
                    fun onClickItem(view: View, position: Int) {
                        val book = bookViewModel.books.value!![position]
                        loadBook(book)
                    }

                    override
                    fun onLongClickItem(view: View, position: Int) {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        // Vibrate for 500 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    500,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        } else {
                            //deprecated in API 26
                            vibrator.vibrate(500)
                        }
                        removeBook(position)
                    }
                })
        )
        fab.setOnClickListener {
            val intent = Intent(this, FilePickerActivity::class.java)
            intent.putExtra(FilePickerActivity.CONFIGS, Configurations.Builder()
                            .setSuffixes("rtf", "pdf", "txt")
                            .setSingleChoiceMode(true)
                            .setShowFiles(true)
                            .setShowImages(false)
                            .setShowVideos(false)
                            .setShowAudios(false)
                            .build()
            )
            startActivityForResult(intent, FILE_REQUEST_CODE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.raw.
        return if (item.itemId == R.id.action_test_eye_detection) {
            startActivity(Intent(this, TestEyeDetection::class.java))
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    fun loadBook(book: Book) {
        indeterminateBar.visibility = View.VISIBLE

        val intent = if(book.path.endsWith(".pdf")) {
            Intent(applicationContext, PdfExtractor::class.java)
        } else {
            Intent(applicationContext, ReadBookActivity::class.java)
        }

        intent.putExtra("BOOK_ID", book.id.toString())
        intent.putExtra("BOOK_PATH", book.path)
        intent.putExtra("BOOK_PAGE_COUNTER", book.page_counter)

        startActivity(intent)
    }

    fun removeBook(position: Int) {
        val builder = android.app.AlertDialog.Builder(this@MainActivity)

        builder.setTitle("Confirm")
        builder.setMessage("Are you sure you want to delete?")
        builder.setPositiveButton("YES") { dialog, which ->
            val book = bookViewModel.books.value!![position]
            bookViewModel.delete(book)
        }
        builder.setNegativeButton("NO") { dialog, which ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun createBook(data: Intent?): Book {
        val books: ArrayList<MediaFile> = data!!.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES) //Always one book
        val book = books[0]
        val bookPath = book.path
        val bookName: String = bookPath!!.substringAfterLast("/")
        val bookFormat: String = bookName.substringAfterLast(".")
        val bookChecksum: String = DigestUtils.md5Hex(book.toString())

        return Book(name = bookName, format = bookFormat, path = bookPath, checksum = bookChecksum)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val book = createBook(data)
            bookViewModel.insert(book)
        }
    }

}
