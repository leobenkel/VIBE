/*
 * Copyright 2019 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leobenkel.vibe.server.Utils.zioslick

import slick.basic.BasicBackend
import zio.UIO

/**
  * A trait used to select with databaseProvider will be used (a database provider gives the application the Slick database it'll need)
  */
trait DatabaseProvider {
  def databaseProvider: DatabaseProvider.Service
}

object DatabaseProvider {
  trait Service {
    def db: UIO[BasicBackend#DatabaseDef]
  }
}
