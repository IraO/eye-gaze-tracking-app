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
import com.iorlova.diploma.ViewModel.BookViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        internal const val REQUEST_PERMISSION = 1
        internal const val OPEN_DOCUMENT_REQUEST_CODE = 1
    }

    private lateinit var bookViewModel: BookViewModel

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
        bookViewModel.books.observe(this, Observer { books -> books!!.let { adapter.setBooks(it) } })

        recyclerView.addOnItemTouchListener(
            RecyclerItemListener(this, recyclerView,
                object : RecyclerItemListener.RecyclerTouchListener {
                    override
                    fun onClickItem(view: View, position: Int) {
                        val book = bookViewModel.books.value!![position]
                        indeterminateBar.visibility = View.VISIBLE

                        val intent = BookCoordinator.createIntentByBookFormat(book.format, applicationContext)
                        BookCoordinator.putExtrasToIntent(intent, book)
                        BookCoordinator.loadBook(this@MainActivity, intent)
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
                        BookCoordinator.removeBook(position, bookViewModel, this@MainActivity)
                    }
                })
        )
        fab.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                val extraMimeTypes = arrayOf("application/pdf", "text/rtf", "text/plain")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { bookUri ->
                val book = BookFactory.createBook(bookUri, contentResolver)
                bookViewModel.insert(book)
            }

        }
    }

}
