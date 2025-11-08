package cz.visualio.archeologie.androidApp.viewmodels

import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.google.firebase.crashlytics.FirebaseCrashlytics
import cz.visualio.archeologie.shared.service.ApiRepository
import cz.visualio.archeologie.shared.viewmodels.ApplicationAction
import cz.visualio.archeologie.shared.viewmodels.ApplicationState
import cz.visualio.archeologie.shared.viewmodels.MVI
import cz.visualio.archeologie.shared.viewmodels.MainMVI
import java.util.*


@Suppress("RedundantSuspendModifier")
suspend fun <ACTION> logAction(action: ACTION): Either<Nothing, Unit> {
    Log.d("ACTION", action.toString())
    return Either.right(Unit)
}

suspend fun logError(t: Throwable) = Either.catch {
    Log.e("ERROR", t.message, t)
    FirebaseCrashlytics.getInstance().recordException(t)
}



class AndroidApplicationViewModel : ViewModel(),
    MVI<ApplicationState, ApplicationAction> by MainMVI(
        logAction = ::logAction,
        logError = ::logError,
        fetchLocations = { ApiRepository().getLocations(getAcceptedLanguageHeaderValue()) },
        fetchThematics = { ApiRepository().getThematics(getAcceptedLanguageHeaderValue()) },
    )

fun getAcceptedLanguageHeaderValue(): String {
    var weight = 1.0F
    return getPreferredLocaleList()
        .map { it.toLanguageTag() }
        .reduce { accumulator, languageTag ->
            weight -= 0.1F
            "$accumulator,$languageTag;q=$weight"
        }
}

fun getPreferredLocaleList(): List<Locale> {
    val adjustedLocaleListCompat: LocaleListCompat = LocaleListCompat.getAdjustedDefault()

    return (0 until adjustedLocaleListCompat.size()).fold(mutableListOf(), { acc, index ->
        acc.add(adjustedLocaleListCompat.get(index))
        acc
    })
}