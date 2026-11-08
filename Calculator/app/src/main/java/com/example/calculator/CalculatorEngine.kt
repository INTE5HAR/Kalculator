/*
 * Built by: Inteshar Alam
 * Date: 11-nov-2026
 * Note: Developed as part of the Mobile Programming coursework.
 */

package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack

sealed class CalculationResult {
    data class Success(val value: BigDecimal) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}

data class CalculatorState(
    val displayText: String = "",
    val expressionText: String = "",
    val clearButtonText: String = "AC",
    val hasError: Boolean = false,
)

class CalculatorEngine {

    companion object {
        private val MATH_CONTEXT = MathContext(16, RoundingMode.HALF_UP)
        private val MAX_PLAIN_VALUE = BigDecimal("1000000000000")
        private val MIN_PLAIN_VALUE = BigDecimal("0.0000001")
    }

    private var expression: String = ""
    private var liveResult: String = ""
    private var hasError: Boolean = false
    private var errorMessage: String? = null
    private var isEvaluated: Boolean = false

    private fun resetAll() {
        expression = ""
        liveResult = ""
        hasError = false
        errorMessage = null
        isEvaluated = false
    }

    fun getState(): CalculatorState {
        val display = if (hasError) {
            errorMessage ?: "Error"
        } else {
            liveResult
        }

        val clearText = if (expression.isNotEmpty() || hasError) "C" else "AC"

        return CalculatorState(
            displayText = display,
            expressionText = formatExpressionForDisplay(expression),
            clearButtonText = clearText,
            hasError = hasError,
        )
    }

    fun onDigit(digit: Char) {
        if (!digit.isDigit()) return

        if (hasError) {
            resetAll()
        }

        if (isEvaluated) {
            expression = digit.toString()
            isEvaluated = false
        } else {
            if (expression == "0") {
                expression = digit.toString()
            } else {
                expression += digit
            }
        }
        updateLivePreview()
    }

    fun onDecimalPoint() {
        if (hasError) {
            resetAll()
        }

        if (isEvaluated) {
            expression = "0."
            isEvaluated = false
        } else {
            val lastToken = getLastToken(expression)
            if (!lastToken.contains('.')) {
                expression += if (lastToken.isEmpty() || isOperatorChar(lastToken.last()) || (lastToken.last() == '(')) {
                    "0."
                } else {
                    "."
                }
            }
        }
        updateLivePreview()
    }

    fun onParentheses() {
        if (hasError) {
            resetAll()
        }

        if (isEvaluated) {
            expression = "("
            isEvaluated = false
            updateLivePreview()
            return
        }

        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }

