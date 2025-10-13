package com.example.kkarhua.ui.home

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R
import com.example.kkarhua.utils.LocationHelper

class HomeFragment : Fragment() {

    private lateinit var locationHelper: LocationHelper
    private lateinit var btnVerProducts: Button
    private lateinit var btnFindStores: Button
    private lateinit var titleText: TextView

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNearbyStores()
        } else {
            locationHelper.showPermissionDeniedMessage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireContext())

        setupViews(view)
        setupAnimations()
        setupListeners()
    }

    private fun setupViews(view: View) {
        titleText = view.findViewById(R.id.titleText)
        btnVerProducts = view.findViewById(R.id.btnVerProducts)
        btnFindStores = view.findViewById(R.id.btnFindStores)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        btnVerProducts.startAnimation(slideUp)
        btnFindStores.startAnimation(slideUp)
    }

    private fun setupListeners() {
        btnVerProducts.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_homeFragment_to_productListFragment)
        }

        btnFindStores.setOnClickListener {
            animateButtonClick(it)
            requestLocationAndFindStores()
        }
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }

    private fun requestLocationAndFindStores() {
        when {
            locationHelper.hasLocationPermission() -> {
                findNearbyStores()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    requireContext(),
                    "Necesitamos tu ubicación para mostrar tiendas cercanas",
                    Toast.LENGTH_LONG
                ).show()
                locationHelper.requestLocationPermission(locationPermissionLauncher)
            }
            else -> {
                locationHelper.requestLocationPermission(locationPermissionLauncher)
            }
        }
    }

    private fun findNearbyStores() {
        locationHelper.getCurrentLocation(
            onSuccess = { location ->
                Toast.makeText(
                    requireContext(),
                    "Tu ubicación: ${location.latitude}, ${location.longitude}\n" +
                            "Tiendas cercanas encontradas (demo)",
                    Toast.LENGTH_LONG
                ).show()
            },
            onFailure = { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error al obtener ubicación: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}