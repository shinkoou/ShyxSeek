package com.shyxseek.app.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel class MemoryViewModel @Inject constructor(private val repo:MemoryRepository):ViewModel(){val items=repo.observeAll().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000), emptyList());fun remember(content:String){viewModelScope.launch{repo.remember(content)}}}
