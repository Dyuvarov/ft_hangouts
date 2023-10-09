package com.ugreyiro.ft_hangouts.activity

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ugreyiro.ft_hangouts.db.HEADER_COLOR_SETTING_NAME
import com.ugreyiro.ft_hangouts.db.repository.SettingsRepository

abstract class BaseActivity : AppCompatActivity(), ConfigurableHeaderActivity {

    abstract val settingsRepository : SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        setHeaderColor()
        super.onCreate(savedInstanceState)
    }
    override fun setHeaderColor() {
        val color = settingsRepository.settingValueByName(HEADER_COLOR_SETTING_NAME)
        setHeaderColor(color)
    }

    protected fun setHeaderColor(color : Int) {
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
    }

    fun getPermission(permission: String, activity : Activity) {
        val permissionStatus = ContextCompat.checkSelfPermission(activity, permission)
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
        }
    }
}