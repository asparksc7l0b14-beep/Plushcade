package com.mirvo.tazlen.plushcade.game.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.mirvo.tazlen.plushcade.R

object GameFonts {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

    val primary = FontFamily(Font(GoogleFont("Baloo 2"), provider))
    val hud = FontFamily(Font(GoogleFont("Nunito"), provider))
}
