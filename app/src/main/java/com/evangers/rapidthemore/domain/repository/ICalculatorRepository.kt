package com.evangers.rapidthemore.domain.repository

import java.math.BigDecimal

interface ICalculatorRepository {
    fun addUnitDigitFrom(digit: Int)
    fun removeLastDigit()
    fun getAmount(): BigDecimal
    fun clearAmount()
}