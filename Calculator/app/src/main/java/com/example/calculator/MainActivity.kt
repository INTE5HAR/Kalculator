@file:Suppress("UsePropertyAccessSyntax")

/*
 * Built by: Inteshar Alam
 * Date: 11-nov-2026
 * Note: Developed as part of the Mobile Programming coursework.
 */

package com.example.calculator

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private val engine = CalculatorEngine()

    private lateinit var tvExpression: TextView
    private lateinit var tvDisplay: TextView
    private lateinit var scrollExpression: HorizontalScrollView
    private lateinit var scrollDisplay: HorizontalScrollView

    private lateinit var btnClear: AppCompatButton
    private lateinit var btnParentheses: AppCompatButton
    private lateinit var btnPercent: AppCompatButton
    private lateinit var btnDivide: AppCompatButton
    private lateinit var btnMultiply: AppCompatButton
    private lateinit var btnSubtract: AppCompatButton
    private lateinit var btnAdd: AppCompatButton
    private lateinit var btnEquals: AppCompatButton
    private lateinit var btnDot: AppCompatButton
    private lateinit var btnBackspace: AppCompatButton

    private lateinit var digitButtons: List<AppCompatButton>

    companion object {
        private const val KEY_CALC_STATE = "key_calc_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupListeners()

        if (savedInstanceState != null) {
            val bundleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getSerializable(KEY_CALC_STATE, CalculatorEngine.BundleData::class.java)
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getSerializable(KEY_CALC_STATE) as? CalculatorEngine.BundleData
            }
            bundleData?.let { engine.restoreState(it) }
        }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_CALC_STATE, engine.saveState())
    }

    private fun bindViews() {
        tvExpression = findViewById(R.id.tvExpression)
        tvDisplay = findViewById(R.id.tvDisplay)
        scrollExpression = findViewById(R.id.scrollExpression)
        scrollDisplay = findViewById(R.id.scrollDisplay)

        btnClear = findViewById(R.id.btnClear)
        btnParentheses = findViewById(R.id.btnParentheses)
        btnPercent = findViewById(R.id.btnPercent)
        btnDivide = findViewById(R.id.btnDivide)
        btnMultiply = findViewById(R.id.btnMultiply)
        btnSubtract = findViewById(R.id.btnSubtract)
        btnAdd = findViewById(R.id.btnAdd)
        btnEquals = findViewById(R.id.btnEquals)
        btnDot = findViewById(R.id.btnDot)
        btnBackspace = findViewById(R.id.btnBackspace)

        val btn0 = findViewById<AppCompatButton>(R.id.btn0)
        val btn1 = findViewById<AppCompatButton>(R.id.btn1)
        val btn2 = findViewById<AppCompatButton>(R.id.btn2)
        val btn3 = findViewById<AppCompatButton>(R.id.btn3)
        val btn4 = findViewById<AppCompatButton>(R.id.btn4)
        val btn5 = findViewById<AppCompatButton>(R.id.btn5)
        val btn6 = findViewById<AppCompatButton>(R.id.btn6)
        val btn7 = findViewById<AppCompatButton>(R.id.btn7)
        val btn8 = findViewById<AppCompatButton>(R.id.btn8)
        val btn9 = findViewById<AppCompatButton>(R.id.btn9)

        digitButtons = listOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
    }

    private fun setupListeners() {
        digitButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                vibrate(it)
                engine.onDigit('0' + index)
                updateUI()
            }
        }

        btnDot.setOnClickListener {
            vibrate(it)
            engine.onDecimalPoint()
            updateUI()
        }

        btnParentheses.setOnClickListener {
            vibrate(it)
            engine.onParentheses()
            updateUI()
        }

        btnDivide.setOnClickListener {
            vibrate(it)
            engine.onOperation("÷")
            updateUI()
        }
        btnMultiply.setOnClickListener {
            vibrate(it)
            engine.onOperation("×")
            updateUI()
        }
        btnSubtract.setOnClickListener {
            vibrate(it)
            engine.onOperation("−")
            updateUI()
        }
        btnAdd.setOnClickListener {
            vibrate(it)
            engine.onOperation("+")
            updateUI()
        }

        btnEquals.setOnClickListener {
            vibrate(it)
            engine.onEquals()
            updateUI()
        }

        btnClear.setOnClickListener {
            vibrate(it)
            engine.onClear()
            updateUI()
        }
        btnClear.setOnLongClickListener {
            vibrate(it)
            engine.onAllClear()
            updateUI()
            true
        }

        btnBackspace.setOnClickListener {
            vibrate(it)
            engine.onBackspace()
            updateUI()
        }
        btnBackspace.setOnLongClickListener {
            vibrate(it)
            engine.onAllClear()
            updateUI()
            true
        }

        btnPercent.setOnClickListener {
            vibrate(it)
            engine.onPercent()
            updateUI()
        }

        tvExpression.setOnLongClickListener {
            copyToClipboard(tvExpression.text.toString())
            true
        }
        tvDisplay.setOnLongClickListener {
            copyToClipboard(tvDisplay.text.toString())
            true
        }

        setupDisplaySwipeGesture()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDisplaySwipeGesture() {
        val swipeThreshold = 50
        val swipeVelocityThreshold = 50

        val gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float,
                ): Boolean {
                    if (e1 == null) return false
                    val diffX = e2.x - e1.x
                    if ((abs(diffX) > swipeThreshold) && (abs(velocityX) > swipeVelocityThreshold)) {
                        vibrate(tvDisplay)
                        engine.onBackspace()
                        updateUI()
                        return true
                    }
                    return false
                }
            },
        )

        val touchListener = View.OnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        scrollDisplay.setOnTouchListener(touchListener)
        scrollExpression.setOnTouchListener(touchListener)
    }

    private fun updateUI() {
        val (displayText, expressionText, clearButtonText) = engine.getState()

        tvExpression.text = expressionText
        tvDisplay.text = displayText
        btnClear.text = clearButtonText

        scrollDisplay.post {
            scrollDisplay.scrollTo(tvDisplay.width, 0)
        }
        scrollExpression.post {
            scrollExpression.scrollTo(tvExpression.width, 0)
        }
    }

    private fun vibrate(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun copyToClipboard(text: String) {
        if (text.isEmpty()) return
        val cleanText = text.replace(",", "")
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", cleanText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }
}
