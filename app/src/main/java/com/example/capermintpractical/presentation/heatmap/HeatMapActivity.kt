package com.example.capermintpractical.presentation.heatmap

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.example.capermintpractical.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import org.json.JSONArray

class HeatMapActivity : FragmentActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_heat_map)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val data = generateHeatMapData()

        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .weightedData(data)
            .radius(50)
            .maxIntensity(1000.0)
            .build()

        googleMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))

        val indiaLte = LatLng(20.5937, 78.9629)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indiaLte, 5f))
    }

    private fun generateHeatMapData(): ArrayList<WeightedLatLng>{
        val data = ArrayList<WeightedLatLng>()

        val jsonData = getJsonDataFromAsset( )
        jsonData?.let{
            for (i in 0 until it.length()){
                val entry = it.getJSONObject(i)
                val lat = entry.getDouble("lat")
                val lng = entry.getDouble("lng")
                val density = entry.getDouble("density")

                if (density!= 0.0){
                    data.add(WeightedLatLng(LatLng(lat, lng), density))
                }
            }
        }
        return data
    }

    private fun getJsonDataFromAsset(): JSONArray?{
        try {
            val jsonString = assets.open("heatmap.json").bufferedReader().use { it.readText() }
            return JSONArray(jsonString)
        } catch (e: Exception){
            e.printStackTrace()
            return null
        }
    }
}