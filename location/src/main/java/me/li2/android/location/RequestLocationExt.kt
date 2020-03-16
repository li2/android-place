/*
 * Created by Weiyi Li on 10/03/20.
 * https://github.com/li2
 */
package me.li2.android.location

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import me.li2.android.common.framework.PermissionResult
import me.li2.android.common.framework.requestLocationPermission

/**
 * Show location permission ((allow, deny, deny & don't ask again)) and service prompt dialog
 * to ask user to grant location permission and to turn on location service.
 *
 * @param onError will be executed when error happens
 * @param onResult will be executed when user made a choice.
 */
fun FragmentActivity.ifLocationAllowed(
        onError: (Throwable) -> Unit = {},
        onResult: (RequestLocationResult) -> Unit): Disposable {
    return RequestLocationUtils.checkLocationPermissionAndService(this)
            .subscribeBy(onNext = {
                onResult(it)
            }, onError = {
                onError(it)
            })
}

/**
 * Open system location settings page.
 *
 * @param onResult will be executed when user navigate back, true if user turn on location service.
 */
fun FragmentActivity.openSystemLocationSetting(onResult: (Boolean) -> Unit): Disposable {
    return RxActivityResult(this)
            .start(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            .subscribeBy {
                onResult(LocationServiceUtil.isLocationServiceEnabled(this))
            }
}

/**
 * Open application settings page.
 *
 * @param appId application ID
 */
fun FragmentActivity.openAppSettings(appId: String) {
     startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$appId")))
}

private object RequestLocationUtils {
    fun checkLocationPermissionAndService(activity: FragmentActivity): Observable<RequestLocationResult> {
        return activity.requestLocationPermission()
                .flatMap { permissionResult ->
                    when (permissionResult) {
                        PermissionResult.GRANTED -> {
                            if (LocationServiceUtil.isLocationServiceEnabled(activity)) {
                                Observable.just(RequestLocationResult.ALLOWED)
                            } else {
                                LocationServiceUtil.requestLocationService(activity).map { isServiceEnabled ->
                                    if (isServiceEnabled) {
                                        RequestLocationResult.ALLOWED
                                    } else {
                                        RequestLocationResult.SERVICE_OFF
                                    }
                                }
                            }
                        }
                        PermissionResult.DENIED -> {
                            Observable.just(RequestLocationResult.PERMISSION_DENIED)
                        }
                        PermissionResult.DENIED_NOT_ASK_AGAIN -> {
                            Observable.just(RequestLocationResult.PERMISSION_DENIED_NOT_ASK_AGAIN)
                        }
                    }
                }
    }
}
