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