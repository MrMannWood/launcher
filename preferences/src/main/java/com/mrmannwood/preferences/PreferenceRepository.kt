package com.mrmannwood.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import java.lang.Double.longBitsToDouble
import java.lang.Double.doubleToLongBits
import java.lang.Exception
import java.math.BigInteger

class PreferenceRepository(private val dao: PreferenceDao) {

    suspend fun <T> writePref(value: PreferenceValue<T>) {
        dao.setPreference(convertToPreferenceData(value))
    }

    suspend fun <T> getPref(preferenceDef: PreferenceDef<T>): PreferenceValue<T> {
        return convertToPreferenceValue(dao.getPreference(preferenceDef.name), preferenceDef)
    }

    fun <T> watchPref(preferenceDef: PreferenceDef<T>): Flow<PreferenceValue<T>> {
        return dao.watchPreference(preferenceDef.name)
            .transform { raw -> emit(convertToPreferenceValue(raw, preferenceDef)) }
    }

    private fun <T> convertToPreferenceValue(raw: PreferenceData?, def: PreferenceDef<T>): PreferenceValue<T> {
        return if (raw == null) {
            PreferenceValue(def, def.defaultValue)
        } else {
            PreferenceValue(def, byteArrayToValue(def, raw.value))
        }
    }

    private fun <T> convertToPreferenceData(value: PreferenceValue<T>) : PreferenceData {
        return PreferenceData(
            key = value.def.name,
            value = valueToByteArray(value)
        )
    }

    private fun <T> valueToByteArray(value: PreferenceValue<T>): ByteArray? {
        return if (value.value == null) {
            null
        } else {
            valueToByteArray(value.value)
        }
    }

    private fun <T> valueToByteArray(value: T): ByteArray? {
        return when (value) {
            is String -> value.toByteArray()
            is Boolean -> ByteArray(1) { if (value) 1.toByte() else 0.toByte() }
            is Float -> valueToByteArray(value.toDouble())
            is Double -> valueToByteArray(doubleToLongBits(value))
            is Int -> valueToByteArray(value.toLong())
            is Long -> BigInteger.valueOf(value).toByteArray()
            else -> throw Exception("Unknown type ${value!!::class.qualifiedName}")
        }
    }

    private fun <T> byteArrayToValue(def: PreferenceDef<T>, bytes: ByteArray?): T? {
        return if (bytes == null) {
            def.defaultValue
        } else {
            return byteArrayToValue(def.type, bytes)
        }
    }

    private fun <T> byteArrayToValue(type: PreferenceType<T>, bytes: ByteArray): T {
        return when (type) {
            is PreferenceType.STRING -> String(bytes) as T
            is PreferenceType.BOOLEAN -> (bytes[0] == 1.toByte()) as T
            is PreferenceType.FLOAT -> byteArrayToValue(PreferenceType.DOUBLE, bytes).toFloat() as T
            is PreferenceType.DOUBLE -> longBitsToDouble(byteArrayToValue(PreferenceType.LONG, bytes)) as T
            is PreferenceType.INT -> byteArrayToValue(PreferenceType.LONG, bytes).toInt() as T
            is PreferenceType.LONG -> BigInteger(bytes).toLong() as T
        }
    }
}
