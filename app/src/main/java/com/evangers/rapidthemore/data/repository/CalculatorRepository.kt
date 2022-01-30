package com.evangers.rapidthemore.data.repository

import com.evangers.rapidthemore.domain.repository.ICalculatorRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculatorRepository @Inject constructor(
) : ICalculatorRepository {

    private var amount = BigDecimal.ZERO

    override fun addUnitDigitFrom(digit: Int) {
        amount = amount.multiply(BigDecimal.TEN).plus(BigDecimal(digit))
    }

    override fun removeLastDigit() {
        val value = amount.divide(BigDecimal.TEN, RoundingMode.DOWN)
        amount = if (value >= BigDecimal.ZERO) {
            value
        } else {
            BigDecimal.ZERO
        }
    }

    override fun getAmount(): BigDecimal {
        return amount
    }

    override fun clearAmount() {
        amount = BigDecimal.ZERO
    }
}