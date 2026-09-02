package com.shyxseek.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecretStore(private val context:Context){
 private val prefs=context.getSharedPreferences("secrets",Context.MODE_PRIVATE)
 private val alias="shyxseek_master_key"
 private fun key():SecretKey{
   val ks=KeyStore.getInstance("AndroidKeyStore").apply{load(null)}
   (ks.getKey(alias,null) as? SecretKey)?.let{return it}
   val kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore")
   kg.init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
   return kg.generateKey()
 }
 fun put(name:String,value:String){
   val c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE,key()); val data=c.doFinal(value.toByteArray())
   prefs.edit().putString(name,Base64.encodeToString(c.iv+data,Base64.NO_WRAP)).apply()
 }
 fun get(name:String):String?=runCatching{
   val raw=Base64.decode(prefs.getString(name,null)?:return null,Base64.NO_WRAP); val iv=raw.copyOfRange(0,12); val data=raw.copyOfRange(12,raw.size)
   val c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,iv)); String(c.doFinal(data))
 }.getOrNull()
}
