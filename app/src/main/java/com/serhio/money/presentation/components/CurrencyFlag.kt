package com.serhio.money.presentation.components

fun currencyFlag(code: String): String = when (code) {
    "EUR" -> "\uD83C\uDDEA\uD83C\uDDFA"
    "BTC" -> "\u20BF"
    "ETH" -> "\uD83D\uDD35"
    "XAU" -> "\uD83C\uDFC5"
    "XAG" -> "\uD83E\uDE99"
    "XPT" -> "\uD83D\uDC8E"
    "XPD" -> "\uD83D\uDD0C"
    "XDR" -> "\uD83C\uDF0D"
    else -> {
        if (code.length == 3) {
            val a = code[0]
            val b = code[1]
            if (a in 'A'..'Z' && b in 'A'..'Z') {
                val regionalA = Character.toChars(0x1F1E6 + (a - 'A'))
                val regionalB = Character.toChars(0x1F1E6 + (b - 'A'))
                String(regionalA) + String(regionalB)
            } else ""
        } else ""
    }
}

fun currencyName(code: String): String = code
