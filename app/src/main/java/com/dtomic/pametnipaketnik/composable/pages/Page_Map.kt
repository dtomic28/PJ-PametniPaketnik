package com.dtomic.pametnipaketnik.composable.pages

import android.R.attr.data
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.algorithms.GA
import com.dtomic.core.TSP
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.DataCache
import com.dtomic.pametnipaketnik.utils.DirectionsAPI
import com.dtomic.pametnipaketnik.utils.DistanceMatrixAPI
import com.dtomic.pametnipaketnik.utils.GeocodingAPI
import com.dtomic.pametnipaketnik.utils.PacketParcer
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.toIntArray

enum class DisplayOptions { Distance, Time, Coords }
fun Set<Int>.toSortedIntArray(): IntArray = this.toIntArray().sortedArray()

data class CacheState(
    val ready: Boolean = false,
    val isDownloading: Boolean = false,
    val error: String = ""
)

private fun formatKm(meters: Int): String {
    val km = meters / 1000.0
    return String.format("%.1f km", km)
}

private fun formatHms(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "%dh %02dm".format(h, m) else "%dm".format(m)
}

class MapViewModel(app: Application) : AndroidViewModel(app) {
    @SuppressLint("StaticFieldLeak")
    val context = getApplication<Application>().applicationContext;
    val towns = listOf("Goriška+cesta+24,+5270+Ajdovščina", "Čolnikov+trg+9,+2234+Benedikt", "Ljubljanska+cesta+10,+4260+Bled", "Trg+svobode+2c,+4264+Bohinjska+Bistrica", "Molkov+trg+12,+1353+Borovnica", "Obrobna+ulica+1,+2354+Bresternica", "Podpeška+cesta+20,+1351+Brezovica+pri+Ljubljani", "Ulica+stare+pravde+34,+8250+Brežice", "Krekov+trg+9,+3000+Celje", "Pošta,+Ulica+Prekmurske+čete+14,+9232+Črenšovci", "Center+16,+2393+Črna+na+Koroškem", "Kolodvorska+cesta+30a,+8340+Črnomelj", "Krtina+144,+1233+Dob", "Ulica+bratov+Gerjovičev+52,+8257+Dobova", "1+%26+Trg+4.+julija,+2370+Dravograd", "Gorišnica+79,+2272+Gorišnica", "Cesta+na+stadion+1,+9250+Gornja+Radgona", "Podsmrečje+20,+3342+Gornji+Grad", "Attemsov+trg+8,+3342+Gornji+Grad", "Partizanska+cesta+7,+1290+Grosuplje", "Vodnikova+ulica+1,+5280+Idrija", "Ploščad+Osvobodilne+fronte+4,+1295+Ivančna+Gorica", "Ivanjkovci+9b,+2259+Ivanjkovci", "Cankarjev+drevored+1,+6310+Izola", "Cesta+Cirila+Tavčarja+8,+4270+Jesenice", "Jesenice+9,+8261+Jesenice+na+Dolenjskem", "Cesta+v+Rošpoh+18,+2351+Kamnica", "Ljubljanska+cesta+14a,+1241+Kamnik", "Pošta,+Kopališka+ulica+2,+2325+Kidričevo", "Kobilje+30,+9227+Kobilje", "Ljubljanska+cesta+23,+1330+Kočevje", "Pošta,+Glavarjeva+cesta+39,+1218+Komenda", "Kolodvorska+cesta+9,+6000+Koper", "Dražgoška+ulica+8,+4000+Kranj", "Škofjeloška+cesta+17,+4000+Kranj", "Jezerska+cesta+41,+4000+Kranj", "Ulica+Lojzeta+Hrovata+2,+4000+Kranj", "Borovška+cesta+92,+4280+Kranjska+Gora", "Trg+Matije+Gubca+1,+8270+Krško", "v+S.g,+Partizanska+cesta+3,+2230+Lenart+v+Slovenskih+Goricah", "Trg+ljudske+pravice+7,+9220+Lendava", "Alpska+cesta+37+B,+4248+Lesce", "Poljanski+nasip+30,+1000+Ljubljana", "Pražakova+Ul+3,+1106+Ljubljana", "Zaloška+cesta+57,+1000+Ljubljana", "Dunajska+c.+141,+1113+Ljubljana", "Riharjeva+ulica+38,+1000+Ljubljana", "Vodnikova+cesta+235,+1000+Ljubljana", "Resljeva+c.+14,+1124+Ljubljana", "etališka+cesta+12,+1000+Ljubljana", "Tacenska+cesta+94,+1000+Ljubljana", "Hrvaška+ulica+8,+1000+Ljubljana", "Ob+železnici+22,+1000+Ljubljana", "Hranilniška+ulica+1,+1000+Ljubljana", "Dunajska+c.+145,+1000+Ljubljana", "Litijska+cesta+140", "Tehnološki+park+22a,+1000+Ljubljana", "Tržaška+c.+89,+1000+Ljubljana", "Avtocestno+počivališče+Barje,+Cesta+dveh+cesarjev+73,+1000+Ljubljana", "BS,+Cesta+dveh+cesarjev+71,+1000+Ljubljana", "Dunajska+c.+361,+1231+Ljubljana+-+Črnuče", "Zadobrovška+cesta+14,+1260+Ljubljana+-+Polje", "/Prušnikova+Ul.+2,+1210+Ljubljana+-+Šentvid", "Makole+37,+2321+Makole", "Malečnik+56,+2229+Malečnik", "Dominkuševa+ulica+4,+2000+Maribor", "Istrska+ulica+49,+2000+Maribor", "Gosposvetska+Cesta+83,+2000+Maribor", "Gosposvetska+Cesta+84,+2000+Maribor", "Arena,+Pohorska+ulica+21a,+2000+Maribor", "Prvomajska+ulica+35,+2000+Maribor", "Radvanjska+cesta+63,+2000+Maribor", "Razlagova+ulica+3,+2000+Maribor", "Šarhova+ulica+53,+2000+Maribor", "Šarhova+ulica+59+A,+2106+Maribor", "Tyrševa+ulica+23,+2000+Maribor", "Ulica+heroja+Šlandra+15,+2000+Maribor", "Trdinov+trg+8a,+1234+Mengeš", "Naselje+Borisa+Kidriča+2,+8330+Metlika", "Cesta+na+Fužine+3,+8233+Mirna", "Vegova+ulica+1,+1251+Moravče", "Savinjska+cesta+3,+3330+Mozirje", "Trg+zmage+6,+9000+Murska+Sobota", "Ulica+Štefana+Kovača+43,+9000+Murska+Sobota", "Glavni+trg+31,+2366+Muta", "/Glavna+cesta+24,+4202+Naklo", "Kidričeva+ulica+19,+5000+Nova+Gorica", "Industrijska+cesta+9,+5000+Nova+Gorica", "Ulica+Slavka+Gruma+7,+8000+Novo+mesto", "Novi+trg+7,+8000+Novo+mesto", "Poštna+ulica+2,+2270+Ormož", "Gornji+Petrovci+40,+9203+Petrovci", "Malteška+Cesta+38,+3313+Polzela", "Ulica+1.+maja+2a,+6230+Postojna", "Pošta,+Šiška+1,+4205+Preddvor", "Trg+32a,+2391+Prevalje", "Mariborska+Cesta+19,+2250+Ptuj", "Ljubljanska+cesta+14,+2327+Rače", "Panonska+cesta+5,+9252+Radenci", "Trg+svobode+19,+2390+Ravne+na+Koroškem", "Pošta,+Kolodvorska+ulica+2,+1310+Ribnica", "Slovenski+trg+4a,+2352+Selnica+ob+Dravi", "Trg+svobode+9,+8290+Sevnica", "Partizanska+c.+48+A,+6210+Sežana", "Kidričeva+ulica+3a,+2380+Slovenj+Gradec", "Cesta+k+Dravi+5,+2241+Spodnji+Duplek", "Vrtojbenska+cesta+19+C,+5290+Šempeter+pri+G", "Gasilska+cesta+2a,+4208+Šenčur", "Prvomajska+cesta+3b,+8310+Šentjernej", "/Mestni+trg+5a,+3230+Šentjur", "Kapucinski+trg+14,+4220+Škofja+Loka", "Pošta,+Aškerčev+trg+26,+3240+Šmarje+pri+Jelšah", "Trg+maršala+Tita+10,+5220+Tolmin", "Trg+revolucije+27,+1420+Trbovlje", "Trg+Franca+Fakina+4,+1420+Trbovlje", "Goliev+trg+11,+8210+Trebnje", "Predilniška+cesta+10,+4290+Tržič", "Kidričeva+cesta+2a,+3320+Velenje", "Poštna+ulica+2,+1360+Vrhnika", "Tržaška+cesta+32,+1360+Vrhnika", "Cesta+zmage+28,+1410+Zagorje+ob+Savi", "Mariborska+ulica+26,+2314+Zgornja+Polskava", "Ulica+heroja+Staneta+1,+3310+Žalec", "Na+Kresu+1,+4228+Železniki", "Trg+svobode+1,+4226+Žiri", "Pošta+4274+Žirovnica" )

