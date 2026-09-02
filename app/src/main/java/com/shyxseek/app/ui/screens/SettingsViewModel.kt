package com.shyxseek.app.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel class SettingsViewModel @Inject constructor(private val settings:AppSettings):ViewModel(){val state=settings.flow.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),ProviderSettings());fun save(provider:String,base:String,model:String,key:String){viewModelScope.launch{settings.save(ProviderSettings(provider,base,model,state.value.temperature,key.isNotBlank()||state.value.hasApiKey),key)}}}
