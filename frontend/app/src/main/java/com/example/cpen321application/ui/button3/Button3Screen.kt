package com.example.cpen321application.ui.button3

import com.example.cpen321application.ui.theme.AppButtonShape
import androidx.compose.foundation.layout.height
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cpen321application.network.Joke
import com.example.cpen321application.network.fetchRandomJoke
import com.example.cpen321application.network.randomFallbackJoke
import java.io.IOException
import kotlinx.coroutines.delay

private const val MAX_INPUT_DIGITS = 3
private const val VIBRATION_MS = 500L

private enum class TimerPhase { SETUP, RUNNING, DONE }

@Composable
fun Button3Screen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var phase by remember { mutableStateOf(TimerPhase.SETUP) }
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var inputError by remember { mutableStateOf<String?>(null) }

    var joke by remember { mutableStateOf<Joke?>(null) }
    var punchlineShown by remember { mutableStateOf(false) }
    var jokeRequest by remember { mutableIntStateOf(0) }
    var giftOpened by remember { mutableStateOf(false) }

    // Counts down once per second while the timer is running.
    LaunchedEffect(phase) {
        if (phase != TimerPhase.RUNNING) return@LaunchedEffect
        while (remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds--
        }
        vibrate(context)
        phase = TimerPhase.DONE
    }

    // Fetches a joke when the timer finishes and again on "Another one!".
    LaunchedEffect(phase, jokeRequest) {
        if (phase != TimerPhase.DONE) return@LaunchedEffect
        joke = null
        punchlineShown = false
        joke = try {
            fetchRandomJoke()
        } catch (e: IOException) {
            randomFallbackJoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (phase) {
            TimerPhase.SETUP -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField("Minutes", minutesInput, { minutesInput = it }, Modifier.weight(1f))
                    NumberField("Seconds", secondsInput, { secondsInput = it }, Modifier.weight(1f))
                }
                inputError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        val total = (minutesInput.toIntOrNull() ?: 0) * 60 + (secondsInput.toIntOrNull() ?: 0)
                        if (total <= 0) {
                            inputError = "Enter a time greater than zero."
                        } else {
                            inputError = null
                            remainingSeconds = total
                            giftOpened = false
                            phase = TimerPhase.RUNNING
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = AppButtonShape
                ) {
                    Text("Start")
                }
            }

            TimerPhase.RUNNING -> {
                Text(
                    text = formatTime(remainingSeconds),
                    fontSize = 64.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = { phase = TimerPhase.SETUP }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = AppButtonShape) {
                    Text("Cancel")
                }
            }

            TimerPhase.DONE -> {
                if (!giftOpened) {
                    GiftBox(onOpen = { giftOpened = true })
                } else {
                    Text(
                        text = "Time's up! Here's a surprise:",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    JokeCard(joke = joke, punchlineShown = punchlineShown, onReveal = { punchlineShown = true })
                    Button(onClick = { jokeRequest++ }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = AppButtonShape) {
                        Text("Another one!")
                    }
                }
                OutlinedButton(onClick = { phase = TimerPhase.SETUP }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = AppButtonShape) {
                    Text("New timer")
                }
            }
        }
    }
}

@Composable
private fun GiftBox(onOpen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
            .clickable(onClick = onOpen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "\uD83C\uDF81", fontSize = 120.sp)
        Text(text = "???", style = MaterialTheme.typography.displayMedium)
        Text(text = "Tap to open your surprise", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(MAX_INPUT_DIGITS)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
private fun JokeCard(joke: Joke?, punchlineShown: Boolean, onReveal: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (joke == null) {
                CircularProgressIndicator()
            } else {
                Text(text = joke.setup, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                if (punchlineShown) {
                    Text(text = joke.punchline, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                } else {
                    Text(
                        text = "Tap to reveal the punchline",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable(onClick = onReveal)
                    )
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String =
    "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

private fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Vibrator::class.java) ?: return
    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
}
