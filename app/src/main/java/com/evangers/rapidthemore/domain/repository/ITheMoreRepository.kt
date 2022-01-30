package com.evangers.rapidthemore.domain.repository

import java.math.BigDecimal

interface ITheMoreRepository {
    fun getRatio(amount: BigDecimal): Float
}