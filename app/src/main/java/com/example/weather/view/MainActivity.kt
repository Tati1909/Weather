package com.example.weather.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.R
import com.example.weather.databinding.MainActivityBinding
import com.example.weather.view.main.MainFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.ok.setOnClickListener(clickListener)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitAllowingStateLoss()
        }
    }

    /*var clickListener: View.OnClickListener = object : View.OnClickListener {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onClick(v: View?) {
            try {
                val uri = URL(binding.url.text.toString())
                val handler = Handler() //Запоминаем основной поток
                //Выходить в сеть из основного потока запрещается. Чтобы этого не происходило,
                //надо создавать отдельный поток и уже в потоке работать с сетью.
                //Создаём дополнительный поток для работы с сетью:
                Thread {
                    var urlConnection: HttpsURLConnection? = null
                    try {
                        urlConnection = uri.openConnection() as HttpsURLConnection
                        urlConnection.requestMethod =
                            "GET" // установка метода получения данных -- GET
                        urlConnection.readTimeout =
                            10000 // установка таймаута -- 10 000 миллисекунд
                        // читаем данные в поток
                        val reader =
                            BufferedReader(InputStreamReader(urlConnection.getInputStream()))
                        val result = getLines(reader)
                        // Возвращаемся к основному потоку
                        handler.post {
                            //загружаем результат в нашу view
                            //Все методы WebView должны вызываться в одном потоке
                            //для этого будем использовать специальный класс Handler
                            binding.webview.loadDataWithBaseURL(
                                null,
                                result,
                                "text/html; charset=utf-8",
                                "utf-8",
                                null
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("", "Fail connection", e)
                        e.printStackTrace()
                    } finally {
                        urlConnection?.disconnect()
                    }
                }.start()
            } catch (e: MalformedURLException) {
                Log.e("", "Fail URI", e)
                e.printStackTrace()
            }
        }

        //читаем данные(используется аннотация, т к lines у ридера добавился в 30 API
        @RequiresApi(Build.VERSION_CODES.N)
        private fun getLines(reader: BufferedReader): String {
            //lines - Возвращает Stream, элементами которого являются строки,
            // прочитанные из этого BufferedReader. Поле Stream заполняется лениво, т. Е.
            // Выполняется только чтение во время операции терминального потока .
            return reader.lines().collect(Collectors.joining("\n"))
        }
    }*/
}
