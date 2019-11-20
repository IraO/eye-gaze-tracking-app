package com.iorlova.diploma.UI

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.iorlova.diploma.R

import kotlinx.android.synthetic.main.activity_add_book.*

class AddBookActivity : AppCompatActivity() {

    private lateinit var editViewView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        setSupportActionBar(toolbar)

        editViewView = findViewById(R.id.edit_word)
        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editViewView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val book = editViewView.text.toString()
                replyIntent.putExtra("reply", book)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }

    }

}
