package kr.evangers.rapidthemore.data.repository

import kr.evangers.rapidthemore.domain.repository.ITheMoreRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class TheMoreRepository @Inject constructor(
) : ITheMoreRepository {
    override fun getRatio(amount: BigDecimal): Float {
        val value = amount.minus(BigDecimal(5000))
        return if (value > BigDecimal.ZERO) {
            val remainder = value.remainder(BigDecimal(1000))
            val ratio = remainder.multiply(BigDecimal(100))
                .divide(amount, 2, RoundingMode.HALF_UP)
            ratio.toFloat()
        } else {
            0.0f
        }
    }
}