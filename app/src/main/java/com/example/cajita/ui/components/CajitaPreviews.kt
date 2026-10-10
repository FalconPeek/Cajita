package com.example.cajita.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cajita.ui.theme.CajitaTheme

@Preview(name = "Light Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewDenominationChip() {
    CajitaTheme {
        Surface {
            DenominationChip(denominacion = 20000)
        }
    }
}

@Preview(name = "Numeric Keypad Light", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Numeric Keypad Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewNumericKeypad() {
    CajitaTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                NumericKeypad(
                    onDigitClick = {},
                    onBackspaceClick = {},
                    onClearClick = {}
                )
            }
        }
    }
}
