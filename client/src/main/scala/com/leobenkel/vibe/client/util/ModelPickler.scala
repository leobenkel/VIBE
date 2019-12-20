package com.leobenkel.vibe.client.util

import com.leobenkel.vibe.client.schemas.Temp
import upickle.default.{macroRW, ReadWriter => RW, _}

/**
 * Here's where we define all of the model object's picklers and unpicklers.
 * You may want to move this to the shared project, though I like to keep them separately in case
 * you want to use a different method for marshalling json between the client and server
 */
object ModelPickler {
  // TODO: To delete and import all the schema from 'core'
  implicit val SampleModelObjectRW: RW[Temp] = macroRW
}
