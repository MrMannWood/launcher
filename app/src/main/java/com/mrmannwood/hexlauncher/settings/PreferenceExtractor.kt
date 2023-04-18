package com.mrmannwood.hexlauncher.settings

interface PreferenceExtractor<T> {

    fun getValue(pref: Preference<*>): T

    object StringExtractor : PreferenceExtractor<String> {
        override fun getValue(pref: Preference<*>): String {
            if (pref !is Preference.StringPreference) throwClassCastException(pref, "String")
            return pref.value
        }
    }

    object BooleanExtractor : PreferenceExtractor<Boolean> {
        override fun getValue(pref: Preference<*>): Boolean {
            if (pref !is Preference.BooleanPreference) throwClassCastException(pref, "Boolean")
            return pref.value
        }
    }

    object IntExtractor : PreferenceExtractor<Int> {
        override fun getValue(pref: Preference<*>): Int {
            if (pref !is Preference.IntPreference) throwClassCastException(pref, "Int")
            return pref.value
        }
    }

    object FloatExtractor : PreferenceExtractor<Float> {
        override fun getValue(pref: Preference<*>): Float {
            if (pref !is Preference.FloatPreference) throwClassCastException(pref, "Float")
            return pref.value
        }
    }

    object LongExtractor : PreferenceExtractor<Long> {
        override fun getValue(pref: Preference<*>): Long {
            if (pref !is Preference.LongPreference) throwClassCastException(pref, "Long")
            return pref.value
        }
    }
    
    fun throwClassCastException(pref: Preference<*>, expected: String): Nothing {
        throw ClassCastException("${pref.key} does not represent a $expected: ${pref.value}")
    }
}
