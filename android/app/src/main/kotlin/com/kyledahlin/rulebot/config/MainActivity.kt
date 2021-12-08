package com.kyledahlin.rulebot.config

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val rulesModel by viewModels<RuleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val state by rulesModel.state.observeAsState(initial = RuleState.Loading)
                    when (val current = state) {
                        is RuleState.Failed -> {
                            Text(current.reason)
                        }
                        is RuleState.Loaded -> {
                            LazyColumn {
                                items(current.rules) { rule ->
                                    Text(rule)
                                }
                            }
                        }
                        RuleState.Loading -> Text("Loading")
                    }
                }
            }
        }
    }
}