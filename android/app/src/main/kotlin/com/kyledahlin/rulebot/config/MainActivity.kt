package com.kyledahlin.rulebot.config

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var _facade: ConfigFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Button({
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    Log.d("DEBUG", "${_facade.getRules()}")
                                } catch (e: Exception) {
                                    Log.d("DEBUG", "got error $e")
                                }
                            }
                        }) {
                            Text("Get rules debug", color = Color.Black)
                        }

                        Button({
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    Log.d("DEBUG", "${_facade.getGuilds()}")
                                } catch (e: Exception) {
                                    Log.d("DEBUG", "got error $e")
                                }
                            }
                        }) {
                            Text("Get guilds debug", color = Color.Black)
                        }

                        Button({
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                Log.d("DEBUG", "${_facade.getGuildInfo("559637112232738828")}")
                                } catch (e: Exception) {
                                    Log.d("DEBUG", "got error $e")
                                }
                            }
                        }) {
                            Text("Get rules debug", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}