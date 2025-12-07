package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

/**
 * Supabase Client Singleton
 *
 * Configure your Supabase project credentials here.
 *
 * Steps to get credentials:
 * 1. Go to your Supabase project dashboard
 * 2. Navigate to Settings > API
 * 3. Copy the "Project URL" and "anon/public key"
 * 4. Replace the placeholder values below
 */
object SupabaseClient {

    // TODO: Replace these with your actual Supabase project credentials
    private const val SUPABASE_URL = "https://voulnkdchojxkhpyildr.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_-AZaiV6Zlx43gssN0vNhyQ_OXTz1wlc"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
    }
}

