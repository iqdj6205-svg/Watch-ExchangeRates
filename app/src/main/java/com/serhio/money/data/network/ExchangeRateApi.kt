package com.serhio.money.data.network

import com.serhio.money.data.network.dto.ExchangeRateResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Интерфейс для работы с API exchangerate.fun
 */
interface ExchangeRateApi {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") baseCurrency: String
    ): Response<ExchangeRateResponseDto>

    companion object {
        const val BASE_URL = "https://api.exchangerate.fun/"
    }
}
