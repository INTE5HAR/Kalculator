package com.example.kalculator

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.HorizontalScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.kalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calculator = CalculatorEngine()

    companion object {
        private const val KEY_EXPRESSION = "key_expression"
        private const val KEY_RESULT = "key_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()

        if (savedInstanceState != null) {
            val savedExpr = savedInstanceState.getString(KEY_EXPRESSION, "")
            val savedResult = savedInstanceState.getString(KEY_RESULT, "")
            calculator.setExpressionText(savedExpr)
            binding.tvExpression.text = savedExpr
            binding.tvResult.text = savedResult
        } else {
            updateDisplay()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EXPRESSION, calculator.expression)
        outState.putString(KEY_RESULT, binding.tvResult.text.toString())
    }

    private fun setupListeners() {
        // Digits
        val digitButtons = mapOf(
            binding.btn0 to '0',
            binding.btn1 to '1',
            binding.btn2 to '2',
            binding.btn3 to '3',
            binding.btn4 to '4',
            binding.btn5 to '5',
            binding.btn6 to '6',
            binding.btn7 to '7',
            binding.btn8 to '8',
            binding.btn9 to '9'
        )

        for ((button, digit) in digitButtons) {
            button.setOnClickListener { view ->
                vibrateTap(view)
                calculator.inputDigit(digit)
                updateDisplay()
            }
        }

        // Operators
        binding.btnPlus.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputOperator(CalculatorEngine.OP_ADD)
            updateDisplay()
        }

        binding.btnSubtract.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputOperator(CalculatorEngine.OP_SUB)
            updateDisplay()
        }

        binding.btnMultiply.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputOperator(CalculatorEngine.OP_MUL)
            updateDisplay()
        }

        binding.btnDivide.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputOperator(CalculatorEngine.OP_DIV)
            updateDisplay()
        }

        binding.btnPercent.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputPercent()
            updateDisplay()
        }

        // Parentheses
        binding.btnParentheses.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputParentheses()
            updateDisplay()
        }

        // Decimal Point
        binding.btnDot.setOnClickListener { view ->
            vibrateTap(view)
            calculator.inputDecimal()
            updateDisplay()
        }

        // Clear (AC)
        binding.btnClear.setOnClickListener { view ->
            vibrateTap(view)
            calculator.clear()
            binding.tvExpression.text = ""
            binding.tvResult.text = ""
        }

        // Backspace
        binding.btnBackspace.setOnClickListener { view ->
            vibrateTap(view)
            calculator.backspace()
            updateDisplay()
        }

        // Long press Backspace to clear all
        binding.btnBackspace.setOnLongClickListener { view ->
            vibrateTap(view)
            calculator.clear()
            binding.tvExpression.text = ""
            binding.tvResult.text = ""
            true
        }

        // Equals (=)
        binding.btnEquals.setOnClickListener { view ->
            vibrateTap(view)
            when (val result = calculator.calculate()) {
                is CalculationResult.Success -> {
                    binding.tvExpression.text = result.formattedResult
                    binding.tvResult.text = ""
                }
                is CalculationResult.Error -> {
                    binding.tvResult.text = result.message
                }
                is CalculationResult.Empty, CalculationResult.Incomplete -> {
                    // Do nothing
                }
            }
            scrollToEnd()
        }
    }

    private fun updateDisplay() {
        binding.tvExpression.text = calculator.expression

        // Compute live result preview
        when (val liveResult = calculator.evaluateLive()) {
            is CalculationResult.Success -> {
                binding.tvResult.text = "= ${liveResult.formattedResult}"
            }
            is CalculationResult.Error -> {
                // Keep previous preview or clear until user finishes inputting
                binding.tvResult.text = ""
            }
            is CalculationResult.Incomplete, CalculationResult.Empty -> {
                binding.tvResult.text = ""
            }
        }

        scrollToEnd()
    }

    private fun scrollToEnd() {
        binding.scrollExpression.post {
            binding.scrollExpression.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
    }

    private fun vibrateTap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
