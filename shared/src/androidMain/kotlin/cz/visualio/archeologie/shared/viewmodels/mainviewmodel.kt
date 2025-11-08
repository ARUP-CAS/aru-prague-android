package cz.visualio.archeologie.shared.viewmodels

import arrow.core.Either
import arrow.core.getOrElse
import arrow.optics.optics
import cz.visualio.archeologie.shared.model.Thematic
import cz.visualio.archeologie.shared.model.res.Container
import cz.visualio.archeologie.shared.model.res.LocationRes
import cz.visualio.archeologie.shared.util.NetworkStateMachine
import cz.visualio.archeologie.shared.util.getOrElse
import cz.visualio.archeologie.shared.util.partially4
import cz.visualio.archeologie.shared.util.toNetworkStateMachine

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
            return locationSM.select()
                .getOrElse { emptyList() }
                .filter { it.id in activeThematic.locationIds }
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

                val texts = locationSM.getOrElse { emptyList() }.filter { it.id in thematic.locationIds }
                    .flatMap {
                        it.content.flatMap {
                            when(it){
                                is Container.Text -> it.content.map { it.text }
                                is Container.Model -> emptyList()
                                is Container.File -> emptyList()
                                is Container.Image -> it.content.map { it.text }
                                is Container.Video -> it.content.map { it.text }
                            }
                        }
                    }.map { it.toLowerCase() }

                thematic.locationIds.any { it in locationIds } || filterQuery.toLowerCase() inAny listOfNotNull(
                    thematic.artisticCooperation,
                    thematic.author,
                    thematic.professionalCooperation,
                    thematic.title
                ).map { it.toLowerCase() } || filterQuery.toLowerCase() inAny texts
            }
        }

    companion object
}

infix fun String.inAny(l: Collection<String>) = l.any { this in it }


sealed class ApplicationAction {
    internal data class SetThematicsSM(
        val value: NetworkStateMachine<Throwable, List<Thematic>>
    ) : ApplicationAction()

    internal data class SetLocationSM(
        val value: NetworkStateMachine<Throwable, List<LocationRes>>
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
    is ApplicationAction.SetThematicsSM -> ApplicationState.thematicSM.set(state, action.value)
    is ApplicationAction.SetLocationSM -> ApplicationState.locationSM.set(state, action.value)
    is ApplicationAction.SetActiveThematic -> ApplicationState.nullableActiveThematic.set(
        state,
        action.value
    )
    is ApplicationAction.SetActiveLocation -> ApplicationState.nullableActiveLocation.set(
        state,
        action.value
    )
    is ApplicationAction.SetFilterQuery -> ApplicationState.filterQuery.set(state, action.value)
    is ApplicationAction.SetSelectedFilters -> ApplicationState.activeFilters.set(
        state,
        action.value
    )
    is ApplicationAction.SetThematicBotomSheetExpanded -> ApplicationState.thematicBottomSheetExpanded.set(
        state,
        action.value
    )
    is ApplicationAction.SetLocationBotomSheetExpanded -> ApplicationState.locationBottomSheetExpanded.set(
        state,
        action.value
    )
    ApplicationAction.LoadThematics, ApplicationAction.LoadLocations -> state
}


suspend fun getEffects(
    state: ApplicationState,
    action: ApplicationAction,
    dispatch: Dispatch<ApplicationAction>,
    dependencies: Dependencies
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
    is ApplicationAction.SetLocationSM -> Either.right(Unit)
}


private suspend fun loadThematicsEffect(
    dispatch: Dispatch<ApplicationAction.SetThematicsSM>,
    loadThematics: suspend () -> Either<Throwable, List<Thematic>>
) =
    loadThematics()
        .toNetworkStateMachine()
        .let(ApplicationAction::SetThematicsSM)
        .let { dispatch(it) }

private suspend fun loadLocationsEffect(
    dispatch: Dispatch<ApplicationAction.SetLocationSM>,
    loadLocations: suspend () -> Either<Throwable, List<LocationRes>>
) =
    loadLocations()
        .toNetworkStateMachine()
        .let(ApplicationAction::SetLocationSM)
        .let { dispatch(it) }