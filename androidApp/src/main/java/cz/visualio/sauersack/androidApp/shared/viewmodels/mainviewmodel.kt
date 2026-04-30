package cz.visualio.sauersack.androidApp.shared.viewmodels

import arrow.core.Either
import arrow.core.getOrElse
import arrow.optics.optics
import cz.visualio.sauersack.androidApp.shared.Container
import cz.visualio.sauersack.androidApp.shared.LocationRes
import cz.visualio.sauersack.androidApp.shared.model.Thematic
import cz.visualio.sauersack.androidApp.shared.util.NetworkStateMachine
import cz.visualio.sauersack.androidApp.shared.util.getOrElse
import cz.visualio.sauersack.androidApp.shared.util.partially4
import cz.visualio.sauersack.androidApp.shared.util.toNetworkStateMachine


@optics
data class ApplicationState(
    val thematicSM: NetworkStateMachine<Throwable, List<Thematic>> = NetworkStateMachine.Init,
    val locationSM: NetworkStateMachine<Throwable, List<LocationRes>> = NetworkStateMachine.Init,
    val filterQuery: String = "",
    val activeFilters: Set<String> = emptySet(),
    val activeThematic: Thematic? = null,
    val activeLocation: LocationRes? = null,
    val thematicBottomSheetExpanded: Boolean = false,
    val locationBottomSheetExpanded: Boolean = false,
) {

    val filteredLocations: List<LocationRes>
        get() {
            activeThematic ?: return emptyList()
            val list = locationSM.select()
                .getOrElse { emptyList() }
                .filter { it.id in activeThematic.locationIds }

            val locationById = list.associateBy { it.id }
            return activeThematic.locationIds.mapNotNull { locationById[it] }
        }

    val filteredThematics: List<Thematic>
        get() {
            val thematics = thematicSM.getOrElse { emptyList() }
            val locations = locationSM.getOrElse { emptyList() }


            val locationIds =
                locations.filter {
                    it.address.contains(filterQuery, ignoreCase = true)
                }
                    .map(LocationRes::id)
                    .toSet()

            return thematics.filter { thematic: Thematic ->

                val texts =
                    locationSM.getOrElse { emptyList() }.filter { it.id in thematic.locationIds }
                        .flatMap {
                            it.content.flatMap {
                                when (it) {
                                    is Container.Text -> it.content.map { it.text }
                                    is Container.Model -> emptyList()
                                    is Container.File -> emptyList()
                                    is Container.Image -> it.content.map { it.text }
                                    is Container.Video -> it.content.map { it.text }
                                    is Container.SphereImages -> it.content.map { it.text }

                                }
                            }
                        }.map { it.lowercase() }

                thematic.locationIds.any { it in locationIds } || filterQuery.lowercase() inAny listOfNotNull(
                    thematic.artisticCooperation,
                    thematic.author,
                    thematic.professionalCooperation,
                    thematic.title
                ).map { it.lowercase() } || filterQuery.lowercase() inAny texts
            }
        }

    companion object
}

infix fun String.inAny(l: Collection<String>) = l.any { this in it }


sealed class ApplicationAction {
    internal data class SetThematicsSM(
        val value: NetworkStateMachine<Throwable, List<Thematic>>,
    ) : ApplicationAction()

    internal data class SetLocationSM(
        val value: NetworkStateMachine<Throwable, List<LocationRes>>,
    ) : ApplicationAction()

    object LoadThematics : ApplicationAction()
    object LoadLocations : ApplicationAction()

    data class SetActiveThematic(val value: Thematic?) : ApplicationAction()
    data class SetActiveLocation(val value: LocationRes?) : ApplicationAction()
    data class SetFilterQuery(val value: String) : ApplicationAction()
    data class SetSelectedFilters(val value: Set<String>) : ApplicationAction()

    data class SetThematicBotomSheetExpanded(val value: Boolean) : ApplicationAction()
    data class SetLocationBotomSheetExpanded(val value: Boolean) : ApplicationAction()
}

sealed class ApplicationError

data class Dependencies(
    val fetchThematics: suspend () -> Either<Throwable, List<Thematic>>,
    val fetchLocations: suspend () -> Either<Throwable, List<LocationRes>>,
)

@Suppress("FunctionName")
fun MainMVI(
    logError: suspend (Throwable) -> Either<Throwable, Unit>,
    logAction: suspend (ApplicationAction) -> Either<Throwable, Unit>,
    fetchThematics: suspend () -> Either<Throwable, List<Thematic>>,
    fetchLocations: suspend () -> Either<Throwable, List<LocationRes>>,
) = MVI(
    initialState = ApplicationState(),
    logError = logError,
    logAction = logAction,
    reducer = ::reducer,
    getEffect = ::getEffects.partially4(
        Dependencies(
            fetchThematics = fetchThematics,
            fetchLocations = fetchLocations,
        )
    ),
)

fun reducer(state: ApplicationState, action: ApplicationAction): ApplicationState = when (action) {
    is ApplicationAction.SetThematicsSM -> state.copy(thematicSM = action.value)
    is ApplicationAction.SetLocationSM -> state.copy(locationSM = action.value)
    is ApplicationAction.SetActiveThematic -> state.copy(activeThematic = action.value)

    is ApplicationAction.SetActiveLocation -> state.copy(activeLocation = action.value)

    is ApplicationAction.SetFilterQuery -> state.copy(filterQuery = action.value)
    is ApplicationAction.SetSelectedFilters -> state.copy(activeFilters = action.value)

    is ApplicationAction.SetThematicBotomSheetExpanded -> state.copy(thematicBottomSheetExpanded = action.value)

    is ApplicationAction.SetLocationBotomSheetExpanded -> state.copy(
        locationBottomSheetExpanded =
        action.value
    )

    ApplicationAction.LoadThematics, ApplicationAction.LoadLocations -> state
}


suspend fun getEffects(
    state: ApplicationState,
    action: ApplicationAction,
    dispatch: Dispatch<ApplicationAction>,
    dependencies: Dependencies,
): Either<Throwable, Unit> = when (action) {
    ApplicationAction.LoadThematics -> loadThematicsEffect(dispatch, dependencies.fetchThematics)
    ApplicationAction.LoadLocations -> loadLocationsEffect(dispatch, dependencies.fetchLocations)
    is ApplicationAction.SetThematicBotomSheetExpanded,
    is ApplicationAction.SetLocationBotomSheetExpanded,
    is ApplicationAction.SetSelectedFilters,
    is ApplicationAction.SetFilterQuery,
    is ApplicationAction.SetActiveThematic,
    is ApplicationAction.SetActiveLocation,
    is ApplicationAction.SetThematicsSM,
    is ApplicationAction.SetLocationSM,
    -> Either.right(Unit)
}


private suspend fun loadThematicsEffect(
    dispatch: Dispatch<ApplicationAction.SetThematicsSM>,
    loadThematics: suspend () -> Either<Throwable, List<Thematic>>,
) =
    loadThematics()
        .toNetworkStateMachine()
        .let(ApplicationAction::SetThematicsSM)
        .let { dispatch(it) }

private suspend fun loadLocationsEffect(
    dispatch: Dispatch<ApplicationAction.SetLocationSM>,
    loadLocations: suspend () -> Either<Throwable, List<LocationRes>>,
) =
    loadLocations()
        .toNetworkStateMachine()
        .let(ApplicationAction::SetLocationSM)
        .let { dispatch(it) }