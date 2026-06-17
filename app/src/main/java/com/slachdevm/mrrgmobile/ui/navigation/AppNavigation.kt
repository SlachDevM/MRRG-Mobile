package com.slachdevm.mrrgmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.slachdevm.mrrgmobile.data.repository.NotificationRepository
import com.slachdevm.mrrgmobile.ui.auth.LoginScreen
import com.slachdevm.mrrgmobile.ui.auth.LoginViewModel
import com.slachdevm.mrrgmobile.ui.jobs.JobDetailScreen
import com.slachdevm.mrrgmobile.ui.jobs.JobDetailViewModel
import com.slachdevm.mrrgmobile.ui.jobs.JobListScreen
import com.slachdevm.mrrgmobile.ui.jobs.JobListViewModel
import com.slachdevm.mrrgmobile.ui.notifications.NotificationScreen
import com.slachdevm.mrrgmobile.ui.notifications.NotificationViewModel

object Routes {
    const val LOGIN = "login"
    const val JOBS = "jobs"

    const val NOTIFICATIONS = "notifications"
    const val JOB_ID_ARG = "jobId"
    const val JOB_DETAIL = "job_detail/{$JOB_ID_ARG}"

    fun jobDetail(jobId: Long) = "job_detail/$jobId"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    val authRepository = remember { AuthRepository(RetrofitClient.authApi, sessionManager) }
    val jobRepository = remember { JobRepository(RetrofitClient.jobApi) }
    val notificationRepository = remember {
        NotificationRepository(RetrofitClient.notificationApi)
    }
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                NotificationViewModel(notificationRepository)
            }
        }
    )

    val startDestination = if (authRepository.isLoggedIn()) Routes.JOBS else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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

            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        jobListViewModel.loadJobs()
                        notificationViewModel.loadUnreadCount()
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            JobListScreen(
                viewModel = jobListViewModel,
                notificationUnreadCount = notificationViewModel.uiState.unreadCount,
                onNotificationsClick = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
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
            arguments = listOf(navArgument(Routes.JOB_ID_ARG ) { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong(Routes.JOB_ID_ARG ) ?: return@composable
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

        composable(Routes.NOTIFICATIONS) {
            NotificationScreen(
                viewModel = notificationViewModel,
                onBack = {
                    notificationViewModel.loadUnreadCount()
                    navController.popBackStack()
                },
                onOpenJob = { jobId ->
                    notificationViewModel.loadUnreadCount()
                    navController.popBackStack()
                    navController.navigate(Routes.jobDetail(jobId))
                }
            )
        }
    }
}
