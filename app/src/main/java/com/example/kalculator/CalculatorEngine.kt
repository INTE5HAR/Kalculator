package com.example.kalculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Stack

/**
 * CalculatorEngine handles arithmetic expression construction,
 * mathematical parsing using the Shunting-Yard algorithm, and evaluation.
 */
class CalculatorEngine {

    var expression: String = ""
        private set

    var isCalculated: Boolean = false
        private set

    companion object {
        const val OP_ADD = "+"
        const val OP_SUB = "−"
        const val OP_MUL = "×"
        const val OP_DIV = "÷"
        const val OP_PCT = "%"
    }

    /**
     * Appends a digit ('0'-'9') to the current expression.
     */
    fun inputDigit(digit: Char): String {
        if (isCalculated) {
            expression = digit.toString()
            isCalculated = false
        } else {
            // Prevent multiple leading zeros like "00"
            if (expression == "0") {
                expression = digit.toString()
            } else {
                expression += digit
            }
        }
        return expression
    }

    /**
     * Appends a decimal point, preventing duplicate decimals in the current number token.
     */
    fun inputDecimal(): String {
        if (isCalculated) {
            expression = "0."
            isCalculated = false
            return expression
        }

        if (expression.isEmpty()) {
            expression = "0."
            return expression
        }

        val lastChar = expression.last()
        if (isOperator(lastChar) || lastChar == '(') {
            expression += "0."
            return expression
        }

        // Find the start of the current number segment to verify no decimal already exists
        val currentNumber = getCurrentNumberSegment()
        if (!currentNumber.contains('.')) {
            expression += "."
        }

        return expression
    }

    /**
     * Appends or replaces an arithmetic operator (+, −, ×, ÷).
     */
    fun inputOperator(op: String): String {
        if (expression.isEmpty()) {
            if (op == OP_SUB) {
                expression = OP_SUB
            }
            return expression
        }

        if (isCalculated) {
            isCalculated = false
        }

        val lastChar = expression.last()

        // If the last character is a decimal point, strip it before adding operator
        if (lastChar == '.') {
            expression = expression.dropLast(1)
        }

        val lastOp = expression.takeLast(1)
        if (isOperator(lastOp[0])) {
            // Replace the last operator with the new one
            expression = expression.dropLast(1) + op
        } else if (lastChar != '(') {
            expression += op
        }

        return expression
    }

    /**
     * Appends percentage symbol '%'.
     */
    fun inputPercent(): String {
        if (expression.isEmpty()) return expression

        if (isCalculated) {
            isCalculated = false
        }

        val lastChar = expression.last()
        if (lastChar.isDigit() || lastChar == ')') {
            expression += OP_PCT
        }
        return expression
    }

    /**
     * Smartly inputs parentheses:
     * Inserts ')' if there are open parentheses to close and preceded by a number or ')'.
     * Otherwise inserts '('.
     */
    fun inputParentheses(): String {
        if (isCalculated) {
            expression = "("
            isCalculated = false
            return expression
        }

        if (expression.isEmpty()) {
            expression = "("
            return expression
        }

        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }
        val lastChar = expression.last()

        if (openCount > closeCount && (lastChar.isDigit() || lastChar == ')' || lastChar == '%')) {
            expression += ")"
        } else {
            if (lastChar.isDigit() || lastChar == ')' || lastChar == '%') {
                expression += "$OP_MUL("
            } else {
                expression += "("
            }
        }

