package com.example.kkarhua

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var cartBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar ActionBar con Navigation
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.splashFragment,
                R.id.homeFragment,
                R.id.productListFragment,
                R.id.cartFragment,
                R.id.adminPanelFragment // ✅ Agregado
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                R.id.navigation_dashboard -> {
                    if (navController.currentDestination?.id != R.id.productListFragment) {
                        navController.navigate(R.id.productListFragment)
                    }
                    true
                }
                R.id.navigation_notifications -> {
                    if (navController.currentDestination?.id != R.id.cartFragment) {
                        navController.navigate(R.id.cartFragment)
                    }
                    true
                }
                // ✅ Eliminamos el botón de agregar producto del BottomNav
                else -> false
            }
        }

        // Ocultar/Mostrar BottomNav según la pantalla actual
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // ✅ Fragmentos donde NO debe aparecer el BottomNav
            val hideBottomNavFragments = setOf(
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.adminPanelFragment,  // ✅ Ocultar en panel admin
                R.id.addProductFragment   // ✅ Ocultar al agregar producto
            )

            if (destination.id in hideBottomNavFragments) {
                bottomNav.visibility = android.view.View.GONE
            } else {
                bottomNav.visibility = android.view.View.VISIBLE
            }

            // Sincronizar botón seleccionado
            when (destination.id) {
                R.id.homeFragment -> bottomNav.selectedItemId = R.id.navigation_home
                R.id.productListFragment -> bottomNav.selectedItemId = R.id.navigation_dashboard
                R.id.cartFragment -> bottomNav.selectedItemId = R.id.navigation_notifications
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun updateCartBadge(count: Int) {
        cartBadge?.number = count
        cartBadge?.isVisible = count > 0
    }
}