    data class RouteTotals(
        val distanceMeters: Int = 0,
        val durationSeconds: Int = 0
    )

    private val _routeTotals = MutableStateFlow(RouteTotals())
    val routeTotals: StateFlow<RouteTotals> = _routeTotals

    private val _displayOption = MutableStateFlow(DisplayOptions.Distance)
    val displayOption: StateFlow<DisplayOptions> = _displayOption

    private val _selectedTownIndexes = MutableStateFlow<Set<Int>>(emptySet())
    val selectedTownIndexes: StateFlow<Set<Int>> = _selectedTownIndexes

    private val _routePath = MutableStateFlow<List<com.google.android.gms.maps.model.LatLng>>(emptyList())
    val routePath: StateFlow<List<com.google.android.gms.maps.model.LatLng>> = _routePath

    fun setDisplayOption(option: DisplayOptions) {
        _displayOption.value = option
    }
    fun runAlgorithm() {
        val directionsAPI = DirectionsAPI(context)
        val packetParcer = PacketParcer(context)

        val popSize = 100
        val cr = 0.8
        val pm = 0.1

        val selected = selectedTownIndexes.value.toIntArray().sortedArray()
        require(selected.size >= 2) { "Select at least 2 towns." }

        val data = when (displayOption.value) {
            DisplayOptions.Coords -> packetParcer.fromCoords(selected.toTypedArray())
            DisplayOptions.Time -> packetParcer.fromDuractions(selected.toTypedArray())
            DisplayOptions.Distance -> packetParcer.fromDistances(selected.toTypedArray())
        }

        val maxFes = 1000 * data.dimension
        val problem = TSP(data, maxFes)
        val ga = GA(popSize, cr, pm)

        val result = ga.execute(problem)

        val routeInSelectedSpace = buildList {
            add(0)
            result.order.forEach { add(it) }
            add(0)
        }

        val routeGlobal = routeInSelectedSpace.map { selected[it] }

        viewModelScope.launch(Dispatchers.IO) {
            val points = routeGlobal.map { towns[it] }
            val path = directionsAPI.buildPath(points)
            _routePath.value = path

            val distMatrix = readMatrixInts(DataCache.DISTANCES)
            val durMatrix = readMatrixInts(DataCache.DURATIONS)

            var totalMeters = 0
            var totalSeconds = 0
            for (i in 0 until routeGlobal.size - 1) {
                val a = routeGlobal[i]
                val b = routeGlobal[i + 1]
                totalMeters += distMatrix[a][b]
                totalSeconds += durMatrix[a][b]
            }

            _routeTotals.value = RouteTotals(
                distanceMeters = totalMeters,
                durationSeconds = totalSeconds
            )
        }
    }

