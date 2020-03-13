package me.li2.android.placesample

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.forUi(): Observable<T> =
    this.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())

fun <T> Observable<T>.throttleFirstShort() = this.throttleFirst(500L, TimeUnit.MILLISECONDS)!!

/**
 * Return query text changes observable.
 *
 * - filter: to filter undesired text like blank text to avoid unnecessary API call.
 * - debounce: to ignore the previous items in the given time and only emit the last one, to avoid too much API calls.
 * - distinctUntilChanged: to avoid the duplicate API call.
 */
fun EditText.queryTextChanges(): Observable<String> {
    return textChanges()
        .map { it.toString() }
        .filter { it.isNotBlank() }
        .debounce(300, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
}

fun Fragment.toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

@BindingAdapter("android:visibility")
fun setViewVisibility(view: View, value: Boolean?) {
    view.visibility = if (value == true) View.VISIBLE else View.GONE
}


@BindingAdapter("android:src")
fun setImageUrl(view: ImageView, src: String) {
    Glide.with(view.context)
        .load(src)
        .into(view)
}
