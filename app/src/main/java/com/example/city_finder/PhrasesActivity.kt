package com.example.city_finder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.city_finder.databinding.ActivityPhrasesBinding

class PhrasesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhrasesBinding

    private val englishPhrases = listOf(
        "Hi my name is [name]",
        "Where is the nearest bus?",
        "How are you doing?",
        "I am lost",
        "How much is it?",
        "Thank you"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhrasesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countryCode = intent.getStringExtra("EXTRA_COUNTRY_CODE") ?: "US"

        binding.backButtonPhrases.setOnClickListener { finish() }

        val targetLanguage = getLanguageForCountry(countryCode)

        if (targetLanguage == "Language Not Found") {
            val intent = Intent(this, DadJokeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val translatedPhrases = getTranslations(targetLanguage)
        binding.targetLanguageName.text = targetLanguage

        englishPhrases.forEachIndexed { index, englishPhrase ->
            addPhraseView(englishPhrase, translatedPhrases[index])
        }
    }

    private fun addPhraseView(english: String, translated: String) {
        val inflater = LayoutInflater.from(this)
        val phraseView = inflater.inflate(R.layout.list_item_phrase, binding.phrasesContainer, false)

        val englishTextView = phraseView.findViewById<TextView>(R.id.english_phrase)
        val translatedTextView = phraseView.findViewById<TextView>(R.id.translated_phrase)

        englishTextView.text = english
        translatedTextView.text = translated

        binding.phrasesContainer.addView(phraseView)
    }

    private fun getLanguageForCountry(code: String): String {
        return when (code.uppercase()) {
            "FR", "BE", "CA", "CH", "LU", "MC", "SN" -> "French"
            "ES", "MX", "AR", "CO", "PE", "CL", "VE" -> "Spanish"
            "DE", "AT", "LI" -> "German"
            "RU", "BY", "KZ" -> "Russian"
            "CN", "SG", "TW" -> "Chinese"
            "JP" -> "Japanese"
            "IT", "SM" -> "Italian"
            "PT", "BR" -> "Portuguese"
            "EG", "SA", "AE", "JO", "MA" -> "Arabic"
            "IN", "PK", "NP" -> "Hindi"
            "BD" -> "Bengali"
            else -> "Language Not Found"
        }
    }

    private fun getTranslations(language: String): List<String> {
        return when (language) {
            "French" -> listOf(
                "Salut, je m'appelle [name]", "Où est le bus le plus proche?",
                "Comment ça va?", "Je suis perdu(e)", "Combien ça coûte?", "Merci"
            )
            "Spanish" -> listOf(
                "Hola, mi nombre es [name]", "¿Dónde está el autobús más cercano?",
                "¿Cómo estás?", "Estoy perdido(a)", "¿Cuánto cuesta?", "Gracias"
            )
            "German" -> listOf(
                "Hallo, mein Name ist [name]", "Wo ist der nächste Bus?",
                "Wie geht es Ihnen?", "Ich habe mich verirrt", "Wie viel kostet es?", "Danke"
            )
            "Russian" -> listOf(
                "Привет, меня зовут [name]", "Где ближайший автобус?",
                "Как дела?", "Я заблудился (zabлуди́лась)", "Сколько это стоит?", "Спасибо"
            )
            "Chinese" -> listOf(
                "你好, 我叫 [name] (Nǐ hǎo, wǒ jiào)", "最近的公共汽车站在哪里? (Zuìjìn de gōnggòng qìchē zhàn zài nǎlǐ?)",
                "你好吗? (Nǐ hǎo ma?)", "我迷路了 (Wǒ mílù le)", "这个多少钱? (Zhège duōshǎo qián?)", "谢谢 (Xièxiè)"
            )
            "Japanese" -> listOf(
                "こんにちは、私の名前は [name] です (Konnichiwa, ...)", "最寄りのバス停はどこですか? (Moyori no basutei wa doko desu ka?)",
                "お元気ですか? (O-genki desu ka?)", "道に迷いました (Michi ni mayoi mashita)", "これはいくらですか? (Kore wa ikura desu ka?)", "ありがとう (Arigatō)"
            )
            "Italian" -> listOf(
                "Ciao, mi chiamo [name]", "Dov'è la fermata dell'autobus più vicina?",
                "Come stai?", "Mi sono perso(a)", "Quanto costa?", "Grazie"
            )
            "Portuguese" -> listOf(
                "Oi, meu nome é [name]", "Onde é o ônibus mais próximo?",
                "Como você está?", "Estou perdido(a)", "Quanto custa?", "Obrigado(a)"
            )
            "Arabic" -> listOf(
                "مرحباً، اسمي [name]", "أين هي أقرب محطة حافلات؟",
                "كيف حالك؟", "لقد ضللت الطريق", "كم سعره؟", "شكراً"
            )
            "Hindi" -> listOf(
                "नमस्ते, मेरा नाम [name] है", "सबसे नज़दीकी बस स्टॉप कहाँ है?",
                "आप कैसे हैं?", "मैं खो गया हूँ", "इसका दाम कितना है?", "धन्यवाद"
            )

            "Bengali" -> listOf(
                "নমস্কার, আমার নাম [name]", "সবচেয়ে কাছের বাস স্টপ কোথায়?",
                "আপনি কেমন আছেন?", "আমি হারিয়ে গেছি", "এটার দাম কত?", "ধন্যবাদ"
            )
            else -> emptyList()
        }
    }
}