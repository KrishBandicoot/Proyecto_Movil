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
                R.id.homeFragment,
                R.id.productListFragment,
                R.id.cartFragment,
                R.id.addProductFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar BottomNavigationView
        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.let { bottomNav ->
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        // Navegar limpiamente al home
                        navController.popBackStack(R.id.homeFragment, false)
                        true
                    }
                    R.id.navigation_dashboard -> {
                        // Navegar a la lista de productos
                        if (navController.currentDestination?.id != R.id.productListFragment) {
                            navController.navigate(R.id.productListFragment)
                        }
                        true
                    }
                    R.id.navigation_notifications -> {
                        // Navegar al carrito
                        if (navController.currentDestination?.id != R.id.cartFragment) {
                            navController.navigate(R.id.cartFragment)
                        }
                        true
                    }
                    R.id.navigation_add_product -> {
                        // Navegar a agregar producto
                        if (navController.currentDestination?.id != R.id.addProductFragment) {
                            navController.navigate(R.id.addProductFragment)
                        }
                        true
                    }
                    else -> false
                }
            }

            // Sincronizar el botón seleccionado con el destino actual
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeFragment -> bottomNav.selectedItemId = R.id.navigation_home
                    R.id.productListFragment -> bottomNav.selectedItemId = R.id.navigation_dashboard
                    R.id.cartFragment -> bottomNav.selectedItemId = R.id.navigation_notifications
                    R.id.addProductFragment -> bottomNav.selectedItemId = R.id.navigation_add_product
                }
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