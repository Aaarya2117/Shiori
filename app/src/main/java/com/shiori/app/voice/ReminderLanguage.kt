package com.shiori.app.voice

enum class BhashiniTtsFamily(val defaultServiceId: String) {
    INDO_ARYAN("ai4bharat/indic-tts-coqui-indo_aryan-gpu--t4"),
    DRAVIDIAN("ai4bharat/indic-tts-coqui-dravidian-gpu--t4"),
    MISC("ai4bharat/indic-tts-coqui-misc-gpu--t4")
}

enum class ReminderLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val ttsFamily: BhashiniTtsFamily
) {
    ENGLISH("en", "English", "English", BhashiniTtsFamily.MISC),
    HINDI("hi", "Hindi", "हिन्दी", BhashiniTtsFamily.INDO_ARYAN),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", BhashiniTtsFamily.DRAVIDIAN),
    TAMIL("ta", "Tamil", "தமிழ்", BhashiniTtsFamily.DRAVIDIAN),
    TELUGU("te", "Telugu", "తెలుగు", BhashiniTtsFamily.DRAVIDIAN),
    MARATHI("mr", "Marathi", "मराठी", BhashiniTtsFamily.INDO_ARYAN),
    BENGALI("bn", "Bengali", "বাংলা", BhashiniTtsFamily.INDO_ARYAN),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", BhashiniTtsFamily.INDO_ARYAN),
    MALAYALAM("ml", "Malayalam", "മലയാളം", BhashiniTtsFamily.DRAVIDIAN),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", BhashiniTtsFamily.INDO_ARYAN);

    companion object {
        fun fromCode(code: String): ReminderLanguage {
            val normalized = code.trim().lowercase()
            return entries.firstOrNull { it.code == normalized } ?: ENGLISH
        }

        fun buildEnglishSourcePrompt(title: String, details: String): String {
            val cleanTitle = title.trim().ifEmpty { "Scheduled task" }
            val cleanDetails = details.trim()
            return if (cleanDetails.isNotEmpty()) {
                "Hello, this is your Shiori reminder: $cleanTitle. Instructions: $cleanDetails."
            } else {
                "Hello, this is your Shiori reminder: $cleanTitle."
            }
        }

        fun buildLocalizedFallbackPrompt(langCode: String, title: String, details: String): String {
            val lang = fromCode(langCode)
            val cleanTitle = title.trim().ifEmpty { "Reminder" }
            val cleanDetails = details.trim()
            val detailSuffix = if (cleanDetails.isNotEmpty()) " $cleanDetails." else ""

            return when (lang) {
                HINDI -> "यह शिओरी रिमाइंडर है: $cleanTitle।$detailSuffix"
                KANNADA -> "ಇದು ಶಿಯೋರಿ ಜ್ಞಾಪನೆ: $cleanTitle.$detailSuffix"
                TAMIL -> "இது ஷியோரி நினைவூட்டல்: $cleanTitle.$detailSuffix"
                TELUGU -> "ఇది షియోరి రిమైండర్: $cleanTitle.$detailSuffix"
                MARATHI -> "हा शिओरी रिमाइंडर आहे: $cleanTitle.$detailSuffix"
                BENGALI -> "এটি শিওরি রিমাইন্ডার: $cleanTitle।$detailSuffix"
                GUJARATI -> "આ શિઓરી રિમાઇન્ડર છે: $cleanTitle.$detailSuffix"
                MALAYALAM -> "ഇത് ഷിയോറി ഓർമ്മപ്പെടുത്തലാണ്: $cleanTitle.$detailSuffix"
                PUNJABI -> "ਇਹ ਸ਼ਿਓਰੀ ਰੀਮਾਈਂਡਰ ਹੈ: $cleanTitle।$detailSuffix"
                ENGLISH -> "This is your Shiori reminder: $cleanTitle.$detailSuffix"
            }
        }
    }
}
