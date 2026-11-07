package com.example.kalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var calculator: CalculatorEngine

    @Before
    fun setUp() {
        calculator = CalculatorEngine()
    }

    @Test
    fun testSimpleAddition() {
        calculator.inputDigit('2')
        calculator.inputOperator(CalculatorEngine.OP_ADD)
        calculator.inputDigit('3')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("5", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testSimpleSubtraction() {
        calculator.inputDigit('9')
        calculator.inputOperator(CalculatorEngine.OP_SUB)
        calculator.inputDigit('4')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("5", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testMultiplicationAndPrecedence() {
        // 2 + 3 × 4 = 14
        calculator.inputDigit('2')
        calculator.inputOperator(CalculatorEngine.OP_ADD)
        calculator.inputDigit('3')
        calculator.inputOperator(CalculatorEngine.OP_MUL)
        calculator.inputDigit('4')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("14", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testParenthesesPrecedence() {
        // (2 + 3) × 4 = 20
        calculator.inputParentheses() // opens '('
        calculator.inputDigit('2')
        calculator.inputOperator(CalculatorEngine.OP_ADD)
        calculator.inputDigit('3')
        calculator.inputParentheses() // closes ')'
        calculator.inputOperator(CalculatorEngine.OP_MUL)
        calculator.inputDigit('4')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("20", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testDecimalFloatingPointAccuracy() {
        // 0.1 + 0.2 = 0.3
        calculator.inputDigit('0')
        calculator.inputDecimal()
        calculator.inputDigit('1')
        calculator.inputOperator(CalculatorEngine.OP_ADD)
        calculator.inputDigit('0')
        calculator.inputDecimal()
        calculator.inputDigit('2')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("0.3", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testDivisionByZero() {
        calculator.inputDigit('5')
        calculator.inputOperator(CalculatorEngine.OP_DIV)
        calculator.inputDigit('0')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Error)
        assertEquals("Cannot divide by 0", (result as CalculationResult.Error).message)
    }

    @Test
    fun testOperatorReplacement() {
        // Type 5, then +, then ×, then 2 -> should be 5 × 2 = 10
        calculator.inputDigit('5')
        calculator.inputOperator(CalculatorEngine.OP_ADD)
        calculator.inputOperator(CalculatorEngine.OP_MUL)
        calculator.inputDigit('2')

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("10", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testPercentage() {
        // 50% = 0.5
        calculator.inputDigit('5')
        calculator.inputDigit('0')
        calculator.inputPercent()

        val result = calculator.calculate()
        assertTrue(result is CalculationResult.Success)
        assertEquals("0.5", (result as CalculationResult.Success).formattedResult)
    }

    @Test
    fun testBackspace() {
        calculator.inputDigit('1')
        calculator.inputDigit('2')
        calculator.inputDigit('3')
        calculator.backspace()

        assertEquals("12", calculator.expression)
    }

    @Test
    fun testClear() {
        calculator.inputDigit('9')
        calculator.inputOperator(CalculatorEngine.OP_ADD)
        calculator.inputDigit('9')
        calculator.clear()

        assertEquals("", calculator.expression)
    }
}
