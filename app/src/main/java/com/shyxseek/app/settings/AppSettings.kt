package com.shyxseek.app.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.shyxseek.app.security.SecretStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("shyxseek_settings")
data class ProviderSettings(val provider:String="fake",val baseUrl:String="https://api.openai.com/",val model:String="",val temperature:Double=0.4,val hasApiKey:Boolean=false)
class AppSettings(private val context:Context,private val secrets:SecretStore){
 private val provider=stringPreferencesKey("provider"); private val base=stringPreferencesKey("base_url"); private val model=stringPreferencesKey("model"); private val temp=doublePreferencesKey("temperature")
 val flow:Flow<ProviderSettings> = context.dataStore.data.map{ProviderSettings(it[provider]?:"fake",it[base]?:"https://api.openai.com/",it[model]?:"",it[temp]?:0.4,!secrets.get("api_key").isNullOrBlank())}
 suspend fun save(v:ProviderSettings,apiKey:String?){ context.dataStore.edit{it[provider]=v.provider;it[base]=v.baseUrl;it[model]=v.model;it[temp]=v.temperature}; if(!apiKey.isNullOrBlank())secrets.put("api_key",apiKey)}
 fun apiKey()=secrets.get("api_key")
}
