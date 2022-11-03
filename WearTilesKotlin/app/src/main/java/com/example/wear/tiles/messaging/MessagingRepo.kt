/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.wear.tiles.messaging

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wear.tiles.messaging.Contact.Companion.toContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "contacts")

class MessagingRepo(private val context: Context) {
    fun getFavoriteContacts(): Flow<List<Contact>> = context.dataStore.data.map { preferences ->
        val count = preferences[intPreferencesKey("contact.count")] ?: 0

        (0 until count).mapNotNull {
            preferences[stringPreferencesKey("contact.$it")]?.toContact()
        }
    }

    suspend fun updateContacts(contacts: List<Contact>) {
        context.dataStore.edit {
            it.clear()
            contacts.forEachIndexed { index, contact ->
                it[stringPreferencesKey("contact.$index")] = contact.toPreferenceString()
            }
            it[intPreferencesKey("contact.count")] = contacts.size
        }
    }

    companion object {
        private const val avatarPath =
            "https://github.com/android/wear-os-samples/raw/main/WearTilesKotlin/" +
                "app/src/main/res/drawable-nodpi"

        val knownContacts = listOf(
            Contact(
                id = 0,
                initials = "JV",
                name = "Jyoti V",
                avatarUrl = null
            ),
            Contact(
                id = 1,
                initials = "AC",
                name = "Ali C",
                avatarUrl = "$avatarPath/ali.png"
            ),
            Contact(
                id = 2,
                initials = "TB",
                name = "Taylor B",
                avatarUrl = "$avatarPath/taylor.jpg"
            ),
            Contact(
                id = 3,
                initials = "FS",
                name = "Felipe S",
                avatarUrl = null
            ),
            Contact(
                id = 4,
                initials = "JG",
                name = "Judith G",
                avatarUrl = null
            ),
            Contact(
                id = 5,
                initials = "AO",
                name = "Andrew O",
                avatarUrl = null
            )
        )
    }
}