        if (expression.isEmpty()) {
            expression = "("
        } else {
            val lastChar = expression.last()
            expression += if (isOperatorChar(lastChar) || (lastChar == '(')) {
                "("
            } else if (isClosingParenValid(lastChar, openCount, closeCount)) {
                ")"
            } else {
                "×("
            }
        }
        updateLivePreview()
    }

    private fun isClosingParenValid(lastChar: Char, openCount: Int, closeCount: Int): Boolean {
        val isOperandOrClose = lastChar.isDigit() || (lastChar == '.') || (lastChar == '%') || (lastChar == ')')
        return isOperandOrClose && (openCount > closeCount)
    }

    fun onOperation(opSymbol: String) {
        if (hasError) {
            resetAll()
        }

        isEvaluated = false

        if (expression.isEmpty()) {
            if ((opSymbol == "−") || (opSymbol == "-")) {
                expression = "−"
            }
            return
        }

        val lastChar = expression.last()
        if (isOperatorChar(lastChar)) {
            expression = expression.dropLast(1) + opSymbol
        } else {
            expression += opSymbol
        }
        updateLivePreview()
    }

    fun onPercent() {
        if (hasError) {
            resetAll()
        }

        if (expression.isNotEmpty()) {
            val lastChar = expression.last()
            if ((lastChar.isDigit()) || (lastChar == ')')) {
                expression += "%"
            }
        }
        updateLivePreview()
    }

    fun onBackspace() {
        if (hasError) {
            resetAll()
            return
        }

        if (isEvaluated) {
            resetAll()
            return
        }

        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
        updateLivePreview()
    }

    fun onClear() {
        resetAll()
    }

    fun onAllClear() {
        resetAll()
    }

    fun onEquals() {
        if (hasError || expression.isEmpty()) return

        when (val result = evaluate(expression)) {
            is CalculationResult.Success -> {
                val formatted = formatNumberValue(result.value)
                expression = formatted
                liveResult = ""
                isEvaluated = true
            }
            is CalculationResult.Error -> {
                hasError = true
                errorMessage = result.message
            }
        }
    }

    private fun updateLivePreview() {
        if (expression.isEmpty()) {
            liveResult = ""
            return
        }

        var evalExpr = expression
        while (evalExpr.isNotEmpty() && isOperatorChar(evalExpr.last())) {
            evalExpr = evalExpr.dropLast(1)
        }

        if (evalExpr.isEmpty()) {
            liveResult = ""
            return
        }

        liveResult = when (val result = evaluate(evalExpr)) {
            is CalculationResult.Success -> formatInputForDisplay(formatNumberValue(result.value))
            is CalculationResult.Error -> ""
        }
    }

    private fun isOperatorChar(c: Char): Boolean {
        return (c == '+') || (c == '−') || (c == '-') || (c == '×') || (c == '*') || (c == '÷') || (c == '/')
    }

    private fun getLastToken(expr: String): String {
        var i = expr.length - 1
        while ((i >= 0) && (!isOperatorChar(expr[i])) && (expr[i] != '(') && (expr[i] != ')')) {
            i--
        }
        return expr.substring(i + 1)
    }

    private fun formatExpressionForDisplay(expr: String): String {
        return expr
    }

    fun evaluate(exprStr: String): CalculationResult {
        return try {
            val tokens = tokenize(exprStr)
            if (tokens.isEmpty()) return CalculationResult.Error("Invalid Expression")
            val rpn = infixToRpn(tokens)
            val result = evaluateRpn(rpn)
            CalculationResult.Success(result)
        } catch (_: ArithmeticException) {
            CalculationResult.Error("Cannot divide by zero")
        } catch (_: Exception) {
            CalculationResult.Error("Error")
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val n = expr.length

        while (i < n) {
            val c = expr[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            if ((c.isDigit()) || (c == '.')) {
                val sb = StringBuilder()
                while ((i < n) && ((expr[i].isDigit()) || (expr[i] == '.'))) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                continue
            }

            if ((c == '−') || (c == '-')) {
                val prevToken = tokens.lastOrNull()
                val isUnary = (prevToken == null) || (prevToken == "(") || isOperatorToken(prevToken)
                if (isUnary) {
                    if (i + 1 < n && (expr[i + 1].isDigit() || expr[i + 1] == '.')) {
                        val sb = StringBuilder("-")
                        i++
                        while (i < n && (expr[i].isDigit() || expr[i] == '.')) {
                            sb.append(expr[i])
                            i++
                        }
                        tokens.add(sb.toString())
                        continue
                    }
                }
                tokens.add("−")
                i++
                continue
            }

            if (c == '%') {
                var nextIndex = i + 1
                while (nextIndex < n && expr[nextIndex].isWhitespace()) {
                    nextIndex++
                }
                val nextChar = expr.getOrNull(nextIndex)
                val isPostfix = nextChar == null ||
                        nextChar == '+' ||
                        nextChar == '−' ||
                        nextChar == '-' ||
                        nextChar == '×' ||
                        nextChar == '*' ||
                        nextChar == '÷' ||
                        nextChar == '/' ||
                        nextChar == ')'

                tokens.add(if (isPostfix) "PERCENT" else "%")
                i++
                continue
            }

            if (c == '+' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '(' || c == ')') {
                val token = when (c) {
                    '*' -> "×"
                    '/' -> "÷"
                    else -> c.toString()
                }
                tokens.add(token)
                i++
                continue
            }

            i++
        }
        return tokens
    }

    private fun isOperatorToken(t: String): Boolean {
        return t == "+" || t == "−" || t == "×" || t == "÷" || t == "%"
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "×", "÷", "%" -> 2
            "+", "−" -> 1
            else -> 0
        }
    }

    private fun infixToRpn(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()

        for (token in tokens) {
            when {
                token.toBigDecimalOrNull() != null -> output.add(token)
                token == "PERCENT" -> output.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty()) stack.pop()
                }
                isOperatorToken(token) -> {
                    while (stack.isNotEmpty() && precedence(stack.peek()) >= precedence(token) && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.pop()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluateRpn(tokens: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()

        for (token in tokens) {
            val num = token.toBigDecimalOrNull()
            if (num != null) {
                stack.push(num)
            } else if (token == "PERCENT") {
                if (stack.isEmpty()) {
                    throw IllegalArgumentException("Invalid Expression")
                }
                val value = stack.pop()
                stack.push(value.divide(BigDecimal(100), MATH_CONTEXT))
            } else if (isOperatorToken(token)) {
                if (stack.size < 2) {
                    throw IllegalArgumentException("Invalid Expression")
                }

                val b = stack.pop()
                val a = stack.pop()

                val res = when (token) {
                    "+" -> a.add(b, MATH_CONTEXT)
                    "−" -> a.subtract(b, MATH_CONTEXT)
                    "×" -> a.multiply(b, MATH_CONTEXT)
                    "%" -> a.divide(BigDecimal(100), MATH_CONTEXT).multiply(b, MATH_CONTEXT)
                    "÷" -> {
                        if (b.compareTo(BigDecimal.ZERO) == 0) {
                            throw ArithmeticException("Divide by zero")
                        }
                        a.divide(b, 12, RoundingMode.HALF_UP)
                    }
                    else -> BigDecimal.ZERO
                }

                stack.push(res)
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid Expression")
        }

        return stack.pop()
    }

    fun formatNumberValue(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        val stripped = value.stripTrailingZeros()
        val absVal = value.abs()
        return if (absVal >= MAX_PLAIN_VALUE || (absVal > BigDecimal.ZERO && absVal < MIN_PLAIN_VALUE)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            df.format(value)
        } else {
            stripped.toPlainString()
        }
    }

    fun formatInputForDisplay(input: String): String {
        if (input.isEmpty()) return ""
        if (input.contains('E') || input.contains('e')) return input

        val isNegative = input.startsWith("-")
        val cleanInput = if (isNegative) input.substring(1) else input

        val parts = cleanInput.split(".", limit = 2)
        val intPart = parts[0]
        val hasDot = cleanInput.contains(".")
        val decPart = if (parts.size > 1) parts[1] else ""

        val formattedInt = try {
            if (intPart.isEmpty()) "0" else {
                val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
                val df = DecimalFormat("#,##0", symbols)
                df.format(BigDecimal(intPart))
            }
        } catch (_: Exception) {
            intPart
        }

        return buildString {
            if (isNegative) append("-")
            append(formattedInt)
            if (hasDot) {
                append(".")
                append(decPart)
            }
        }
    }

    fun saveState(): BundleData {
        return BundleData(
            expression = expression,
            liveResult = liveResult,
            hasError = hasError,
            errorMessage = errorMessage,
            isEvaluated = isEvaluated,
        )
    }

    fun restoreState(data: BundleData) {
        expression = data.expression
        liveResult = data.liveResult
        hasError = data.hasError
        errorMessage = data.errorMessage
        isEvaluated = data.isEvaluated
    }

    data class BundleData(
        val expression: String,
        val liveResult: String,
        val hasError: Boolean,
        val errorMessage: String?,
        val isEvaluated: Boolean,
    ) : java.io.Serializable
}