        return expression
    }

    /**
     * Removes the last character in the expression.
     */
    fun backspace(): String {
        if (isCalculated) {
            clear()
            return expression
        }

        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
        return expression
    }

    /**
     * Clears the current expression and reset state.
     */
    fun clear() {
        expression = ""
        isCalculated = false
    }

    /**
     * Sets expression directly (e.g. restoring state).
     */
    fun setExpressionText(expr: String) {
        expression = expr
        isCalculated = false
    }

    /**
     * Completes the calculation when '=' is pressed.
     * Returns CalculationResult.
     */
    fun calculate(): CalculationResult {
        if (expression.isEmpty()) {
            return CalculationResult.Empty
        }

        val result = evaluate(expression)
        if (result is CalculationResult.Success) {
            expression = result.formattedResult
            isCalculated = true
        }
        return result
    }

    /**
     * Live preview evaluation of the current expression.
     */
    fun evaluateLive(): CalculationResult {
        if (expression.isEmpty()) {
            return CalculationResult.Empty
        }

        val lastChar = expression.last()
        // If expression ends with an open operator or '(', don't evaluate live error yet
        if (isOperator(lastChar) || lastChar == '(' || lastChar == '.') {
            // Try evaluating up to the operator if possible
            val trimmed = expression.dropLast(1)
            if (trimmed.isNotEmpty() && !isOperator(trimmed.last()) && trimmed.last() != '(') {
                return evaluate(trimmed)
            }
            return CalculationResult.Incomplete
        }

        return evaluate(expression)
    }

    /**
     * Evaluates a mathematical expression string using Shunting-yard algorithm.
     */
    fun evaluate(rawExpr: String): CalculationResult {
        return try {
            val normalized = normalizeExpression(rawExpr)
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) return CalculationResult.Empty

            val rpn = shuntingYard(tokens)
            val value = evaluateRpn(rpn)

            if (value.isNaN()) {
                CalculationResult.Error("Error")
            } else if (value.isInfinite()) {
                CalculationResult.Error("Cannot divide by 0")
            } else {
                CalculationResult.Success(value, formatNumber(value))
            }
        } catch (e: ArithmeticException) {
            CalculationResult.Error(e.message ?: "Calculation error")
        } catch (e: Exception) {
            CalculationResult.Error("Invalid format")
        }
    }

    private fun normalizeExpression(expr: String): String {
        return expr
            .replace(OP_SUB, "-")
            .replace(OP_MUL, "*")
            .replace(OP_DIV, "/")
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/'
    }

    private fun getCurrentNumberSegment(): String {
        val idx = expression.indexOfLast { isOperator(it) || it == '(' || it == ')' }
        return if (idx == -1) expression else expression.substring(idx + 1)
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            when {
                c.isWhitespace() -> {
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c == '-' -> {
                    // Check if this is unary minus
                    val isUnary = tokens.isEmpty() ||
                            tokens.last() in listOf("+", "-", "*", "/", "(")
                    if (isUnary) {
                        // Unary minus token or attach to number
                        if (i + 1 < len && (expr[i + 1].isDigit() || expr[i + 1] == '.')) {
                            val sb = StringBuilder("-")
                            i++
                            while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                                sb.append(expr[i])
                                i++
                            }
                            tokens.add(sb.toString())
                        } else {
                            tokens.add("u-")
                            i++
                        }
                    } else {
                        tokens.add("-")
                        i++
                    }
                }
                c in "+*/()%" -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    i++
                }
            }
        }

        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()

        fun precedence(op: String): Int = when (op) {
            "%" -> 4
            "u-" -> 3
            "*", "/" -> 2
            "+", "-" -> 1
            else -> 0
        }

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> {
                    output.add(token)
                }
                token == "(" -> {
                    stack.push(token)
                }
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty() && stack.peek() == "(") {
                        stack.pop()
                    }
                }
                token == "%" -> {
                    // Postfix percent: apply immediately to previous number
                    output.add(token)
                }
                else -> {
                    // Operator
                    while (stack.isNotEmpty() && stack.peek() != "(" &&
                        precedence(stack.peek()) >= precedence(token)
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.pop()
            if (top != "(") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>): Double {
        val stack = Stack<Double>()

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
            } else if (token == "u-") {
                if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                val a = stack.pop()
                stack.push(-a)
            } else if (token == "%") {
                if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                val a = stack.pop()
                stack.push(a / 100.0)
            } else {
                if (stack.size < 2) throw IllegalArgumentException("Invalid expression")
                val b = stack.pop()
                val a = stack.pop()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> {
                        if (b == 0.0) throw ArithmeticException("Cannot divide by 0")
                        a / b
                    }
                    else -> throw IllegalArgumentException("Unknown operator: $token")
                }
                stack.push(res)
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid syntax")
        }
        return stack.pop()
    }

    /**
     * Formats numbers cleanly without trailing .0 and with scientific notation for huge numbers.
     */
    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        val abs = kotlin.math.abs(value)
        if (abs >= 1e12 || (abs > 0 && abs < 1e-6)) {
            val df = DecimalFormat("0.######E0")
            return df.format(value)
        }

        return try {
            val bd = BigDecimal(value.toString())
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            bd.toPlainString()
        } catch (e: Exception) {
            val df = DecimalFormat("#.##########")
            df.format(value)
        }
    }
}

sealed class CalculationResult {
    data class Success(val value: Double, val formattedResult: String) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
    object Incomplete : CalculationResult()
    object Empty : CalculationResult()
}
