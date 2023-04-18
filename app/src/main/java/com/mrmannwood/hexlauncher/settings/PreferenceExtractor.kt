package com.mrmannwood.hexlauncher.settings

interface PreferenceExtractor<T> {

    fun getValue(pref: Preference<*>): T

    object StringExtractor : PreferenceExtractor<String> {
        override fun getValue(pref: Preference<*>): String {
            if (pref !is Preference.StringPreference) throw ClassCastException("${pref.key} does not represent a String")
            return pref.value
        }
    }

    object BooleanExtractor : PreferenceExtractor<Boolean> {
        override fun getValue(pref: Preference<*>): Boolean {
            if (pref !is Preference.BooleanPreference) throw ClassCastException("${pref.key} does not represent a Boolean")
            return pref.value
        }
    }

    object IntExtractor : PreferenceExtractor<Int> {
        override fun getValue(pref: Preference<*>): Int {
            if (pref !is Preference.IntPreference) throw ClassCastException("${pref.key} does not represent a Int")
            return pref.value
        }
    }

    object FloatExtractor : PreferenceExtractor<Float> {
        override fun getValue(pref: Preference<*>): Float {
            if (pref !is Preference.FloatPreference) throw ClassCastException("${pref.key} does not represent a Float")
            return pref.value
        }
    }

    object LongExtractor : PreferenceExtractor<Long> {
        override fun getValue(pref: Preference<*>): Long {
            if (pref !is Preference.LongPreference) throw ClassCastException("${pref.key} does not represent a Long")
            return pref.value
        }
    }
}
