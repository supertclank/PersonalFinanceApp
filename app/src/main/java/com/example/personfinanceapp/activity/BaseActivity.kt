import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import api.RetrofitClient
import com.example.personfinanceapp.utils.SharedPreferenceManager

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyUserPreferences()
    }

    private fun applyUserPreferences() {
        val sharedPrefManager = SharedPreferenceManager(this, apiService = RetrofitClient.instance)

        // Get the stored dark mode setting
        val isDarkModeEnabled = sharedPrefManager.isDarkModeEnabled()

        // Get the current night mode from resources
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        // Only update night mode if the preference is different from the current mode
        if (isDarkModeEnabled) {
            if (currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        } else {
            if (currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Apply font size as per the stored preference
        val fontSize = sharedPrefManager.getFontSize()
        when (fontSize) {
            "Small" -> setFontScale(0.85f)
            "Large" -> setFontScale(1.15f)
            else -> setFontScale(1.0f)
        }
    }

    private fun setFontScale(fontScale: Float) {
        val configuration = resources.configuration
        configuration.fontScale = fontScale
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}
