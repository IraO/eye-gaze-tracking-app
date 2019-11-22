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
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
    internal val REQUEST_PERMISSION = 1

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        val adapter = BookListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookViewModel = ViewModelProviders.of(this).get(BookViewModel::class.java)
        bookViewModel.allBooks.observe(this, Observer { books ->
            books!!.let { adapter.setBooks(it) }
        })

        recyclerView.addOnItemTouchListener(
            RecyclerItemListener (
                this,
                recyclerView,
                object : RecyclerItemListener.RecyclerTouchListener {
                    override
                    fun onClickItem(v: View, position: Int) {
                        val book = bookViewModel.allBooks.value!![position]
                        loadBook(book)
                    }

                    override
                    fun onLongClickItem(v: View, position: Int) {
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
        fab.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, 1)
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
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadBook(book: Book) {
        // TODO
    }

    fun removeBook(position: Int) {
        val builder = android.app.AlertDialog.Builder(this@MainActivity)

        builder.setTitle("Confirm")
        builder.setMessage("Are you sure you want to delete?")
        builder.setPositiveButton("YES") {
            dialog, which ->
                val book = bookViewModel.allBooks.value!![position]
                bookViewModel.delete(book)
        }
        builder.setNegativeButton("NO") {
            dialog, which ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            var path = data!!.dataString
            if (path!!.endsWith(".rtf") || path!!.endsWith(".pdf")) {
                path = path.substringAfterLast(":")
                val filename: String = path.substringAfterLast("/")
                val format: String  = filename.substringAfterLast(".")
                val book = Book(name = filename, path = path, format = format)

                bookViewModel.insert(book)
                loadBook(book)
            }
            else {
                Toast.makeText(
                    applicationContext,
                    R.string.invalid_format,
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                R.string.invalid_path,
                Toast.LENGTH_LONG).show()
        }
    }

}
