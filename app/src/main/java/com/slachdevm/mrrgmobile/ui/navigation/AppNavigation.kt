package com.slachdevm.mrrgmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slachdevm.mrrgmobile.data.SessionManager
import com.slachdevm.mrrgmobile.data.api.RetrofitClient
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import com.slachdevm.mrrgmobile.data.repository.JobRepository
import com.slachdevm.mrrgmobile.ui.auth.LoginScreen
import com.slachdevm.mrrgmobile.ui.auth.LoginViewModel
import com.slachdevm.mrrgmobile.ui.jobs.JobDetailScreen
import com.slachdevm.mrrgmobile.ui.jobs.JobDetailViewModel
import com.slachdevm.mrrgmobile.ui.jobs.JobListScreen
import com.slachdevm.mrrgmobile.ui.jobs.JobListViewModel

object Routes {
    const val LOGIN = "login"
    const val JOBS = "jobs"
    const val JOB_DETAIL = "job_detail/{jobId}"
    
    fun jobDetail(jobId: Long) = "job_detail/$jobId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val sessionManager = remember { SessionManager(context) }
    val authRepository = remember { AuthRepository(RetrofitClient.authApi, sessionManager) }
    val jobRepository = remember { JobRepository(RetrofitClient.jobApi) }

    val startDestination = if (authRepository.isLoggedIn()) Routes.JOBS else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        LoginViewModel(authRepository)
                    }
                }
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.JOBS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.JOBS) {
            val jobListViewModel: JobListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        JobListViewModel(jobRepository, authRepository)
                    }
                }
            )
            JobListScreen(
                viewModel = jobListViewModel,
                onJobClick = { jobId ->
                    navController.navigate(Routes.jobDetail(jobId))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.JOBS) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong("jobId") ?: return@composable
            val jobDetailViewModel: JobDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        JobDetailViewModel(jobRepository, jobId)
                    }
                }
            )
            JobDetailScreen(
                viewModel = jobDetailViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
