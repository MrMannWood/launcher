package com.mrmannwood.hexlauncher.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.nio.ByteBuffer

sealed class Preference<T : Any>(val key: String, val value: T) {
    class StringPreference(key: String, value: String) : Preference<String>(key, value)
    class IntPreference(key: String, value: Int) : Preference<Int>(key, value)
    class LongPreference(key: String, value: Long) : Preference<Long>(key, value)
    class FloatPreference(key: String, value: Float) : Preference<Float>(key, value)
    class DoublePreference(key: String, value: Double) : Preference<Double>(key, value)
    class BooleanPreference(key: String, value: Boolean) : Preference<Boolean>(key, value)
}

@Entity(tableName = "preferences")
class PreferenceEntity(
    @PrimaryKey @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "value", typeAffinity = ColumnInfo.BLOB) val value: ByteArray
) {
    enum class Type {
        STRING,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN
    }

    fun toPreference(): Preference<*> {
        return when (Type.valueOf(type)) {
            Type.STRING -> {
                Preference.StringPreference(
                    key = key,
                    value = value.toString(charset = Charsets.UTF_8)
                )
            }
            Type.INT -> {
                Preference.IntPreference(
                    key = key,
                    value = ByteBuffer.wrap(value).int
                )
            }
            Type.LONG -> {
                Preference.LongPreference(
                    key = key,
                    value = ByteBuffer.wrap(value).long
                )
            }
            Type.FLOAT -> {
                Preference.FloatPreference(
                    key = key,
                    value = ByteBuffer.wrap(value).float
                )
            }
            Type.DOUBLE -> {
                Preference.DoublePreference(
                    key = key,
                    value = ByteBuffer.wrap(value).double
                )
            }
            Type.BOOLEAN -> {
                Preference.BooleanPreference(
                    key = key,
                    value = ByteBuffer.wrap(value).int != 0
                )
            }
        }
    }

    companion object {
        fun fromPreference(preference: Preference<*>): PreferenceEntity {
            return when (preference) {
                is Preference.StringPreference -> {
                    PreferenceEntity(
                        key = preference.key,
                        type = Type.STRING.name,
                        value = preference.value.toByteArray(charset = Charsets.UTF_8)
                    )
                }
                is Preference.IntPreference -> {
                    PreferenceEntity(
                        key = preference.key,
                        type = Type.INT.name,
                        value = preference.value.toByteArray()
                    )
                }
                is Preference.LongPreference -> {
                    PreferenceEntity(
                        key = preference.key,
                        type = Type.LONG.name,
                        value = preference.value.toByteArray()
                    )
                }
                is Preference.FloatPreference -> {
                    PreferenceEntity(
                        key = preference.key,
                        type = Type.FLOAT.name,
                        value = preference.value.toByteArray()
                    )
                }
                is Preference.DoublePreference -> {
                    PreferenceEntity(
                        key = preference.key,
                        type = Type.DOUBLE.name,
                        value = preference.value.toByteArray()
                    )
                }
                is Preference.BooleanPreference -> {
                    PreferenceEntity(
                        key = preference.key,
                        type = Type.BOOLEAN.name,
                        value = preference.value.toByteArray()
                    )
                }
            }
        }

        private fun Int.toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(Int.SIZE_BYTES)
            buffer.putInt(this)
            return buffer.array()
        }

        private fun Long.toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
            buffer.putLong(this)
            return buffer.array()
        }

        private fun Float.toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(Float.SIZE_BYTES)
            buffer.putFloat(this)
            return buffer.array()
        }

        private fun Double.toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(Double.SIZE_BYTES)
            buffer.putDouble(this)
            return buffer.array()
        }

        private fun Boolean.toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(Int.SIZE_BYTES)
            buffer.putInt(if (this) 1 else 0)
            return buffer.array()
        }
    }
}
