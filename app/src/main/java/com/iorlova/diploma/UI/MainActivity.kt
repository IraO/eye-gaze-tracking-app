package com.iorlova.diploma.UI

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        internal const val REQUEST_PERMISSION = 1
        internal const val OPEN_DOCUMENT_REQUEST_CODE = 1
    }

    private lateinit var bookViewModel: BookViewModel
    private lateinit var radioText: String

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
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
                        if (true) {
                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle("Reading Goal")
                            var radio: RadioButton? = findViewById(R.id.radio_timer)
                            var radioCount: RadioButton? = findViewById(R.id.radio_counter)
                            val view = layoutInflater.inflate(R.layout.dialog_reading_goal, null)
                            builder.setView(view)
                            builder.setPositiveButton("YES") { dialog, which ->
                                var goalId = 3
                                var goalVal = ""

                                if (radio!!.isChecked()) {
                                    goalId = 0
                                    val hour = findViewById<EditText>(R.id.timer_hours)
                                    val minute = findViewById<EditText>(R.id.timer_minutes)
                                    goalVal = hour.text.toString() + ":" + minute.text
                                } else if (radioCount!!.isChecked()) {
                                    goalId = 0
                                    val counter = findViewById<EditText>(R.id.counter)
                                    goalVal = counter.text.toString()
                                } else {

                                }

                                loadBook(book, goalId, goalVal)
                            }
                            val alert = builder.create()
                            alert.show()
                        } else {
                            loadBook(book)
                        }
                    }

                    override
                    fun onLongClickItem(view: View, position: Int) {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            vibrator.vibrate(
//                                VibrationEffect.createOneShot(
//                                    500,
//                                    VibrationEffect.DEFAULT_AMPLITUDE
//                                )
//                            )
//                        } else {
//                            //deprecated in API 26
//                            vibrator.vibrate(500)
//                        }
                        removeBook(position)
                    }
                })
        )
        fab.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                val extraMimeTypes = arrayOf("application/pdf", "application/rtf", "text/plain")
                putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
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

    fun loadBook(book: Book, goal_id: Int = 3, goal_value: String? = null) {
        indeterminateBar.visibility = View.VISIBLE

        val intent = if (book.format == BookFormat.PDF.format) {
            Intent(applicationContext, PdfExtractor::class.java)
        } else {
            Intent(applicationContext, ReadBookActivity::class.java)
        }

        intent.putExtra("BOOK_ID", book.id.toString())
        intent.putExtra("BOOK_URI", book.uri)
        intent.putExtra("BOOK_FORMAT", book.format)
        intent.putExtra("BOOK_PAGE_COUNTER", book.page_counter)
        val bundle = Bundle()
        bundle.putInt("GOAL_ID", goal_id)
        bundle.putString("GOAL_RES", goal_value)
        intent.putExtras(bundle)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { bookUri ->
                val book = createBook(bookUri)
                bookViewModel.insert(book)
            }

        }
    }

    private fun parseFullNameWithExtensionFormat(bookUri: Uri): String {
        val cursor = contentResolver.query(bookUri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: return bookUri.path!!.substringAfterLast("/")
        cursor.moveToFirst()
        val bookName = cursor.getString(nameIndex)
        cursor.close()
        return bookName
    }

    private fun createBook(bookUri: Uri): Book {
        val uri = bookUri.toString()
        val fullName = parseFullNameWithExtensionFormat(bookUri)
        val name = fullName.substringBefore(".")
        val format = fullName.substringAfterLast(".")

        return Book(name = name, format = format, uri = uri)
    }

}
