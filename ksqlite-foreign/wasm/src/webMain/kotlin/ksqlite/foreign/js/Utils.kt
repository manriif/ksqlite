package ksqlite.foreign.js

import kotlin.js.JsAny

/**
 * Returns the [property] of an object [instance].
 */
@JsFun("(instance, property) => instance[property]")
public external fun getMember(instance: JsAny, property: String): JsAny?