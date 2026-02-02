package com.umc_9th.sleepinghero.api

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {

    private const val PREF_NAME = "auth_token"
    private const val ACCESS_TOKEN = "access_token"
    private const val MEMBER_ID = "member_id"
    private const val NICKNAME = "nickname"
    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    // Access Token 저장
    fun setAccessToken(context: Context, token: String) {
        getPrefs(context).edit().putString(ACCESS_TOKEN, token).apply()
    }
    // Member Id 저장
    fun setMemberId(context: Context, id: String) {
        getPrefs(context).edit().putString(MEMBER_ID, id).apply()
    }
    // Nickname 저장
    fun setNickName(context: Context, name: String) {
        getPrefs(context).edit().putString(NICKNAME, name).apply()
    }

    // Access Token 불러오기
    fun getAccessToken(context: Context): String? =
        getPrefs(context).getString(ACCESS_TOKEN, null)
    // Member Id 불러오기
    fun getMemberId(context: Context): String? =
        getPrefs(context).getString(MEMBER_ID, null)
    // Nickname 불러오기
    fun getNickname(context: Context): String? =
        getPrefs(context).getString(NICKNAME, null)

    // 로그인 여부 확인
    fun isLoggedin(context: Context) : Boolean {
        return getAccessToken(context) != null
    }

    // 모든 토큰 삭제(로그아웃 시)
    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}