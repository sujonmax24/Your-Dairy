package com.sujonmax.yourdairy.ui.media

import android.location.Location
import android.net.Uri

object LocationAttachment {
    const val TYPE = "location"

    fun toUri(location: Location): Uri = Uri.parse(
        "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}"
    )

    fun label(location: Location): String =
        "${"%.6f".format(location.latitude)}, ${"%.6f".format(location.longitude)}"
}
