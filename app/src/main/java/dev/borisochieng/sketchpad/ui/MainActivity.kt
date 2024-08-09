package dev.borisochieng.sketchpad.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import dev.borisochieng.sketchpad.ui.components.NavBar
import dev.borisochieng.sketchpad.ui.navigation.AppRoute
import dev.borisochieng.sketchpad.ui.navigation.NavActions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.Root
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.activityChooser
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.checkAndAskPermission
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.saveImage
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   enableEdgeToEdge()
        setContent {
            Root(window = window) {
                val navController = rememberNavController()
                val navActions = NavActions(navController)
                AppTheme {
                    Scaffold(
                        bottomBar = { NavBar(navController) }
                    ) { _ ->
                        AppRoute(
                            navActions = navActions,
                            navController = navController,
                            saveImage = {
                                checkAndAskPermission {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val uri = saveImage(it)
                                        withContext(Dispatchers.Main) {
                                            startActivity(activityChooser(uri))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}