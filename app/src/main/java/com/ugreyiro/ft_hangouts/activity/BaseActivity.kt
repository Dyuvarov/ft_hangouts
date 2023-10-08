package com.ugreyiro.ft_hangouts.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
}