package com.shyxseek.app.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyxseek.app.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel class ProjectsViewModel @Inject constructor(private val repo:ProjectRepository):ViewModel(){val items=repo.observeAll().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000), emptyList());init{viewModelScope.launch{repo.ensureDefault()}}}
