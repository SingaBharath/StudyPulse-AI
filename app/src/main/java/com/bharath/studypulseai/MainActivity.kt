package com.bharath.studypulseai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharath.studypulseai.data.local.StudyPulseDatabase
import com.bharath.studypulseai.data.repository.StudyRepository
import com.bharath.studypulseai.presentation.ui.screen.HomeScreen
import com.bharath.studypulseai.presentation.viewmodel.StudyViewModel
import com.bharath.studypulseai.ui.theme.StudyPulseAITheme

/**
 * Entry point.
 *
 * Production note: replace manual dependency construction here with Hilt
 * (@HiltAndroidApp / @AndroidEntryPoint) so the ViewModel can receive
 * its repository via @HiltViewModel — zero factory boilerplate,
 * fully testable with HiltAndroidTest.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db         = StudyPulseDatabase.getInstance(this)
        val repository = StudyRepository(db.studyNoteDao())

        setContent {
            StudyPulseAITheme {
                val viewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.Factory(repository)
                )
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
