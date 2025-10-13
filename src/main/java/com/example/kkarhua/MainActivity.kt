package com.example.kkarhua

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
                R.id.cartFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar BottomNavigationView si lo tienes
        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.let { bottomNav ->
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        navController.navigate(R.id.homeFragment)
                        true
                    }
                    R.id.nav_products -> {
                        navController.navigate(R.id.productListFragment)
                        true
                    }
                    R.id.nav_cart -> {
                        navController.navigate(R.id.cartFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // Configurar badge del carrito
        menu?.findItem(R.id.action_cart)?.let { menuItem ->
            // Aquí puedes agregar un badge si usas Material Components
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                navController.navigate(R.id.cartFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun updateCartBadge(count: Int) {
        // Actualizar badge del carrito
        cartBadge?.number = count
        cartBadge?.isVisible = count > 0
    }
}
