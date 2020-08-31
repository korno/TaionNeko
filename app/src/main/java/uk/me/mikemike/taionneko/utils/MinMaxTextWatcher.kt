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
package uk.me.mikemike.taionneko.utils


import android.view.View
import android.widget.EditText
import android.util.Log

class MinMaxTextWatcher(var min: Float,  var max:Float, var belowMin: (value: Float) -> Unit,
                var aboveMax: (value: Float)->Unit
                ) : View.OnFocusChangeListener {
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
       if(!hasFocus) {
           if (v is EditText) {

               val f = v.text.toString().toFloatOrNull()
               Log.i("Checkign", "check test $f")
               v.setText(when{
                   f == null -> {null}
                   f < min -> {
                       belowMin(f)
                       min.toString()}
                   f > max -> {
                       aboveMax(f)
                       max.toString()}
                   else -> {f.toString()}
               })
           }
       }
    }

}