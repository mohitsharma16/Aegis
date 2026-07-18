package com.mslabs.aegis.services.passkey

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AegisCredentialService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
