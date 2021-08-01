package com.example.weather.view.contentProvider

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.weather.databinding.FragmentContentProviderBinding

const val REQUEST_CODE = 42

class ContentProviderFragment : Fragment() {
    private var _binding: FragmentContentProviderBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContentProviderBinding.inflate(
            inflater, container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
    }


    // Проверяем, разрешено ли чтение контактов
    private fun checkPermission() {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_CONTACTS
                ) ==
                        PackageManager.PERMISSION_GRANTED -> {
//Доступ к контактам на телефоне есть
                    getContacts()
                }
//Опционально: если нужно пояснение перед запросом разрешений
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                    AlertDialog.Builder(it)
                        .setTitle("Доступ к контактам")
                        .setMessage("Объяснение")
                        .setPositiveButton("Предоставить доступ") { _, _ ->
                            requestPermission()
                        }
                        .setNegativeButton("Не надо") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                else -> {
//Запрашиваем разрешение
                    requestPermission()
                }
            }
        }
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE)
    }

    // Обратный вызов после получения разрешений от пользователя
    override fun onRequestPermissionsResult(
        requestCode: Int,
        //например sms,mail,contacts
        permissions: Array<String>,
        //PERMISSION_DENIED - отклонено разрешение
        //PERMISSION_GRANTED - предоставляется разрешение
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
// Проверяем, дано ли пользователем разрешение по нашему запросу
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    getContacts()
                } else {
// Поясните пользователю, что экран останется пустым, потому что доступ к контактам не предоставлен
                    context?.let {
                        AlertDialog.Builder(it)
                            .setTitle("Доступ к контактам")
                            .setMessage("Объяснение")
                            .setNegativeButton("Закрыть") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
                return
            }
        }
    }

    private fun getContacts() {
        context?.let {
// Получаем ContentResolver у контекста
            val contentResolver: ContentResolver = it.contentResolver
// Отправляем запрос на получение контактов и получаем ответ в виде Cursor
            val cursorWithContacts: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )
            cursorWithContacts?.let { cursor ->
                for (i in 0..cursor.count) {
// Переходим на позицию в Cursor
                    if (cursor.moveToPosition(i)) {
// Берём из Cursor столбец с именем
                        val name =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        //addView(it, name)
                    }
                }
            }
            cursorWithContacts?.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContentProviderFragment()
    }
}