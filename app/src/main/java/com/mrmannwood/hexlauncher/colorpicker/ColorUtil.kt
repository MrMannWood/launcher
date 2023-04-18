package com.mrmannwood.hexlauncher.colorpicker

import androidx.annotation.StringRes
import com.mrmannwood.launcher.R

// https://gist.github.com/XiaoxiaoLi/8031146
object ColorUtil {

    private val colorNames = listOf(
        ColorName(R.string.alice_blue, 0xF0, 0xF8, 0xFF),
        ColorName(R.string.antique_white, 0xFA, 0xEB, 0xD7),
        ColorName(R.string.aqua, 0x00, 0xFF, 0xFF),
        ColorName(R.string.aquamarine, 0x7F, 0xFF, 0xD4),
        ColorName(R.string.azure, 0xF0, 0xFF, 0xFF),
        ColorName(R.string.beige, 0xF5, 0xF5, 0xDC),
        ColorName(R.string.bisque, 0xFF, 0xE4, 0xC4),
        ColorName(R.string.black, 0x00, 0x00, 0x00),
        ColorName(R.string.blanched_almond, 0xFF, 0xEB, 0xCD),
        ColorName(R.string.blue, 0x00, 0x00, 0xFF),
        ColorName(R.string.blue_violet, 0x8A, 0x2B, 0xE2),
        ColorName(R.string.brown, 0xA5, 0x2A, 0x2A),
        ColorName(R.string.burly_wood, 0xDE, 0xB8, 0x87),
        ColorName(R.string.cadet_blue, 0x5F, 0x9E, 0xA0),
        ColorName(R.string.chartreuse, 0x7F, 0xFF, 0x00),
        ColorName(R.string.chocolate, 0xD2, 0x69, 0x1E),
        ColorName(R.string.coral, 0xFF, 0x7F, 0x50),
        ColorName(R.string.cornflower_blue, 0x64, 0x95, 0xED),
        ColorName(R.string.cornsilk, 0xFF, 0xF8, 0xDC),
        ColorName(R.string.crimson, 0xDC, 0x14, 0x3C),
        ColorName(R.string.cyan, 0x00, 0xFF, 0xFF),
        ColorName(R.string.dark_blue, 0x00, 0x00, 0x8B),
        ColorName(R.string.dark_cyan, 0x00, 0x8B, 0x8B),
        ColorName(R.string.dark_goldenrod, 0xB8, 0x86, 0x0B),
        ColorName(R.string.dark_gray, 0xA9, 0xA9, 0xA9),
        ColorName(R.string.dark_green, 0x00, 0x64, 0x00),
        ColorName(R.string.dark_khaki, 0xBD, 0xB7, 0x6B),
        ColorName(R.string.dark_magenta, 0x8B, 0x00, 0x8B),
        ColorName(R.string.dark_olive_green, 0x55, 0x6B, 0x2F),
        ColorName(R.string.dark_orange, 0xFF, 0x8C, 0x00),
        ColorName(R.string.dark_orchid, 0x99, 0x32, 0xCC),
        ColorName(R.string.dark_red, 0x8B, 0x00, 0x00),
        ColorName(R.string.dark_salmon, 0xE9, 0x96, 0x7A),
        ColorName(R.string.dark_sea_green, 0x8F, 0xBC, 0x8F),
        ColorName(R.string.dark_slate_blue, 0x48, 0x3D, 0x8B),
        ColorName(R.string.dark_slate_gray, 0x2F, 0x4F, 0x4F),
        ColorName(R.string.dark_turquoise, 0x00, 0xCE, 0xD1),
        ColorName(R.string.dark_violet, 0x94, 0x00, 0xD3),
        ColorName(R.string.deep_pink, 0xFF, 0x14, 0x93),
        ColorName(R.string.deep_sky_blue, 0x00, 0xBF, 0xFF),
        ColorName(R.string.dim_gray, 0x69, 0x69, 0x69),
        ColorName(R.string.dodger_blue, 0x1E, 0x90, 0xFF),
        ColorName(R.string.fire_brick, 0xB2, 0x22, 0x22),
        ColorName(R.string.floral_white, 0xFF, 0xFA, 0xF0),
        ColorName(R.string.forest_green, 0x22, 0x8B, 0x22),
        ColorName(R.string.fuchsia, 0xFF, 0x00, 0xFF),
        ColorName(R.string.gainsboro, 0xDC, 0xDC, 0xDC),
        ColorName(R.string.ghost_white, 0xF8, 0xF8, 0xFF),
        ColorName(R.string.gold, 0xFF, 0xD7, 0x00),
        ColorName(R.string.goldenrod, 0xDA, 0xA5, 0x20),
        ColorName(R.string.gray, 0x80, 0x80, 0x80),
        ColorName(R.string.green, 0x00, 0x80, 0x00),
        ColorName(R.string.green_yellow, 0xAD, 0xFF, 0x2F),
        ColorName(R.string.honey_dew, 0xF0, 0xFF, 0xF0),
        ColorName(R.string.hot_pink, 0xFF, 0x69, 0xB4),
        ColorName(R.string.indian_red, 0xCD, 0x5C, 0x5C),
        ColorName(R.string.indigo, 0x4B, 0x00, 0x82),
        ColorName(R.string.ivory, 0xFF, 0xFF, 0xF0),
        ColorName(R.string.khaki, 0xF0, 0xE6, 0x8C),
        ColorName(R.string.lavender, 0xE6, 0xE6, 0xFA),
        ColorName(R.string.lavender_blush, 0xFF, 0xF0, 0xF5),
        ColorName(R.string.lawn_green, 0x7C, 0xFC, 0x00),
        ColorName(R.string.lemon_chiffon, 0xFF, 0xFA, 0xCD),
        ColorName(R.string.light_blue, 0xAD, 0xD8, 0xE6),
        ColorName(R.string.light_coral, 0xF0, 0x80, 0x80),
        ColorName(R.string.light_cyan, 0xE0, 0xFF, 0xFF),
        ColorName(R.string.light_goldenrod_yellow, 0xFA, 0xFA, 0xD2),
        ColorName(R.string.light_gray, 0xD3, 0xD3, 0xD3),
        ColorName(R.string.light_green, 0x90, 0xEE, 0x90),
        ColorName(R.string.light_pink, 0xFF, 0xB6, 0xC1),
        ColorName(R.string.light_salmon, 0xFF, 0xA0, 0x7A),
        ColorName(R.string.light_sea_green, 0x20, 0xB2, 0xAA),
        ColorName(R.string.light_sky_blue, 0x87, 0xCE, 0xFA),
        ColorName(R.string.light_slate_gray, 0x77, 0x88, 0x99),
        ColorName(R.string.light_steel_blue, 0xB0, 0xC4, 0xDE),
        ColorName(R.string.light_yellow, 0xFF, 0xFF, 0xE0),
        ColorName(R.string.lime, 0x00, 0xFF, 0x00),
        ColorName(R.string.lime_green, 0x32, 0xCD, 0x32),
        ColorName(R.string.linen, 0xFA, 0xF0, 0xE6),
        ColorName(R.string.magenta, 0xFF, 0x00, 0xFF),
        ColorName(R.string.maroon, 0x80, 0x00, 0x00),
        ColorName(R.string.medium_aqua_marine, 0x66, 0xCD, 0xAA),
        ColorName(R.string.medium_blue, 0x00, 0x00, 0xCD),
        ColorName(R.string.medium_orchid, 0xBA, 0x55, 0xD3),
        ColorName(R.string.medium_purple, 0x93, 0x70, 0xDB),
        ColorName(R.string.medium_sea_green, 0x3C, 0xB3, 0x71),
        ColorName(R.string.medium_slate_blue, 0x7B, 0x68, 0xEE),
        ColorName(R.string.medium_spring_green, 0x00, 0xFA, 0x9A),
        ColorName(R.string.medium_turquoise, 0x48, 0xD1, 0xCC),
        ColorName(R.string.medium_violet_red, 0xC7, 0x15, 0x85),
        ColorName(R.string.midnight_blue, 0x19, 0x19, 0x70),
        ColorName(R.string.mint_cream, 0xF5, 0xFF, 0xFA),
        ColorName(R.string.misty_rose, 0xFF, 0xE4, 0xE1),
        ColorName(R.string.moccasin, 0xFF, 0xE4, 0xB5),
        ColorName(R.string.navajo_white, 0xFF, 0xDE, 0xAD),
        ColorName(R.string.navy, 0x00, 0x00, 0x80),
        ColorName(R.string.old_lace, 0xFD, 0xF5, 0xE6),
        ColorName(R.string.olive, 0x80, 0x80, 0x00),
        ColorName(R.string.olive_drab, 0x6B, 0x8E, 0x23),
        ColorName(R.string.orange, 0xFF, 0xA5, 0x00),
        ColorName(R.string.orange_red, 0xFF, 0x45, 0x00),
        ColorName(R.string.orchid, 0xDA, 0x70, 0xD6),
        ColorName(R.string.pale_goldenrod, 0xEE, 0xE8, 0xAA),
        ColorName(R.string.pale_green, 0x98, 0xFB, 0x98),
        ColorName(R.string.pale_turquoise, 0xAF, 0xEE, 0xEE),
        ColorName(R.string.pale_violet_red, 0xDB, 0x70, 0x93),
        ColorName(R.string.papaya_whip, 0xFF, 0xEF, 0xD5),
        ColorName(R.string.peach_puff, 0xFF, 0xDA, 0xB9),
        ColorName(R.string.peru, 0xCD, 0x85, 0x3F),
        ColorName(R.string.pink, 0xFF, 0xC0, 0xCB),
        ColorName(R.string.plum, 0xDD, 0xA0, 0xDD),
        ColorName(R.string.powder_blue, 0xB0, 0xE0, 0xE6),
        ColorName(R.string.purple, 0x80, 0x00, 0x80),
        ColorName(R.string.red, 0xFF, 0x00, 0x00),
        ColorName(R.string.rosy_brown, 0xBC, 0x8F, 0x8F),
        ColorName(R.string.royal_blue, 0x41, 0x69, 0xE1),
        ColorName(R.string.saddle_brown, 0x8B, 0x45, 0x13),
        ColorName(R.string.salmon, 0xFA, 0x80, 0x72),
        ColorName(R.string.sandy_brown, 0xF4, 0xA4, 0x60),
        ColorName(R.string.sea_green, 0x2E, 0x8B, 0x57),
        ColorName(R.string.sea_shell, 0xFF, 0xF5, 0xEE),
        ColorName(R.string.sienna, 0xA0, 0x52, 0x2D),
        ColorName(R.string.silver, 0xC0, 0xC0, 0xC0),
        ColorName(R.string.sky_blue, 0x87, 0xCE, 0xEB),
        ColorName(R.string.slate_blue, 0x6A, 0x5A, 0xCD),
        ColorName(R.string.slate_gray, 0x70, 0x80, 0x90),
        ColorName(R.string.snow, 0xFF, 0xFA, 0xFA),
        ColorName(R.string.spring_green, 0x00, 0xFF, 0x7F),
        ColorName(R.string.steel_blue, 0x46, 0x82, 0xB4),
        ColorName(R.string.tan, 0xD2, 0xB4, 0x8C),
        ColorName(R.string.teal, 0x00, 0x80, 0x80),
        ColorName(R.string.thistle, 0xD8, 0xBF, 0xD8),
        ColorName(R.string.tomato, 0xFF, 0x63, 0x47),
        ColorName(R.string.turquoise, 0x40, 0xE0, 0xD0),
        ColorName(R.string.violet, 0xEE, 0x82, 0xEE),
        ColorName(R.string.wheat, 0xF5, 0xDE, 0xB3),
        ColorName(R.string.white, 0xFF, 0xFF, 0xFF),
        ColorName(R.string.white_smoke, 0xF5, 0xF5, 0xF5),
        ColorName(R.string.yellow, 0xFF, 0xFF, 0x00),
        ColorName(R.string.yellow_green, 0x9A, 0xCD, 0x32)
    )

    private data class ColorName(@StringRes val name: Int, val r: Int, val g: Int, val b: Int) {
        fun computeMSE(pixR: Int, pixG: Int, pixB: Int): Int {
            return (
                    (
                            (pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (
                                    (pixB - b) *
                                            (pixB - b)
                                    )
                            ) / 3
                    )
        }
    }

    @StringRes
    fun getColorNameFromHex(hexColor: Int): Int {
        val r = hexColor and 0xFF0000 shr 16
        val g = hexColor and 0xFF00 shr 8
        val b = hexColor and 0xFF
        return getColorNameFromRgb(r, g, b)
    }

    @StringRes
    private fun getColorNameFromRgb(r: Int, g: Int, b: Int): Int {
        var closestMatch: ColorName? = null
        var minMSE = Int.MAX_VALUE
        var mse: Int
        for (c in colorNames) {
            mse = c.computeMSE(r, g, b)
            if (mse < minMSE) {
                minMSE = mse
                closestMatch = c
            }
        }
        return closestMatch?.name ?: R.string.no_matching_color
    }
}
