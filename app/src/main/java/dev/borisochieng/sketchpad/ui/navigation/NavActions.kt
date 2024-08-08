package dev.borisochieng.sketchpad.ui.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {

    fun navigate(screen: Screens) {
        when (screen) {
            Screens.HomeScreenScreen -> navigateToHomeScreen()
            is Screens.SketchPad -> navigateToSketchPad(screen.sketchId)
            Screens.SettingsScreen -> navigateToSettingsScreen()
            Screens.ProfileScreen -> navigateToProfileScreen()
            Screens.Back -> navController.navigateUp()
            Screens.OnBoardingScreen -> Unit
            Screens.SignUpScreen -> navigateToSignUpScreen()
            Screens.LoginScreen -> navigateToLoginScreen()
        }
    }

    private fun navigateToHomeScreen() {
        navController.navigate(AppRoute.HomeScreen.route)
    }

    private fun navigateToSketchPad(sketchId: Int?) {
        navController.navigate(
            AppRoute.SketchPad.routeWithId(sketchId)
        )
    }

    private fun navigateToSettingsScreen() {
        navController.navigate(AppRoute.SettingsScreen.route)
    }

    private fun navigateToProfileScreen() {
        navController.navigate(AppRoute.ProfileScreen.route)
    }

    private fun navigateToSignUpScreen() {
        navController.navigate(AppRoute.SignUpScreen.route)
    }

    private fun navigateToLoginScreen() {
        navController.navigate(AppRoute.LoginScreen.route)
    }

}

@SuppressLint("DefaultLocale")
sealed class AppRoute(val route: String) {
    data object HomeScreen : AppRoute("home_screen")
    data object SketchPad : AppRoute("sketchpad/{sketchId}") {
        fun routeWithId(sketchId: Int?) = String.format("sketchpad/%d", sketchId)
    }

    data object SettingsScreen : AppRoute("settings_screen")
    data object ProfileScreen : AppRoute("profile_screen")
    data object OnBoardingScreen : AppRoute("onboarding_screen")
    data object SignUpScreen : AppRoute("welcome_screen")
    data object LoginScreen : AppRoute("login_screen")
}

sealed class Screens {
    data object HomeScreenScreen : Screens()
    data class SketchPad(val sketchId: Int?) : Screens()
    data object SettingsScreen : Screens()
    data object ProfileScreen : Screens()
    data object Back : Screens()
    data object OnBoardingScreen : Screens()
    data object SignUpScreen : Screens()
    data object LoginScreen : Screens()
}