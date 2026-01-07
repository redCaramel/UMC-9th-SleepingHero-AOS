package com.umc_9th.sleepinghero

import android.text.method.PasswordTransformationMethod
import android.view.View

class AsteriskCharSequence(private val source : CharSequence) : CharSequence {
    override val length: Int
        get() = source.length

    override fun get(index: Int): Char {
        return '*'
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return AsteriskCharSequence(source.subSequence(startIndex, endIndex))
    }
}

class AsteriskPasswordTransformation : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        return AsteriskCharSequence(source)
    }
}