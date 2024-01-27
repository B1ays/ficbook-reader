package ru.blays.ficbookReader.shared.ui.landingScreenComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbookReader.shared.platformUtils.openInBrowser
import ru.blays.ficbookReader.shared.platformUtils.runOnUiThread
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.settings
import ru.blays.ficbookapi.FICBOOK_REGISTER_LINK
import kotlin.reflect.KFunction1

class DefaultLandingScreenComponent(
    componentContext: ComponentContext,
    private val onOutput: KFunction1<LandingScreenComponent.Output, Unit>
): LandingScreenComponent, ComponentContext by componentContext {
    private val repository: IAuthorizationRepo by getKoin().inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val dialogNavigation = SlotNavigation<ConfirmDialogConfig>()

    override val confirmDialog = childSlot(
        source = dialogNavigation,
        serializer = ConfirmDialogConfig.serializer(),
        initialConfiguration = { null },
    ) { configuration, componentContext ->
        ConfirmDialogComponent(
            componentContext = componentContext,
            onConfirm = {
                when(configuration) {
                    ConfirmDialogConfig.ConfirmAnonymousMode -> {
                        coroutineScope.launch {
                            repository.switchAnonymousMode(true)
                            settings.putBoolean(
                                key = SettingsKeys.FIRST_START_KEY,
                                value = false
                            )
                        }
                    }
                    ConfirmDialogConfig.ConfirmDBMigration -> {
                        coroutineScope.launch {
                            val success = repository.migrateDB()
                            if(success) {
                                settings.putBoolean(
                                    key = SettingsKeys.FIRST_START_KEY,
                                    value = false
                                )
                                runOnUiThread {
                                    onOutput(LandingScreenComponent.Output.Close)
                                }
                            } else {
                                dialogNavigation.activate(
                                    ConfirmDialogConfig.DBMigrationFailed
                                )
                            }
                        }
                    }
                    ConfirmDialogConfig.ConfirmRegisterRedirect -> {
                        openInBrowser(FICBOOK_REGISTER_LINK)
                    }
                    is ConfirmDialogConfig.DBMigrationFailed -> {
                        onOutput(LandingScreenComponent.Output.OpenLogInScreen)
                    }
                }
                dialogNavigation.dismiss()
            },
            onCancel = dialogNavigation::dismiss
        )
    }

    override fun sendIntent(intent: LandingScreenComponent.Intent) {
        when(intent) {
            LandingScreenComponent.Intent.AddNewAccount -> {
                onOutput(LandingScreenComponent.Output.OpenLogInScreen)
            }
            LandingScreenComponent.Intent.EnableAnonymousMode -> {
                dialogNavigation.activate(ConfirmDialogConfig.ConfirmAnonymousMode)
            }
            LandingScreenComponent.Intent.Register -> {
                dialogNavigation.activate(ConfirmDialogConfig.ConfirmRegisterRedirect)
            }
            LandingScreenComponent.Intent.CloseDialog -> {
                dialogNavigation.dismiss()
            }
        }
    }

    private suspend fun checkMigrationNeed(): Boolean {
        val hasSavedAccount = repository.hasSavedAccount
        val hasSavedCookie = repository.hasSavedCookies
        return !hasSavedAccount && hasSavedCookie
    }

    init {
        coroutineScope.launch {
            val needMigration = checkMigrationNeed()
            if(needMigration) {
                dialogNavigation.activate(ConfirmDialogConfig.ConfirmDBMigration)
            }
        }
    }
}