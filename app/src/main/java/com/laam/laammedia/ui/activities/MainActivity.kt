package com.laam.laammedia.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.laam.laammedia.R
import com.laam.laammedia.ui.fragments.DiscoverFragment
import com.laam.laammedia.ui.fragments.HomeFragment
import com.laam.laammedia.ui.fragments.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val fragment = HomeFragment.newInstance()
        addFragment(fragment)

        main_floating_action_button_add.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.navigation_home -> {
                val fragment : Fragment =
                    HomeFragment.newInstance()
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_discover -> {
                val fragment : Fragment =
                    DiscoverFragment.newInstance()
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_profile -> {
                val fragment : Fragment =
                    ProfileFragment.newInstance()
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun addFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.layout_content, fragment, fragment.javaClass.simpleName)
            .commit()
    }
}
