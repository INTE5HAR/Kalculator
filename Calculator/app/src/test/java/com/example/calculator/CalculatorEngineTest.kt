/*
 * Built by: Inteshar Alam
 * Date: 11-nov-2026
 * Note: Developed as part of the Mobile Programming coursework.
 */

package com.example.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    private fun inputString(str: String) {
        for (ch in str) {
            when (ch) {
                '.' -> engine.onDecimalPoint()
                '(' -> engine.onParentheses()
                ')' -> engine.onParentheses()
                '+' -> engine.onOperation("+")
                '−', '-' -> engine.onOperation("−")
                '×', '*' -> engine.onOperation("×")
                '÷', '/' -> engine.onOperation("÷")
                '%' -> engine.onPercent()
                else -> if (ch.isDigit()) engine.onDigit(ch)
            }
        }
    }

    @Test
    fun testInitialState() {
        val (displayText, expressionText, clearButtonText, hasError) = engine.getState()
        assertEquals("", displayText)
        assertEquals("", expressionText)
        assertEquals("AC", clearButtonText)
        assertFalse(hasError)
    }

    @Test
    fun testExpressionTypingAndLivePreview() {
        inputString("−4×6")
        val (displayText, expressionText) = engine.getState()
        assertEquals("−4×6", expressionText)
        assertEquals("-24", displayText)
    }

    @Test
    fun testParenthesesEvaluation() {
        inputString("(12+8)×3")
        val (displayText, expressionText) = engine.getState()
        assertEquals("(12+8)×3", expressionText)
        assertEquals("60", displayText)

        engine.onEquals()
        val (finalDisplay, finalExpr) = engine.getState()
        assertEquals("60", finalExpr)
        assertEquals("", finalDisplay)
    }

    @Test
    fun testPercentageExpressions() {
        var res = engine.evaluate("8%")
        assertTrue(res is CalculationResult.Success)
        assertEquals("0.08", engine.formatNumberValue((res as CalculationResult.Success).value))

        res = engine.evaluate("50%200")
        assertTrue(res is CalculationResult.Success)
        assertEquals("100", engine.formatNumberValue((res as CalculationResult.Success).value))

        res = engine.evaluate("25%80")
        assertTrue(res is CalculationResult.Success)
        assertEquals("20", engine.formatNumberValue((res as CalculationResult.Success).value))

        res = engine.evaluate("10%50")
        assertTrue(res is CalculationResult.Success)
        assertEquals("5", engine.formatNumberValue((res as CalculationResult.Success).value))

        res = engine.evaluate("200+10%")
        assertTrue(res is CalculationResult.Success)
        assertEquals("200.1", engine.formatNumberValue((res as CalculationResult.Success).value))
    }

    @Test
    fun testDecimalPrecisionAddition() {
        inputString("0.1+0.2")
        val (displayText) = engine.getState()
        assertEquals("0.3", displayText)
    }

    @Test
    fun testDivisionByZero() {
        inputString("42÷0")
        engine.onEquals()

        val (displayText, _, _, hasError) = engine.getState()
        assertTrue(hasError)
        assertEquals("Cannot divide by zero", displayText)
    }

    @Test
    fun testBackspace() {
        inputString("1234")
        engine.onBackspace()
        assertEquals("123", engine.getState().expressionText)
    }

    @Test
    fun testClear() {
        inputString("123+45")
        engine.onClear()
        val (displayText, expressionText, clearButtonText) = engine.getState()
        assertEquals("", expressionText)
        assertEquals("", displayText)
        assertEquals("AC", clearButtonText)
    }
}
