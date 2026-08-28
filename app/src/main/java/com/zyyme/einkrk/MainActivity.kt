package com.zyyme.einkrk

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyyme.einkrk.ui.theme.EinkRkTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        window.attributes = window.attributes.apply {
            gravity = Gravity.CENTER
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        SettingsRepository.apply(this)
        setContent {
            EinkRkTheme(darkTheme = false, dynamicColor = false) {
                SettingsPopup(onDismiss = ::finish)
            }
        }
    }
}

@Composable
private fun SettingsPopup(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val initial = remember { SettingsRepository.read(context) }
    var mode by remember { mutableIntStateOf(initial.mode) }
    var contrastProgress by remember { mutableFloatStateOf((1.5f - initial.gamma).coerceIn(0f, 1f)) }
    var gray256 by remember { mutableStateOf(initial.gray256) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(340.dp)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text("咩Eink设置", fontSize = 22.sp, color = Color.Black)
                Spacer(Modifier.height(16.dp))
                Text("刷新模式", fontSize = 16.sp, color = Color.Black)
                Spacer(Modifier.height(4.dp))
                listOf(13 to "抖动最快", 12 to "抖动快刷", 0 to "阅读块刷", 7 to "阅读").forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable {
                                mode = value
                                SettingsRepository.save(context, mode, 1.5f - contrastProgress, gray256)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = mode == value, onClick = {
                            mode = value
                            SettingsRepository.save(context, mode, 1.5f - contrastProgress, gray256)
                        })
                        Spacer(Modifier.width(8.dp))
                        Text(label, color = Color.Black, fontSize = 15.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFFCCCCCC), modifier = Modifier.padding(vertical = 10.dp))
                Text("对比度", fontSize = 16.sp, color = Color.Black)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text("1.5", color = Color.Black, fontSize = 12.sp)
                    Slider(
                        value = contrastProgress,
                        onValueChange = {
                            contrastProgress = it
                            SettingsRepository.save(context, mode, 1.5f - it, gray256)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
//                    Text("0.5", color = Color.Black, fontSize = 12.sp)
                }
                Text(
                    "${(contrastProgress * 100).toInt()}%   gamma ${String.format(Locale.US, "%.2f", 1.5f - contrastProgress)}",
                    color = Color.Black,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("抖256", color = Color.Black, fontSize = 16.sp)
                    Switch(checked = gray256, onCheckedChange = {
                        gray256 = it
                        SettingsRepository.save(context, mode, 1.5f - contrastProgress, it)
                    })
                }
            }
        }
    }
}
