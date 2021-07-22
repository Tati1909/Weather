package com.example.weather.view.experiments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.weather.R
import com.example.weather.databinding.FragmentThreadsBinding
import kotlinx.android.synthetic.main.fragment_threads.*
import java.util.*
import java.util.concurrent.TimeUnit


//переменные для создания собственного интент-фильтра и
// передачи данных
const val TEST_BROADCAST_INTENT_FILTER = "TEST BROADCAST INTENT FILTER"
const val THREADS_FRAGMENT_BROADCAST_EXTRA = "THREADS_FRAGMENT_EXTRA"

class ThreadsFragment : Fragment() {
    private var _binding: FragmentThreadsBinding? = null
    private val binding get() = _binding!!
    private var counterThread = 0

    //Создаём свой BroadcastReceiver (получатель широковещательного сообщения)
    private val testReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Достаём данные из интента
            intent.getStringExtra(THREADS_FRAGMENT_BROADCAST_EXTRA)?.let { addView(context, it) }
        }
    }

    private fun addView(context: Context, it: String) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //подписываемся на BroadcastReceiver
        //LocalBroadcastManager более эффективен в пересылке сообщений внутри приложения
        context?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(
                    testReceiver,
                    IntentFilter(TEST_BROADCAST_INTENT_FILTER)
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThreadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            binding.textView.text =
                startCalculations(binding.editText.text.toString().toInt())
            binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                text = getString(R.string.in_main_thread)
                textSize =
                    resources.getDimension(R.dimen.main_container_text_size)
            })
        }
        binding.calcThreadBtn.setOnClickListener {
            Thread {
                counterThread++
                val calculatedText = startCalculations(editText.text.toString().toInt())
                activity?.runOnUiThread {
                    binding.textView.text = calculatedText
                    binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                        text = String.format(
                            getString(R.string.from_thread),
                            counterThread
                        )
                        textSize =
                            resources.getDimension(R.dimen.main_container_text_size)
                    })
                }
            }.start()
        }
        initServiceWithBroadcastButton()
    }

    //запускаем Сервис
    private fun initServiceWithBroadcastButton() {
        binding.serviceWithBroadcastButton.setOnClickListener {
            context?.let {
                it.startService(Intent(it, MainService::class.java).apply {
                    putExtra(
                        MAIN_SERVICE_INT_EXTRA,
                        binding.editText.text.toString().toInt()
                    )
                })
            }
        }
    }

    private fun startCalculations(seconds: Int): String {
        val date = Date()
        var diffInSec: Long
        do {
            val currentDate = Date()
            val diffInMs: Long = currentDate.time - date.time
            diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
        } while (diffInSec < seconds)
        return diffInSec.toString()
    }

    override fun onDestroy() {
        //отписываемся от BroadcastReceiver  c пом. LocalBroadcastManager
        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(testReceiver)
        }
        super.onDestroy()
    }

    companion object {
        fun newInstance() = ThreadsFragment()
    }
}