package com.shyxseek.app.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.data.repository.KnowledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel class KnowledgeViewModel @Inject constructor(private val repo:KnowledgeRepository):ViewModel(){val items=repo.observeAll().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000), emptyList());fun teach(topic:String,content:String){viewModelScope.launch{repo.teach(topic.ifBlank{"Geral"},content)}}}
