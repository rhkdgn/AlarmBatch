package com.example.alarmbatch

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.AlarmClock
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast

// ─────────────────────────────────────────────────────────────
//  연속 알람 생성기
//  시작 시간 · 간격 · 종료 시간을 입력하면
//  삼성(안드로이드) 기본 시계 앱에 알람이 자동으로 딱딱딱 생성됨.
//  화면 코드는 XML 없이 전부 코드로만 그려서 최대한 단순하게 만듦.
// ─────────────────────────────────────────────────────────────
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(56, 72, 56, 56)
        }

        val title = TextView(this).apply {
            text = "연속 알람 생성기"
            textSize = 24f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 32)
        }

        val startH = numberInput("9")
        val startM = numberInput("0")
        val endH = numberInput("9")
        val endM = numberInput("30")
        val interval = numberInput("5")

        val button = Button(this).apply {
            text = "알람 만들기"
            setOnClickListener {
                try {
                    val n = createAlarms(
                        startH.text.toString().toInt(),
                        startM.text.toString().toInt(),
                        endH.text.toString().toInt(),
                        endM.text.toString().toInt(),
                        interval.text.toString().toInt()
                    )
                    toast("알람 ${n}개 생성 완료! 시계 앱에서 확인해봐")
                } catch (e: Exception) {
                    toast("입력값을 다시 확인해줘")
                }
            }
        }

        root.addView(title)
        root.addView(sectionLabel("시작 시간  (시 / 분)"))
        root.addView(timeRow(startH, startM))
        root.addView(sectionLabel("종료 시간  (시 / 분)"))
        root.addView(timeRow(endH, endM))
        root.addView(sectionLabel("간격 (분)"))
        root.addView(interval)
        root.addView(Space(this), ViewGroup.LayoutParams(1, 56))
        root.addView(button)

        setContentView(ScrollView(this).apply { addView(root) })
    }

    // ── UI 도우미 ────────────────────────────────────────────
    private fun sectionLabel(t: String) = TextView(this).apply {
        text = t
        textSize = 14f
        setTextColor(Color.DKGRAY)
        setPadding(0, 32, 0, 8)
    }

    private fun numberInput(default: String) = EditText(this).apply {
        setText(default)
        inputType = InputType.TYPE_CLASS_NUMBER
        textSize = 18f
    }

    private fun timeRow(hourField: EditText, minField: EditText): LinearLayout {
        val weight = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(hourField, LinearLayout.LayoutParams(weight))
            addView(minField, LinearLayout.LayoutParams(weight))
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ── 핵심 로직 ────────────────────────────────────────────
    //  이 함수가 전부. 시작~종료를 간격만큼 돌면서
    //  각 시각마다 시스템 알람 인텐트를 발사한다.
    private fun createAlarms(
        startH: Int, startM: Int,
        endH: Int, endM: Int,
        intervalMin: Int
    ): Int {
        require(intervalMin > 0) { "간격은 1분 이상" }

        var cur = startH * 60 + startM     // 시작을 '분' 단위로 환산
        val end = endH * 60 + endM         // 종료도 '분' 단위로
        var count = 0

        while (cur <= end) {
            val h = cur / 60
            val m = cur % 60

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, h)
                putExtra(AlarmClock.EXTRA_MINUTES, m)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)   // UI 안 뜨고 조용히 생성
                putExtra(AlarmClock.EXTRA_MESSAGE, "기상 %02d:%02d".format(h, m))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)

            cur += intervalMin
            count++
        }
        return count
    }
}
