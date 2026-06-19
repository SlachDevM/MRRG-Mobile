package com.slachdevm.mrrgmobile.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
import com.slachdevm.mrrgmobile.data.session.SessionManager
import com.slachdevm.mrrgmobile.data.api.RetrofitClient
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import com.slachdevm.mrrgmobile.data.repository.JobRepository
import com.slachdevm.mrrgmobile.data.repository.NotificationRepository
import com.slachdevm.mrrgmobile.data.repository.ProfileRepository
import com.slachdevm.mrrgmobile.data.local.MRRGDatabase
import com.slachdevm.mrrgmobile.data.sync.SyncRepository
import com.slachdevm.mrrgmobile.ui.auth.LoginScreen
import com.slachdevm.mrrgmobile.ui.auth.LoginViewModel
import com.slachdevm.mrrgmobile.ui.jobs.JobDetailScreen
import com.slachdevm.mrrgmobile.ui.jobs.JobDetailViewModel
import com.slachdevm.mrrgmobile.ui.jobs.JobListScreen
import com.slachdevm.mrrgmobile.ui.jobs.JobListViewModel
import com.slachdevm.mrrgmobile.ui.notifications.NotificationScreen
import com.slachdevm.mrrgmobile.ui.notifications.NotificationViewModel
import com.slachdevm.mrrgmobile.ui.profile.ProfileScreen
import com.slachdevm.mrrgmobile.ui.profile.ProfileViewModel
import com.slachdevm.mrrgmobile.ui.settings.SettingsScreen
import com.slachdevm.mrrgmobile.ui.theme.ThemeMode

object Routes {
    const val LOGIN = "login"
    const val JOBS = "jobs"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val JOB_ID_ARG = "jobId"
    const val JOB_DETAIL = "job_detail/{$JOB_ID_ARG}"

    fun jobDetail(jobId: Long) = "job_detail/$jobId"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    initialJobId: Long? = null,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    LaunchedEffect(initialJobId) {
        initialJobId?.let { jobId ->
            navController.navigate(Routes.jobDetail(jobId))
        }
    }
    val context = LocalContext.current

    val database = remember {
        MRRGDatabase.getInstance(context)
    }

    val userApi = remember {
        RetrofitClient.createUserApi()
    }
    val sessionManager = remember { SessionManager(context) }
    val authRepository = remember {
        AuthRepository(
            RetrofitClient.authApi,
            userApi,
            sessionManager
        )
    }
    val jobRepository = remember {
        JobRepository(
            jobApi = RetrofitClient.jobApi,
            jobDao = database.jobDao(),
            pendingSyncDao = database.pendingSyncDao()
        )
    }
    val notificationRepository = remember {
        NotificationRepository(RetrofitClient.notificationApi)
    }
    val syncRepository = remember {
        SyncRepository(
            jobApi = RetrofitClient.jobApi,
            jobDao = database.jobDao(),
            pendingSyncDao = database.pendingSyncDao()
        )
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
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
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
                        JobListViewModel(jobRepository, authRepository, syncRepository)
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
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
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
            arguments = listOf(navArgument(Routes.JOB_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong(Routes.JOB_ID_ARG) ?: return@composable
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

        composable(Routes.PROFILE) {
            val profileRepository = remember {
                ProfileRepository(
                    userApi = RetrofitClient.userApi,
                    userProfileDao = database.userProfileDao()
                )
            }

            val profileViewModel: ProfileViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ProfileViewModel(profileRepository)
                    }
                }
            )

            ProfileScreen(
                viewModel = profileViewModel
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.JOBS) { inclusive = true }
                    }
                }
            )
        }
    }
}