    fun toggleTown(index: Int) {
        _selectedTownIndexes.value =
            _selectedTownIndexes.value.toMutableSet().also { set ->
                if (!set.add(index)) set.remove(index)
            }
    }
    fun clearTowns() {
        _selectedTownIndexes.value = emptySet()
    }
    private fun readMatrixInts(fileName: String): List<List<Int>> {
        return DataCache.file(context, fileName).readLines().map { line ->
            line.split(" ").map { it.toInt() }
        }
    }
}

@Composable
fun Page_Map(navController: NavController, viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val path = viewModel.routePath.collectAsState().value
    val cacheReady = remember { mutableStateOf(DataCache.isReady(context)) }

    LaunchedEffect(Unit) {
        if (!DataCache.isReady(context)) {
            withContext(Dispatchers.IO) {
                DistanceMatrixAPI(context).getData(viewModel.towns, viewModel.towns, context)
                GeocodingAPI(context).getCoordinates(viewModel.towns, context)
            }
        }
        cacheReady.value = DataCache.isReady(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.7f),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    if (path.isNotEmpty()) {
                        Polyline(points = path)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f)
                    .padding(10.dp)
            )
            {
                val selectedOption = viewModel.displayOption.collectAsState().value
                val options = DisplayOptions.entries

                Row(Modifier.selectableGroup().fillMaxWidth()) {
                    options.forEach { option ->
                        Column(
                            Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .selectable(
                                    selected = option == selectedOption,
                                    onClick = { viewModel.setDisplayOption(option) },
                                    role = Role.RadioButton
                                ),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RadioButton(selected = option == selectedOption, onClick = null)
                            Text(text = option.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .weight(0.05f)
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.Absolute.Center
            ) {
                val totals = viewModel.routeTotals.collectAsState().value

                Text(
                    text = "Distance: ${formatKm(totals.distanceMeters)}   Time: ${formatHms(totals.durationSeconds)}"
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.15f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            )
            {
                Custom_Button(
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.45f),
                    text = stringResource(R.string.btn_chooseTown),
                    onClick = { navController.navigate("ChooseTownPage") },
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                )
                Custom_Button(
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.45f),
                    text = stringResource(R.string.btn_runAlgorithm),
                    onClick = { viewModel.runAlgorithm() },
                    enabled = cacheReady.value
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val navController = rememberNavController()
    AppTheme {
        Page_Map(navController)
    }
}