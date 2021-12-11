package com.kyledahlin.utils

import kotlinx.serialization.json.JsonObject

fun JsonObject.getObject(key: String) = get(key) as JsonObject