package com.athkar.alyawm

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaMath {
    const val KAABA_LAT = 21.422487
    const val KAABA_LNG = 39.826206

    fun bearing(lat: Double, lng: Double): Double {
        val p1 = Math.toRadians(lat)
        val p2 = Math.toRadians(KAABA_LAT)
        val dl = Math.toRadians(KAABA_LNG - lng)
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return norm360(Math.toDegrees(atan2(y, x)))
    }

    fun distanceKm(lat: Double, lng: Double): Double {
        val r = 6371.0088
        val p1 = Math.toRadians(lat)
        val p2 = Math.toRadians(KAABA_LAT)
        val dp = Math.toRadians(KAABA_LAT - lat)
        val dl = Math.toRadians(KAABA_LNG - lng)
        val a = sin(dp / 2) * sin(dp / 2) +
            cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
        return 2 * r * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }

    fun norm360(n: Double): Double {
        val m = n % 360.0
        return if (m < 0) m + 360.0 else m
    }
}
