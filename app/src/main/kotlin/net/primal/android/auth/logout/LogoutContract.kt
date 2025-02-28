package net.primal.android.auth.logout

interface LogoutContract {

    sealed class UiEvent {
        data object LogoutConfirmed : UiEvent()
    }

    sealed class SideEffect {
        data object Close : SideEffect()
        data object NavigateToWelcome : SideEffect()
        data object NavigateToHome : SideEffect()
    }
}
