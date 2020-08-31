/**Copyright 2020 Michael Hall

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.**/
package uk.me.mikemike.taionneko.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import uk.me.mikemike.taionneko.BuildConfig
import uk.me.mikemike.taionneko.R
import uk.me.mikemike.taionneko.ui.fragments.Dashboard
import uk.me.mikemike.taionneko.ui.fragments.TemperatureEntryList
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val DELETE_DATA_IN_DEBUG_MODE = true
        const val DASHBOARD_FRAGMENT_TAG = "dashboard"
        const val LIST_FRAGMENT_TAG = "list"
    }

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dashboard = Dashboard.newInstance()
        val list = TemperatureEntryList.newInstance()

        supportFragmentManager.beginTransaction().apply{
            add(R.id.fragmentroot, dashboard, DASHBOARD_FRAGMENT_TAG)
            add(R.id.fragmentroot, list, LIST_FRAGMENT_TAG)
            hide(list)
        }.commit()

        currentFragment = dashboard

        bottomNavigation.setOnNavigationItemSelectedListener {
            val new =
            when(it.itemId){
                R.id.dashboard -> {
                    supportFragmentManager.findFragmentByTag(DASHBOARD_FRAGMENT_TAG)
                }
                R.id.entries ->{
                    supportFragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG)
                }
                else -> {null}
            }

            if(new != null){
                supportFragmentManager.beginTransaction().apply {
                    hide(currentFragment)
                    show(new)
                }.commit()
                currentFragment = new
            }
            else{
                /// this is a weird error that shouldn't happen - what to do here?
                // TODO : Decide course of action if fragment disapears ...
            }

            true
        }




        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        if(BuildConfig.DEBUG && DELETE_DATA_IN_DEBUG_MODE) {
            Log.i("TaionNeko Main Activity", "Debug mode detected and DELETE_DATA_IN_DEBUG_MODE set, deleting all data")
            viewModel.deleteAll()
        }
    }
}