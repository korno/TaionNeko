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
import androidx.lifecycle.ViewModelProvider
import uk.me.mikemike.taionneko.BuildConfig
import uk.me.mikemike.taionneko.R

class MainActivity : AppCompatActivity() {

    companion object {
        const val DELETE_DATA_IN_DEBUG_MODE = true
    }

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        if(BuildConfig.DEBUG && DELETE_DATA_IN_DEBUG_MODE) {
            Log.i("TaionNeko Main Activity", "Debug mode detected and DELETE_DATA_IN_DEBUG_MODE set, deleting all data")
            viewModel.deleteAll()
        }
    }
}