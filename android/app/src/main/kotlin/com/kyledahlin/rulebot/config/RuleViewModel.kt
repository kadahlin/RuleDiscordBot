package com.kyledahlin.rulebot.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RuleViewModel @Inject constructor(
    private val _configFacade: ConfigFacade
) : ViewModel() {

    private val _state = MutableLiveData<RuleState>()
    val state: LiveData<RuleState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(RuleState.Loading)
            val newState = _configFacade.getRules().fold({ failureReason ->
                RuleState.Failed(reason = failureReason)
            }, { response ->
                RuleState.Loaded(response.rules)
            })
            _state.postValue(newState)
        }
    }
}

sealed class RuleState {
    object Loading : RuleState()
    data class Failed(val reason: String) : RuleState()
    data class Loaded(val rules: List<String>) : RuleState()
}